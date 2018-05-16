/***********************************************************************
 * Copyright (c) 2013-2018 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.curve

import org.locationtech.geomesa.curve.NormalizedDimension.BitNormalizedDimension
import org.locationtech.geomesa.curve.TimePeriod.TimePeriod

/**
  * Created by liyq on 2018/4/28.
  *
  * Z3 space filling curve扩展，支持非经纬度投影数据
  *
  * @param period    time period used to bin results
  * @param precision bits used per dimension - note all precisions must sum to less than 64
  * @param xBounds   x方向极值
  * @param yBounds   y方向极值
  */
class Z3SFCEx(period: TimePeriod,
              precision: Int,
              xBounds: (Double, Double),
              yBounds: (Double, Double)) extends Z3SFC(period, precision) {
  override val lon: NormalizedDimension = new BitNormalizedDimension(xBounds._1, xBounds._2, precision)
  override val lat: NormalizedDimension = new BitNormalizedDimension(yBounds._1, yBounds._2, precision)
}

object Z3SFCEx {
  private val cache = new java.util.concurrent.ConcurrentHashMap[Int, Z3SFCEx]()

  def apply(period: TimePeriod,
            precision: Int,
            xBounds: (Double, Double),
            yBounds: (Double, Double)): Z3SFCEx = {
    var sfc = cache.get(precision)

    if (sfc == null) {
      sfc = {
        period match {
          case TimePeriod.Day => new Z3SFCEx(TimePeriod.Day, precision, xBounds, yBounds)
          case TimePeriod.Week => new Z3SFCEx(TimePeriod.Week, precision, xBounds, yBounds)
          case TimePeriod.Month => new Z3SFCEx(TimePeriod.Month, precision, xBounds, yBounds)
          case TimePeriod.Year => new Z3SFCEx(TimePeriod.Year, precision, xBounds, yBounds)
        }
      }
      cache.put(precision, sfc)
    }

    sfc
  }
}