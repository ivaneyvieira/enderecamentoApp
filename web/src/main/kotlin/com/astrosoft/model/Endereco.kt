package com.astrosoft.model

import com.astrosoft.model.enums.EPalet
import com.astrosoft.model.enums.ETipoAltura
import com.astrosoft.model.enums.ETipoEndereco
import com.astrosoft.model.enums.ETipoNivel
import com.astrosoft.model.util.EntityId
import com.astrosoft.model.util.scriptRunner
import com.astrosoft.utils.readFile
import com.astrosoft.vok.ViewException
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll

@Table("enderecos")
data class Endereco(
        override var id: Long? = null,
        var tipoEndereco: ETipoEndereco? = ETipoEndereco.DEPOSITO,
        var observacao: String? = "",
        var localizacao: String? = null,
        var tipoNivel: ETipoNivel? = null
                   ) : EntityId() {
  companion object : Dao<Endereco>

  @get:JsonIgnore
  val apto
    get() = Apto.dataProvider.and { Apto::idEndereco eq id }.getAll().firstOrNull()

  @get:JsonIgnore
  val saldos
    get() = Saldo.dataProvider.and { Saldo::idEndereco eq id }

  @get:JsonIgnore
  val transferenciaEnt
    get() = Transferencia.dataProvider.and { Transferencia::idEnderecoEnt eq id }

  @get:JsonIgnore
  val transferenciaSai
    get() = Transferencia.dataProvider.and { Transferencia::idEnderecoSai eq id }

  fun recebimento(): Endereco {
    val endereco = Endereco.dataProvider
            .and { Endereco::tipoEndereco eq ETipoEndereco.RECEBIMENTO }
            .getAll()
            .firstOrNull()
    if (endereco != null) return endereco
    throw ViewException("Não há endereco de recebimento cadastrado")
  }

  fun disponiveis(palet: EPalet, altura: ETipoAltura, ruas: List<Rua>): List<Endereco> {
    val ruasQuery = if (ruas.isEmpty()) Rua.ruasPulmao() else ruas

    val params = mapOf("tipoPalet" to palet.sigla,
                       "tipoAltura" to altura,
                       "ruas" to ruasQuery.joinToString(separator = ","))
    return scriptRunner(Endereco::class.java, "/sql/enderecosDisponiveis.sql".readFile(), params)
  }

  fun enrederecoPickingQuebec(): Endereco? = findEndereco(ETipoNivel.PULMAO, "00-00-00-00")

  fun findEndereco(tipoNivel: ETipoNivel, localizacao: String): Endereco? {
    return Endereco.dataProvider.and { Endereco::tipoNivel eq tipoNivel }
            .and { Endereco::localizacao eq localizacao }.getAll().firstOrNull()
  }

  val descricao
    get() = when (tipoEndereco) {
      ETipoEndereco.DEPOSITO    -> descricaoDeposito()
      ETipoEndereco.RECEBIMENTO -> "Recebimento"
      ETipoEndereco.EXPEDICAO   -> "Espedicao"
      else                      -> null
    }


  private fun descricaoDeposito(): String {
    return tipoNivel.toString() + " " + localizacao.orEmpty()
  }

  val rua get() = predio?.rua

  val predio get() = nivel?.predio

  val nivel get() = apto?.nivel

  fun enderecoOcupado(bean: Endereco): Boolean {
    val s = QSaldo.saldo
    val e = QEndereco.endereco
    val enderecoOpt = fetchOne { q ->
      q.select(e).from(e).leftJoin(s).on(s.idEndereco.eq(e.id)).where(e.tipoEndereco.eq(ETipoEndereco.DEPOSITO).and(
              e.id.eq(
                      bean.id))).groupBy(e.id).having(s.saldoConfirmado.sum().gt(0))
    }
    return enderecoOpt != null
  }

  fun saldosNaoZerado(bean: Endereco): List<Saldo> {
    val s = QSaldo.saldo
    return fetch { q ->
      q.selectFrom(s).where(s.idEndereco.eq(bean.id).and(s.saldoConfirmado.eq(BigDecimal.ZERO).not()))
    }
  }

  fun findEnderecoPiking(strEndereco: String): Endereco? {
    return findEndereco(PICKING, strEndereco)
  }

  val enderecosPicking: List<Endereco> by lazy {
    val enderecos: List<Endereco> = findAll()
    enderecos.filter { e -> e.tipoNivel == PICKING || e.tipoEndereco == EXPEDICAO }
  }

  companion object Factory : EnderecoService()

  fun enderecoPiking(produto: Produto): List<Endereco> {
    val e = QEndereco.endereco
    val s = QSaldo.saldo
    val p = QProduto.produto
    return fetch { q ->
      q.select(e).from(e).
              innerJoin(s).on(s.idEndereco.eq(e.id)).
              innerJoin(p).on(p.id.eq(s.idProduto)).
              where(p.id.eq(produto.id).
                      and(e.tipoEndereco.eq(DEPOSITO)).
                      and(e.tipoNivel.eq(PICKING))).
              orderBy(s.saldoConfirmado.sum().desc()).
              groupBy(e.id)
    }
  }
}