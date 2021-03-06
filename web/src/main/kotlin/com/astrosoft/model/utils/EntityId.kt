package com.astrosoft.model.util

import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.NativeSqlFilter
import com.github.vok.framework.sql2o.db

abstract class EntityId : Entity<Long> {
  abstract override var id: Long?
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EntityId

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    return id?.hashCode() ?: 0
  }
}

fun <T : Any> scriptRunner(classe: Class<T>, script: String, params: Map<String, Any> = mapOf()): List<T> {
  var scriptParam = ""
  params.forEach { (param, value) ->
    scriptParam = script.replace(":$param", "$value")
  }
  val sqls = scriptParam.split(";").toList()
  val sqlPrincipal = sqls.last()

  return db {
    val lastIndex = sqls.lastIndex
    sqls.forEachIndexed { index, sql ->
      if (index < lastIndex)
        con.createQuery(sql).executeUpdate()
    }
    con.createQuery(sqlPrincipal).executeAndFetch(classe)
  }
}

fun updateRunner(script: String, params: Map<String, Any> = mapOf()) {
  var scriptParam = ""
  params.forEach { (param, value) ->
    scriptParam = script.replace(":$param", "$value")
  }
  val sqls = scriptParam.split(";").toList()

  return db {
    sqls.forEach { sql ->
      con.createQuery(sql).executeUpdate()
    }
  }
}

fun <T : Any> Any?.isNull() = NativeSqlFilter<T>("$this is null", mapOf())