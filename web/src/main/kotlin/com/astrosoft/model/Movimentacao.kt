package com.astrosoft.model

import com.astrosoft.model.enums.EMovTipo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll
import java.text.DecimalFormat
import java.time.LocalDate

@Table("movimentacoes")
data class Movimentacao(
        override var id: Long? = null,
        var chave: String? = "",
        var documento: String? = "",
        var data: LocalDate? = LocalDate.now(),
        var observacao: String? = "",
        var tipoMov: EMovTipo? = EMovTipo.ENTRADA
                       ) : Entity<Long> {
  companion object : Dao<Movimentacao>

  @get:JsonIgnore
  val movProduto
    get() = MovProduto.dataProvider.and { MovProduto::idMovimentacao eq id }

  fun findNotaEntrada(invno: Int): Movimentacao? {
    val chaveEntrada = montaChaveEntrada(invno)
    return findMovimentacao(chaveEntrada)
  }

  fun findMovimentacao(chaveEntrada: String): Movimentacao? {
    return Movimentacao.dataProvider.and { Movimentacao::chave eq chaveEntrada }.getAll().firstOrNull()
  }

  fun findMovProduto(produto: Produto): MovProduto? {
    return MovProduto.dataProvider.and { MovProduto::idMovimentacao eq id }
            .and{ MovProduto::idProduto eq produto.id}.getAll()
            .firstOrNull()
  }

  private fun montaChave(prefixo: String, strNumero: Int): String {
    return prefixo + DecimalFormat("00000000").format(strNumero)
  }

  fun montaChave(prefixo: String, str: String): String {
    return prefixo + str
  }

  fun montaChaveEntrada(invno: Int): String {
    return montaChave("NE", invno)
  }
}