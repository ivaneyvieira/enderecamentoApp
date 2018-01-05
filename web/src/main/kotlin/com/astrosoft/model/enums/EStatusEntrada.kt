package com.astrosoft.model.enums

enum class EStatusEntrada(private val descricao: String) {
  NAO_RECEBIDA("Não recebida"),
  RECEBIDA("Recebida"),
  ENDERECADA("Endereçado"),
  CONFERIDA("Conferida"),
  INCONSISTENTE("Inconsistente");

  override fun toString(): String {
    return descricao
  }
}
