/***********************************************************************
 * Copyright (c) 2013-2018 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.security

import java.util

/**
 * Default implementation of the AuthorizationsProvider that doesn't provide any authorizations
 */
class DefaultAuthorizationsProvider extends AuthorizationsProvider {

  var authorizations: util.List[String] = new util.ArrayList[String]()

  override def getAuthorizations: util.List[String] = authorizations

  override def configure(params: java.util.Map[String, java.io.Serializable]) {
    val authString = AuthsParam.lookup(params)
    if (authString == null || authString.isEmpty) {
      authorizations = new util.ArrayList[String]()
    } else {
      authorizations = util.Arrays.asList(authString.split(","): _*)
    }
  }

}