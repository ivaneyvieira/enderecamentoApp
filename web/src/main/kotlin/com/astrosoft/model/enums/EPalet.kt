package com.astrosoft.model.enums

enum class EPalet(val descricao: String, val sigla: String, val tamanho: Int?) {
  P("Pallet Pequeno", "P", 10),
  G("Pallet Grande", "G", 15),
  T("Transbordando", "T", 20),
  X("Todo Apartamento", "X", 30);

  override fun toString(): String {
    return this.descricao
  }

  companion object {
    val espacoTotal: Int
      get() = 30
  }
}
