package com.astrosoft.utils

fun String.readFile(): String {
  return DB::class.java.getResource(this).readText()
}