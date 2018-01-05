package com.astrosoft.utils

import java.io.File

fun String.readFile() : String {
  val file = File(this)
  return file.readText()
}