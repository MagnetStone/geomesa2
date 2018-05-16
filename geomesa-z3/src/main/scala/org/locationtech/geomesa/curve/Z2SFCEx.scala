/***********************************************************************
 * Copyright (c) 2013-2018 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.curve

import org.locationtech.geomesa.curve.NormalizedDimension.BitNormalizedDimension

/**
  * z2 space-filling curve扩展，支持非经纬度投影数据
  *
  * Created by liyq on 2018/4/28.
  *
  * @param precision precision number of bits used per dimension - note sum must be less than 64
  * @param xBounds   x方向极值
  * @param yBounds   y方向极值
  */
class Z2SFCEx(precision: Int,
              xBounds: (Double, Double),
              yBounds: (Double, Double)) extends Z2SFC(precision) {
  override val lon: NormalizedDimension = new BitNormalizedDimension(xBounds._1, xBounds._2, precision)
  override val lat: NormalizedDimension = new BitNormalizedDimension(yBounds._1, yBounds._2, precision)
}

object Z2SFCEx {
  private val cache = new java.util.concurrent.ConcurrentHashMap[Int, Z2SFCEx]()

  def apply(precision: Int, xBounds: (Double, Double), yBounds: (Double, Double)): Z2SFCEx = {
    var sfc = cache.get(precision)

    if (sfc == null) {
      sfc = new Z2SFCEx(precision, xBounds, yBounds)
      cache.put(precision, sfc)
    }

    sfc
  }
}