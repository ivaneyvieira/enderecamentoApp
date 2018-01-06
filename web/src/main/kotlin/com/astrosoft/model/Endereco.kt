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
import java.math.BigDecimal

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

  fun enderecoOcupado(): Boolean {
    val sql = """
select e.id as idEndereco, IFNULL(SUM(saldoConfirmado), 0) as saldo
from enderecos as e
  left join saldos as s
    ON e.id = s.idEndereco
where tipoEndereco = 'DEPOSITO'
  and e.id = $id"""
    val saldoEndereco = scriptRunner(SaldoEndereco::class.java, sql).firstOrNull()
    return saldoEndereco?.saldo?.let { it.toDouble() > 0.00 } ?: true
  }


  fun saldosNaoZerado(): List<Saldo> {
    val sql = """
select s.*
from saldos as s
where idEndereco = :id
  and saldoConfirmado <> 0
"""
    return scriptRunner(Saldo::class.java, sql)
  }

  fun findEnderecoPiking(strEndereco: String): Endereco? {
    return findEndereco(ETipoNivel.PICKING, strEndereco)
  }

  val enderecosPicking: List<Endereco> by lazy {
    val enderecos = Endereco.dataProvider.getAll()
    enderecos.filter { e -> e.tipoNivel == ETipoNivel.PICKING || e.tipoEndereco == ETipoEndereco.EXPEDICAO }
  }

  fun enderecoPiking(produto: Produto): List<Endereco> {
    val sql = """
      select e.*
from enderecos as e
  inner join saldos as s
    on e.id = s.idProduto
  inner join produtos as p
    on p.id = s.idProduto
where tipoEndereco = 'DEPOSITO'
  and tipoNivel = 'PICKING'
  and p.id = ${produto.id}
GROUP BY e.id
order by sum(saldoConfirmado) desc
    """.trimIndent()
    return scriptRunner(Endereco::class.java, sql)
  }
}

data class SaldoEndereco(
        var idEndereco: Long? = null,
        var saldo: BigDecimal? = null
                        )