package com.astrosoft.model.legado.beans

import br.com.astrosoft.model.enderecamento.jpaBeans.Movimentacao
import br.com.astrosoft.model.enderecamento.services.MovimentacaoService
import br.com.astrosoft.model.framework.exceptions.ViewException
import br.com.astrosoft.model.framework.utils.toLocalDate
import java.time.LocalDate
import java.util.ArrayList
import java.util.Date

class NotaEntrada {
  val invno: Int? = null
  val nfname: String? = null
  val invse: String? = null
  val data: Date? = null
  val fornecedor: String? = null
  val cnpj: String? = null
  var produtos: MutableList<ProdutoNotaEntrada> = ArrayList()
  val documento: String? = null

  fun addProdutos(produtos: List<ProdutoNotaEntrada>) {
    this.produtos.clear()
    this.produtos.addAll(produtos)
  }

  val localData: LocalDate
    get() = this.data .toLocalDate()?: LocalDate.now()

  fun saveNotaEntradaSaci(): Movimentacao {
    invno ?: throw ViewException("O número interno da nota não foi informado")
    val mov = MovimentacaoService.findNotaEntrada(invno) ?: Movimentacao()
    mov.chave = MovimentacaoService.montaChaveEntrada(invno)
    mov.data = localData
    mov.documento = documento ?: ""
    mov.observacao = ""
    mov.tipoMov = br.com.astrosoft.model.enderecamento.entityEnum.EMovTipo.ENTRADA
    return MovimentacaoService.save(mov)
  }
}
