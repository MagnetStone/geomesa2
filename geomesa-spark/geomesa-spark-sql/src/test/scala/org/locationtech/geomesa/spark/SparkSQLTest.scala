/***********************************************************************
 * Copyright (c) 2013-2018 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

/** *********************************************************************
  * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Apache License, Version 2.0
  * which accompanies this distribution and is available at
  * http://www.opensource.org/licenses/apache2.0.php.
  * **********************************************************************/

package org.locationtech.geomesa.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.locationtech.geomesa.hbase.data.HBaseDataStoreParams

/**
  * Created by liyq on 2018/4/19.
  */
object SparkSQLTest {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("HBaseWriterTest").setMaster("local[*]")
    val spark = SparkSession
      .builder
      .config(conf)
      .config("spark.sql.warehouse.dir", "file:///F:/Code/geomesa/geomesa/spark-warehouse")
      .getOrCreate
    spark.sparkContext.setLogLevel("info")

    val dsParams = Map(
      HBaseDataStoreParams.HBaseCatalogParam.getName -> "test",
      "geomesa.feature" -> "line_csv")
      //"cache" -> "true")

    val df = spark.read.
      format("geomesa").
      options(dsParams).
      load()

    df.createOrReplaceTempView("tmp")

    val df2 = spark.sql("select * from tmp where st_contains" +
      "(st_makeBBOX(-75.6527687882974,4.07973590702689,471.698535243407,392.13421372307), geom)")
    df2.show(25)
  }
}
