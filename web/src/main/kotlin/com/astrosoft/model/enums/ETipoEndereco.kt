package com.astrosoft.model.enums

enum class ETipoEndereco(private val descricao: String) {
  DEPOSITO("Depósito"),
  RECEBIMENTO("Recebimento"),
  EXPEDICAO("Expedição");

  override fun toString(): String {
    return descricao
  }
}
