package com.astrosoft.model.enums

enum class ESimNao(private val descricao: String) {
  SIM("Sim"),
  NAO("Não");

  override fun toString(): String {
    return this.descricao
  }
}
