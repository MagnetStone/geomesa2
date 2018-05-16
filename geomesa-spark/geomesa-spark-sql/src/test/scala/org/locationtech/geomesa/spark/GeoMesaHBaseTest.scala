/***********************************************************************
 * Copyright (c) 2013-2018 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.spark

import org.apache.hadoop.conf.Configuration
import org.apache.spark.{SparkConf, SparkContext}
import org.geotools.data.{DataStoreFinder, Query}
import org.geotools.filter.text.ecql.ECQL
import org.locationtech.geomesa.hbase.data.{HBaseDataStore, HBaseDataStoreParams}
import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.collection.JavaConversions._
import org.locationtech.geomesa.utils.geotools.RichSimpleFeatureType.RichSimpleFeatureType

/**
  * Created by liyq on 2018/4/24.
  */
class GeoMesaHBaseTest extends FlatSpec with BeforeAndAfter {
  // 定义SparkConf和SparkContext
  val master = "local[*]"
  var sc: SparkContext = _
  val conf: SparkConf = new SparkConf().setAppName("GeoMesaHBaseTest").setMaster(master)

  before {
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.kryo.registrator", classOf[GeoMesaSparkKryoRegistrator].getName)
    sc = SparkContext.getOrCreate(conf)
    sc.setLogLevel("WARN")
    println("*****before***** ")
  }

  after {
    if (sc != null) {
      sc.stop()
    }
    println("******after***** ")
  }

  "GeoMesaHBaseLineWriterTest" should "write Line" in {
    val csvPath = "line_csv.csv"
    val converterKey =
      """
        | {
        |   type         = "delimited-text",
        |   format       = "CSV",
        |   id-field     = "uuid()",
        |   fields = [
        |     { name = "userID",   transform = "$1::long"     }
        |     { name = "geom",     transform = "geometry($2)" }
        |   ]
        | }
      """.stripMargin
    val sftKey =
      """
        |geomesa {
        |  sfts {
        |    "line_csv" = {
        |      attributes = [
        |        { name = "userID",   type = "Long",       index = false                }
        |        { name = "geom",     type = "LineString", index = true, default = true }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin

    val params = Map(
      "geomesa.converter.inputs" -> csvPath,
      "geomesa.converter" -> converterKey,
      "geomesa.sft" -> sftKey)

    try {
      val rdd = GeoMesaSpark(params).rdd(new Configuration(),
        sc, params, new Query("line_csv"))
      println(rdd.count())

      val params2 = Map(HBaseDataStoreParams.HBaseCatalogParam.getName -> "test")
      val ds = DataStoreFinder.getDataStore(params2).asInstanceOf[HBaseDataStore]
      val featureType = rdd.schema
      featureType.setZBounds("-75.6527687882974,471.698535243407,4.07973590702689,392.13421372307")
      ds.createSchema(featureType)

      GeoMesaSpark(params2).save(rdd, params2, "line_csv")
    } catch {
      case _: Throwable =>
        assert(false, "写数据失败，正确捕获到异常")
    }
  }

  "GeoMesaHBasePointWriterTest" should "Write Point" in {
    val csvPath = "New_Point_1.csv"
    val converterKey =
      """
        | {
        |   type         = "delimited-text",
        |   format       = "CSV",
        |   id-field     = "uuid()",
        |   fields = [
        |     { name = "userID",   transform = "$1::long"     }
        |     { name = "geom",     transform = "geometry($2)" }
        |   ]
        | }
      """.stripMargin
    val sftKey =
      """
        |geomesa {
        |  sfts {
        |    "New_Point1" = {
        |      attributes = [
        |        { name = "userID",   type = "Long",      index = false                }
        |        { name = "geom",     type = "Point",     index = true, default = true }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin

    val params = Map(
      "geomesa.converter.inputs" -> csvPath,
      "geomesa.converter" -> converterKey,
      "geomesa.sft" -> sftKey)

    val typeName = "New_Point1"
//    val typeName = "PointTest"
//    val bounds = "88,120,26,55"
    val bounds = "58,512,100,429"

    try {
      val rdd = GeoMesaSpark(params).rdd(new Configuration(),
        sc, params, new Query(typeName))
      println(rdd.count())

      val params2 = Map(HBaseDataStoreParams.HBaseCatalogParam.getName -> "test")
      val ds = DataStoreFinder.getDataStore(params2).asInstanceOf[HBaseDataStore]
      val featureType = rdd.schema
      featureType.setZBounds(bounds)
      ds.createSchema(featureType)

      GeoMesaSpark(params2).save(rdd, params2, typeName)
    } catch {
      case _: Throwable =>
        assert(false, "写数据失败，正确捕获到异常")
    }
  }

  "GeoMesaHBasePointZWriterTest" should "Write Point with data" in {
    val csvPath = "New_Point.csv"
    val converterKey =
      """
        | {
        |   type         = "delimited-text",
        |   format       = "CSV",
        |   id-field     = "uuid()",
        |   fields = [
        |     { name = "userID",   transform = "$1::long"                                           }
        |     { name = "dtg",      transform = "date('yyyy-MM-dd HH:mm:ss z', concat($2, ' +08:00'))"}
        |     { name = "geom",     transform = "geometry($3)"                                       }
        |   ]
        | }
      """.stripMargin
    val sftKey =
      """
        |geomesa {
        |  sfts {
        |    "New_Point" = {
        |      attributes = [
        |        { name = "userID",   type = "Long",      index = false                }
        |        { name = "dtg",      type = "Date",      index = false                }
        |        { name = "geom",     type = "Point",     index = true, default = true }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin

    val params = Map(
      "geomesa.converter.inputs" -> csvPath,
      "geomesa.converter" -> converterKey,
      "geomesa.sft" -> sftKey)

    val typeName = "New_Point"
    val bounds = "58,512,100,429"

    try {
      val rdd = GeoMesaSpark(params).rdd(new Configuration(),
        sc, params, new Query(typeName))
      println(rdd.count())

      val params2 = Map(HBaseDataStoreParams.HBaseCatalogParam.getName -> "test")
      val ds = DataStoreFinder.getDataStore(params2).asInstanceOf[HBaseDataStore]
      val featureType = rdd.schema
      featureType.setZBounds(bounds)
      ds.createSchema(featureType)

      GeoMesaSpark(params2).save(rdd, params2, typeName)
    } catch {
      case _: Throwable =>
        assert(false, "写数据失败，正确捕获到异常")
    }
  }

  "GeoMesaHBaseLineReaderTest" should "read Line" in {
    val polygon = "POLYGON ((-75.6527687882973940 392.1342137230749900, " +
      "471.6985352434069800 392.1342137230749900, 471.6985352434069800 4.0797359070268904, " +
      "-74.7266244498343040 4.0797359070268904, -75.6527687882973940 392.1342137230749900))"
    val filter = s"CONTAINS($polygon, geom)"
    val params = Map(HBaseDataStoreParams.HBaseCatalogParam.getName -> "test")

    try {
      val rdd = GeoMesaSpark(params).rdd(new Configuration(),
        sc, params, new Query("line_csv", ECQL.toFilter(filter)))

      println(rdd.count())

      //    val feature = rdd.filter(_.getAttribute("UserID").asInstanceOf[Long].equals(2l)).collect()(0)
      val ff = rdd.collect()
      for (i <- ff.indices) {
        println("UserID = " + ff(i).getAttribute(0))
        println("Geom = " + ff(i).getAttribute(1))
      }
    } catch {
      case _: Throwable =>
        assert(false, "读数据失败，正确捕获到异常")
    }
  }

  "GeoMesaHBasePointReaderTest" should "read Point" in {
//    val polygon = "POLYGON((88 26, 120 26, 120 55, 88 55, 88 26))"
    val polygon = "POLYGON((58 100, 512 100, 512 429, 58 429, 58 100))"
    val filter = s"CONTAINS($polygon, geom)"
    val params = Map(HBaseDataStoreParams.HBaseCatalogParam.getName -> "test")
    val typeName = "New_Point1"
//    val typeName = "PointTest"

    try {
      val rdd = GeoMesaSpark(params).rdd(new Configuration(),
        sc, params, new Query(typeName, ECQL.toFilter(filter)))
      print(rdd.count())

      //    val feature = rdd.filter(_.getAttribute("UserID").asInstanceOf[Long].equals(2l)).collect()(0)
      val ff = rdd.collect()
      print("**************")
      println(ff.length)
      for (i <- ff.indices) {
        println("UserID = " + ff(i).getAttribute(0))
        println("Geom = " + ff(i).getAttribute(1))
      }
    } catch {
      case _: Throwable =>
        assert(false, "读数据失败，正确捕获到异常")
    }
  }

  "GeoMesaHBasePointZReaderTest" should "read Point with date" in {
    val polygon = "POLYGON((58 100, 512 100, 512 429, 58 429, 58 100))"
    val during = "2018-04-24T10:43:44+0800/2018-04-24T10:44:45+0800"
//    val filter = s"dtg during $during"
    val filter = s"CONTAINS($polygon, geom)"
    val params = Map(HBaseDataStoreParams.HBaseCatalogParam.getName -> "test")
    val typeName = "New_Point"

    try {
      val rdd = GeoMesaSpark(params).rdd(new Configuration(),
        sc, params, new Query(typeName, ECQL.toFilter(filter)))
      println("******* NumPartitions = " +rdd.getNumPartitions + " ******")

      val ff = rdd.collect()
      println("***************" + ff.length + "************")
      for (i <- ff.indices) {
        println("fid  = " + ff(i).getAttribute(0))
        println("dtg  = " + ff(i).getAttribute(1))
        println("geom = " + ff(i).getAttribute(2))
      }
    } catch {
      case _: Throwable =>
        assert(false, "读数据失败，正确捕获到异常")
    }
  }
}