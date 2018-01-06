package com.astrosoft.utils

import java.io.FileInputStream
import java.util.Properties

class DB(banco: String) {

  private val prop = properties()

  val driver = prop?.getProperty("$banco.driver") ?: ""
  val url = prop?.getProperty("$banco.url") ?: ""
  val username = prop?.getProperty("$banco.username") ?: ""
  val password = prop?.getProperty("$banco.password") ?: ""

  companion object {
    private fun propertieFile(): String? {
      val properties = Properties()
      val configFile = SystemUtils.getResourceAsStream("/configDB.properties")
      properties.load(configFile)
      return properties.getProperty("propertieFile")
    }

    private fun properties(): Properties? {
      val properties = Properties()
      val configFile = FileInputStream(propertieFile())
      properties.load(configFile)
      return properties
    }
  }
}