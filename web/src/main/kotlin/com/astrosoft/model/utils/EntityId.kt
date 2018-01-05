package com.astrosoft.model.util

import com.github.vok.framework.sql2o.Entity

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