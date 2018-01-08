package com.astrosoft.model

import com.astrosoft.model.enums.EPalet
import com.astrosoft.model.enums.EStatusEntrada
import com.astrosoft.model.enums.ETipoAltura
import com.astrosoft.model.enums.EYES_NO
import com.astrosoft.model.util.scriptRunner
import com.astrosoft.vok.ViewException
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.*
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll
import java.math.BigDecimal

@Table("movprodutos")
data class MovProduto(
        override var id: Long? = null,
        var quantCan: BigDecimal? = BigDecimal.ZERO,
        var quantMov: BigDecimal? = BigDecimal.ZERO,
        var quantPalete: BigDecimal? = BigDecimal.ZERO,
        var idMovimentacao: Long? = 0,
        var idProduto: Long? = 0
                     ) : Entity<Long> {
  companion object : Dao<MovProduto>

  val produto
    get() = idProduto?.let { Produto.findById(it) }

  val movimentacao
    get() = idMovimentacao?.let { Movimentacao.findById(it) }

  @get:JsonIgnore
  val transferencias
    get() = Transferencia.dataProvider.and { Transferencia::idMovProduto eq id }

  fun movProdutosNaoEnderecados(): List<MovProduto> {
    val sql = """
      select m.*
from movprodutos as m
  left join transferencias as t
    on m.id = t.idMovProduto
    and t.confirmacao = 'Y'
group by m.id
having  m.quantMov > IFNULL(sum(t.quantMov), 0)
    """.trimIndent()
    return scriptRunner(MovProduto::class.java, sql)
  }

  fun statusEntrada(): EStatusEntrada {
    val confirmada = quantConfirmada()
    val enderecada = quantEnderecada()
    val mov = quantMov ?: BigDecimal.ZERO
    return if (mov.compareTo(BigDecimal.ZERO) == 0) EStatusEntrada.NAO_RECEBIDA
    else if (enderecada < mov) EStatusEntrada.RECEBIDA
    else if (mov == enderecada && mov == confirmada) EStatusEntrada.CONFERIDA
    else if (enderecada == mov) EStatusEntrada.ENDERECADA
    else EStatusEntrada.INCONSISTENTE
  }

  fun quantEnderecada(): BigDecimal {
    val doubleValue = transferencias.getAll().map { t -> t.quantMov?.toDouble() ?: 0.00 }.sum()
    return BigDecimal.valueOf(doubleValue)
  }

  private fun quantConfirmada(): BigDecimal {
    val doubleValue = transferencias.getAll()
            .filter { t -> t.confirmacao == EYES_NO.Y }
            .map { t -> t.quantMov?.toDouble() ?: 0.00 }.sum()
    return BigDecimal.valueOf(doubleValue)
  }


  fun quantNaoEnderecada(): BigDecimal {
    val quant = quantMov?.toDouble() ?: 0 - quantEnderecada().toDouble()
    return BigDecimal.valueOf(quant)
  }

  fun processaEnderecamento(palet: EPalet, altura: ETipoAltura, ruas: List<Rua>) {
    val quantMov = quantMov?.toDouble() ?: 0.00 - transferencias.getAll()
            .filter { t -> t.confirmacao == EYES_NO.Y }.map { t -> t.quantMov?.toDouble() ?: 0.00 }.sum()
    val quantPalete = quantPalete
    //Enderecos onde o produto já esteve
    val enderecosProduto = produto?.enderecos?.getAll().orEmpty()
    //Enderecos livres
    val disponiveis = Endereco.disponiveis(palet, altura, ruas)
    //Intersecao entre os Enderecos do produto e enderecos disponiveis
    val enderecosProvaveis = enderecosProduto.filter { endereco ->
      disponiveis.contains(endereco)
    }
    //Estou criando uma lista onde os enderecos provaives apacrecem na frente dos endereços disponíveis
    val listaOrdenada = ArrayList<Endereco>()
    enderecosProvaveis.forEach { end ->
      if (!listaOrdenada.contains(end)) listaOrdenada.add(end)
    }
    disponiveis.forEach { end ->
      if (!listaOrdenada.contains(end)) listaOrdenada.add(end)
    }
    if (quantMov <= quantPalete.toDouble() * listaOrdenada.size) {
      deleteTransferencias(bean)
      val enderecoS = Endereco.recebimento()
      var quant = quantMov
      var index = 0
      while (quant > 0) {
        val enderecoE = listaOrdenada[index++]
        val quantEnd = if (quant > quantPalete.toDouble()) quantPalete.toDouble()
        else quant
        quant -= quantEnd
        val observacao = ""
        val confirmacao = EYES_NO.N
        val movimentacao = Transferencia(idMovProduto = bean.id,
                                         idEnderecoEnt = enderecoE.id,
                                         idEnderecoSai = enderecoS.id,
                                         quantMov = BigDecimal.valueOf(quantEnd),
                                         observacao = observacao,
                                         confirmacao = confirmacao)
        enderecoE.tipoPalet = palet
        EnderecoService.save(enderecoE)
        TransferenciaService.save(movimentacao)
      }
      bean.produto?.let { ProdutoService.recalculaSaldo(it) }
    }
    else throw ViewException("Há espaço para endereças todos os ítens")
  }

  private fun deleteTransferencias() {
    Transferencia.deleteBy { Transferencia::idMovProduto eq id }
  }

}