package com.astrosoft.model.enums

enum class ETipoNivel(private val descricao: String) {
  PICKING("Picking"),
  PULMAO("Pulmão");

  override fun toString(): String {
    return descricao
  }
}
