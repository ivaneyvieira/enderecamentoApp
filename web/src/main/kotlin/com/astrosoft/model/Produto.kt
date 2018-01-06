package com.astrosoft.model

import com.astrosoft.model.util.scriptRunner
import com.astrosoft.utils.lpad
import com.astrosoft.utils.readFile
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll
import java.math.BigDecimal
import java.time.LocalDateTime

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
                  ) : Entity<Long> {
  companion object : Dao<Produto>

  @get:JsonIgnore
  val movProduto
    get() = MovProduto.dataProvider.and { MovProduto::idProduto eq id }

  @get:JsonIgnore
  val saldos
    get() = Saldo.dataProvider.and { Saldo::idProduto eq id }

  fun findProduto(prdno: String, grade: String?): Produto? {
    return Produto.dataProvider
            .and { Produto::prdno eq prdno }
            .and { Produto::grade eq grade }
            .getAll().firstOrNull()
  }

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
    Produto.dataProvider.and { Produto::codbar eq barra }.getAll()
  }

  private fun findProdutoPrdno(prdno: String): List<Produto> {
    return Produto.dataProvider.and { Produto::prdno eq prdno }.getAll()
  }

  fun quantidadePalete(prdno: String): BigDecimal? {
    return scriptRunner(QuantProduto::class.java, "/sql/produtosPalete.sql".readFile())
            .firstOrNull()?.quantMov
  }

  private fun movimentacaoTransferencia(): Movimentacao {
    val chaveTransferencia = montaChaveTransferencia()
    val movChave = MovimentacaoService.findMovimentacao(chaveTransferencia)
    return if (movChave == null) {
      val movimentacao = Movimentacao()
      movimentacao.documento = bean.prdno.trim { it <= ' ' }
      movimentacao.observacao = "Tranferencia Interna"
      movimentacao.chave = chaveTransferencia
      movimentacao.data = LocalDate.now()
      movimentacao.tipoMov = br.com.astrosoft.model.enderecamento.entityEnum.EMovTipo.SAIDA
      MovimentacaoService.save(movimentacao)
    }
    else movChave
  }

  fun saldoPulmaoTotal(bean: Produto): Double {
    return saldosPulmao(bean).map { s -> s.saldoConfirmado.toDouble() }.sum()
  }

  private fun zeraSaldos(bean: Produto) {
    val s = QSaldo.saldo
    execute { q ->
      q.update(s)
              .where(s.idProduto.eq(bean.id))
              .set(s.saldoConfirmado, BigDecimal.ZERO)
              .set(s.saldoNConfirmado, BigDecimal.ZERO)
    }
  }

  fun saldos(bean: Produto): List<Saldo> {
    val s = QSaldo.saldo
    return fetch { q -> q.selectFrom(s).where(s.idProduto.eq(bean.id)) }
  }

  fun recalculaSaldo(bean: Produto) {
    zeraSaldos(bean)
    val saldos: List<SaldoRecalculado> = nativeQuery("/sql/recalculaSaldo.sql",
                                                     ::SaldoRecalculado,
                                                     Parameter.set("idProduto", bean.id))
    saldos.forEach { saldo -> saldo.salva() }
  }

  fun transferencias(bean: Produto): List<Transferencia> {
    val t = QTransferencia.transferencia
    val m = QMovProduto.movProduto
    return fetch { query ->
      query.select(t).from(t).innerJoin(m).on(m.idMovimentacao.eq(t.id)).where(m.idProduto.eq(bean.id))
    }
  }

  fun transferenciasPicking(bean: Produto): List<Transferencia> {
    val mp = movProdutoPicking(bean)
    val mpNew = MovProdutoService.refreshNew(mp)
    return MovProdutoService.transferencias(mpNew)
  }

  fun enderecosComSaldo(bean: Produto): List<Endereco> {
    val s = QSaldo.saldo
    val e = QEndereco.endereco
    return fetch { query ->
      query.select(e).from(s).innerJoin(e).on(s.idEndereco.eq(e.id)).where(s.idProduto.eq(bean.id).and(s.saldoConfirmado.gt(
              0))).distinct()
    }
  }

  fun enderecos(produto: Produto): List<Endereco> {
    val s = QSaldo.saldo
    val e = QEndereco.endereco
    return fetch { query ->
      query.select(e).from(s).innerJoin(e).on(s.idEndereco.eq(e.id)).where(s.idProduto.eq(produto.id)).distinct()
    }
  }

  fun saldosPulmao(produto: Produto): List<Saldo> {
    val s = QSaldo.saldo
    val e = QEndereco.endereco
    val a = QApto.apto
    val n = QNivel.nivel
    return fetch { query ->
      query.select(s).from(s).innerJoin(e).on(s.idEndereco.eq(e.id)).innerJoin(a).on(a.idEndereco.eq(e.id)).innerJoin(n).on(
              a.idNivel.eq(n.id)).where(n.tipoNivel.eq(br.com.astrosoft.model.enderecamento.entityEnum.ETipoNivel.PULMAO).and(
              s.idProduto.eq(produto.id)).and(s.saldoConfirmado.ne(
              BigDecimal.ZERO)))
    }
  }

  fun saldoEm(bean: Produto, endereco: Endereco): Saldo? {
    val s = QSaldo.saldo
    return fetchOne { q -> q.selectFrom(s).where(s.idProduto.eq(bean.id).and(s.idEndereco.eq(endereco.id))) }
  }

  fun movProdutos(bean: Produto): List<MovProduto> {
    val m = QMovProduto.movProduto
    return fetch { query -> query.selectFrom(m).where(m.idProduto.eq(bean.id)) }
  }

  fun movProdutoPicking(bean: Produto): MovProduto {
    val movPicking = movimentacaoPicking(bean)
    val mov = MovimentacaoService.findMovProduto(movPicking, bean)
    return if (mov == null) {
      val mp = MovProduto()
      mp.idMovimentacao = movPicking.id
      mp.idProduto = bean.id
      mp.quantCan = BigDecimal.ZERO
      mp.quantMov = BigDecimal.ZERO
      mp.quantPalete = BigDecimal.ZERO
      MovProdutoService.save(mp)
    }
    else mov
  }

  private fun montaChavePicking(bean: Produto): String {
    return MovimentacaoService.montaChave("PK", bean.prdno.trim { it <= ' ' }.lpad(6, "0") + bean.grade)
  }

  private fun montaChaveTransferencia(): String {
    return MovimentacaoService.montaChave("TI", prdno?.trim { it <= ' ' }.lpad(6, "0") + grade)
  }

  private fun movimentacaoPicking(bean: Produto): Movimentacao {
    val chavePicking = montaChavePicking(bean)
    val mov = MovimentacaoService.findMovimentacao(chavePicking)
    return if (mov == null) {
      val movimentacao = Movimentacao()
      movimentacao.documento = bean.prdno.trim { it <= ' ' }
      movimentacao.observacao = "Pincking"
      movimentacao.chave = chavePicking
      movimentacao.data = LocalDate.now()
      movimentacao.tipoMov = br.com.astrosoft.model.enderecamento.entityEnum.EMovTipo.SAIDA
      MovimentacaoService.save(movimentacao)
    }
    else mov
  }

  fun movProdutoTransferencia(bean: Produto): MovProduto? {
    val movTransferencia = movimentacaoTransferencia(bean)
    val mov = MovimentacaoService.findMovProduto(movTransferencia, bean)
    return if (mov == null) {
      val mp = MovProduto()
      mp.idMovimentacao = movTransferencia.id
      mp.idProduto = bean.id
      mp.quantCan = BigDecimal.ZERO
      mp.quantMov = BigDecimal.ZERO
      MovProdutoService.save(mp)
    }
    else mov
  }
}

data class QuantProduto(
        var quantMov: BigDecimal? = null,
        var qt: Int? = null,
        var data: LocalDateTime? = null
                       )