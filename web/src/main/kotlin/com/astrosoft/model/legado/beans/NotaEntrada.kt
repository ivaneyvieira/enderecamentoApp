package com.astrosoft.model.legado.beans

import com.astrosoft.model.Movimentacao
import com.astrosoft.model.enums.EMovTipo
import com.astrosoft.utils.toLocalDate
import com.astrosoft.vok.ViewException
import java.time.LocalDate
import java.util.*

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
    val mov = Movimentacao.findNotaEntrada(invno) ?: Movimentacao()
    mov.chave = Movimentacao.montaChaveEntrada(invno)
    mov.data = localData
    mov.documento = documento ?: ""
    mov.observacao = ""
    mov.tipoMov = EMovTipo.ENTRADA
    mov.save()
    return mov
  }
}
