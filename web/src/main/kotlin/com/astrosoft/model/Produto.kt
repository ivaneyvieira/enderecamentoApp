package com.astrosoft.model

import com.astrosoft.model.dtos.QuantProduto
import com.astrosoft.model.enums.EMovTipo
import com.astrosoft.model.util.EntityId
import com.astrosoft.model.util.scriptRunner
import com.astrosoft.model.util.updateRunner
import com.astrosoft.utils.lpad
import com.astrosoft.utils.readFile
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.findById
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll
import java.math.BigDecimal
import java.time.LocalDate

@Table("produtos")
data class Produto(
        override var id: Long? = null,
        var codbar: String? = "",
        var grade: String? = "",
        var nome: String? = "",
        var prdno: String? = "",
        var clno: Int? = 0,
        var vendno: Int? = 0,
        var custo: BigDecimal? = BigDecimal.ZERO,
        var preco: BigDecimal? = BigDecimal.ZERO,
        var quantVolumes: Int? = 0,
        var estoqueMinimo: BigDecimal? = BigDecimal.ZERO
                  ) : EntityId() {
  companion object : Dao<Produto> {
    fun findProduto(prdno: String, grade: String?): Produto? {
      return Produto.dataProvider
              .and { Produto::prdno eq prdno }
              .and { Produto::grade eq grade }
              .getAll().firstOrNull()
    }

    fun quantidadePalete(prdno: String): BigDecimal? {
      var param = mapOf("prdno" to prdno)

      return scriptRunner(QuantProduto::class.java, "/sql/produtosPalete.sql".readFile(), param)
              .firstOrNull()?.quantMov
    }
  }

  @get:JsonIgnore
  val movProduto
    get() = MovProduto.dataProvider.and { MovProduto::idProduto eq id }

  @get:JsonIgnore
  val saldos
    get() = Saldo.dataProvider.and { Saldo::idProduto eq id }


  fun findProdutoQuery(query: String): List<Produto> {
    if (query.trim().length <= 6) {
      val prdNorm = query.trim().lpad(6, "0")
      val prdno = prdNorm.lpad(16, " ")
      return findProdutoPrdno(prdno)
    }
    val split = query.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    if (split.size == 2) {
      val prdNorm = split[0].lpad(6, "0")
      val prdno = prdNorm.lpad(16, " ")
      val grade = split[1]
      val produtoOpt = findProduto(prdno, grade)
      if (produtoOpt != null) {
        val lista = ArrayList<Produto>()
        lista.add(produtoOpt)
        return lista
      }
    }

    return emptyList()
  }

  fun findProduto(barra: String): List<Produto> {
    return Produto.dataProvider.and { Produto::codbar eq barra }.getAll()
  }

  private fun findProdutoPrdno(prdno: String): List<Produto> {
    return Produto.dataProvider.and { Produto::prdno eq prdno }.getAll()
  }

  private fun movimentacaoTransferencia(): Movimentacao {
    val chaveTransferencia = montaChaveTransferencia()
    val movChave = Movimentacao.findMovimentacao(chaveTransferencia)
    return if (movChave == null) {
      val movimentacao = Movimentacao()
      movimentacao.documento = prdno?.trim { it <= ' ' } ?: ""
      movimentacao.observacao = "Tranferencia Interna"
      movimentacao.chave = chaveTransferencia
      movimentacao.data = LocalDate.now()
      movimentacao.tipoMov = EMovTipo.SAIDA
      movimentacao.save()
      movimentacao
    }
    else movChave
  }

  fun saldoPulmaoTotal(): Double {
    return saldosPulmao().map { s -> s.saldoConfirmado?.toDouble() ?: 0.00 }.sum()
  }

  private fun zeraSaldos() {
    saldos.getAll().forEach { saldo ->
      saldo.saldoConfirmado = BigDecimal.ZERO
      saldo.saldoNConfirmado = BigDecimal.ZERO
      saldo.save()
    }
  }


  fun recalculaSaldo() {
    updateRunner("/sql/recalculaSaldo.sql".readFile(), mapOf("produto" to (id ?: 0)))
  }


  fun transferenciasPicking(): List<Transferencia> {
    val mp = movProdutoPicking()
    val mpNew = MovProduto.findById(mp.id ?: 0)
    return mpNew?.transferencias?.getAll().orEmpty()
  }

  fun enderecosComSaldo(): List<Endereco> {
    var sql = """
      select distinct e.*
from enderecos as e
  INNER JOIN saldos as s
    ON s.idEndereco = e.id
WHERE s.idProduto = $id
  and saldoConfirmado > 0
    """.trimIndent()
    return scriptRunner(Endereco::class.java, sql)
  }

  fun saldosPulmao(): List<Saldo> {
    val sql = """
select distinct s.*
  from saldos as s
    inner join enderecos as e
      on s.idEndereco = e.id
where e.tipoNivel = 'PULMAO'
  and idProduto = 5110
  and saldoConfirmado <> 0
    """.trimIndent()
    return scriptRunner(Saldo::class.java, sql)
  }

  fun saldoEm(endereco: Endereco): Saldo? {
    return Saldo.dataProvider.and { Saldo::idEndereco eq endereco.id }
            .and { Saldo::idProduto eq (id ?: 0) }
            .getAll().firstOrNull()
  }

  fun movProdutoPicking(): MovProduto {
    val movPicking = movimentacaoPicking()
    val mov = movPicking.findMovProduto(this)
    return if (mov == null) {
      val mp = MovProduto()
      mp.idMovimentacao = movPicking.id
      mp.idProduto = id
      mp.quantCan = BigDecimal.ZERO
      mp.quantMov = BigDecimal.ZERO
      mp.quantPalete = BigDecimal.ZERO
      mp.save()
      mp
    }
    else mov
  }

  private fun montaChavePicking(): String {
    return Movimentacao.montaChave("PK", (prdno ?: "").trim { it <= ' ' }.lpad(6, "0") + (grade ?: ""))
  }

  private fun montaChaveTransferencia(): String {
    return Movimentacao.montaChave("TI", prdno?.trim { it <= ' ' }.lpad(6, "0") + grade)
  }

  private fun movimentacaoPicking(): Movimentacao {
    val chavePicking = montaChavePicking()
    val mov = Movimentacao.findMovimentacao(chavePicking)
    return if (mov == null) {
      val movimentacao = Movimentacao()
      movimentacao.documento = (prdno ?: "").trim { it <= ' ' }
      movimentacao.observacao = "Pincking"
      movimentacao.chave = chavePicking
      movimentacao.data = LocalDate.now()
      movimentacao.tipoMov = EMovTipo.SAIDA
      movimentacao.save()
      movimentacao
    }
    else mov
  }

  fun movProdutoTransferencia(): MovProduto? {
    val movTransferencia = movimentacaoTransferencia()
    val mov = movTransferencia.findMovProduto(this)
    return if (mov == null) {
      val mp = MovProduto()
      mp.idMovimentacao = movTransferencia.id
      mp.idProduto = id
      mp.quantCan = BigDecimal.ZERO
      mp.quantMov = BigDecimal.ZERO
      mp.save()
      mp
    }
    else mov
  }
}

