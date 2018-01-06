package com.astrosoft.model

import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.findById
import java.math.BigDecimal

@Table("saldos")
data class Saldo(
        override var id: Long? = null,
        var capacidade: BigDecimal? = BigDecimal.ZERO,
        var saldoConfirmado: BigDecimal? = BigDecimal.ZERO,
        var saldoNConfirmado: BigDecimal? = BigDecimal.ZERO,
        var idEndereco: Long? = 0,
        var idProduto: Long? = 0
                ) : Entity<Long> {
  companion object : Dao<Saldo>

  val endereco
    get() = idEndereco?.let { Endereco.findById(it) }

  val produto
    get() = idProduto?.let { Produto.findById(it) }

  fun endereco(bean: Saldo): Endereco? {
    return EnderecoService.findById(bean.idEndereco)
  }

  fun produto(bean: Saldo): Produto {
    return ProdutoService.findById(bean.idProduto) ?: throw BancoDadosException("Produto não encontrado", bean)
  }

  fun savePicking(bean: Saldo, enderecoPiking: Endereco, quantidade: BigDecimal = BigDecimal.ZERO) {
    val mp = ProdutoService.movProdutoPicking(produto(bean))
    val end = endereco(bean)
    val transferencias = MovProdutoService.transferencias(mp).filter { t ->
      TransferenciaService.enderecoS(t)?.equals(end) == true
    }
    if(transferencias.isNotEmpty()) {
      val transferencia = transferencias[0]
      transferencia.quantMov = transferencia.quantMov.add(quantidade)
      TransferenciaService.save(transferencia)
    }
    else {
      val transferencia = Transferencia(idMovProduto = mp.id, idEnderecoEnt = enderecoPiking.id,
                                        idEnderecoSai = endereco(bean)?.id ?: 0, quantMov = quantidade, observacao = "",
                                        confirmacao = false)
      TransferenciaService.save(transferencia)
    }
    mp.produto?.let { ProdutoService.recalculaSaldo(it) }
  }

  fun findSaldos(tipoNivel: ETipoNivel?,
                 rua: String?,
                 lado: ELado?,
                 predio: String?,
                 nivel: String?,
                 apto: String?): List<Saldo> {
    val s = QSaldo.saldo
    val e = QEndereco.endereco
    val a = QApto.apto
    val n = QNivel.nivel
    val p = QPredio.predio
    val r = QRua.rua
    var predicado = s.saldoConfirmado.gt(0).or(s.saldoNConfirmado.gt(0))
    if(tipoNivel != null) predicado = predicado.and(n.tipoNivel.eq(tipoNivel))
    if(rua != null) predicado = predicado.and(r.numero.eq(rua))
    if(lado != null) predicado = predicado.and(p.lado.eq(lado))
    if(predio != null) predicado = predicado.and(p.numero.eq(predio))
    if(nivel != null) predicado = predicado.and(n.numero.eq(nivel))
    if(apto != null) predicado = predicado.and(a.numero.eq(apto))
    val predicadoWhere = predicado
    return fetch { q ->
      q.select(s).from(s).innerJoin(e).on(s.idEndereco.eq(e.id)).innerJoin(a).on(a.idEndereco.eq(e.id)).innerJoin(n).on(
              a.idNivel.eq(n.id)).innerJoin(p).on(n.idPredio.eq(p.id)).innerJoin(r).on(p.idRua.eq(r.id)).where(
              predicadoWhere).orderBy(e.tipoNivel.asc(), e.localizacao.asc())
    }
  }
}