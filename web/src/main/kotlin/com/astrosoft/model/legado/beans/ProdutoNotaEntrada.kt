package com.astrosoft.model.legado.beans

import br.com.astrosoft.model.framework.utils.lpad
import com.astrosoft.utils.lpad
import java.math.BigDecimal

class ProdutoNotaEntrada {
  val invno: Int? = null
  val quant: Double? = null
  val prdno: String? = null
  val nomeProduto: String? = null
  val grade: String? = null
  val codbar: String? = null
  var quantRecebido = BigDecimal.ZERO
  var status = br.com.astrosoft.model.enderecamento.entityEnum.EStatusEntrada.NAO_RECEBIDA
  var quantPalete: BigDecimal? = null
  fun codigoSaci(): String {
    return this.prdno.orEmpty().lpad(16, " ")
  }
  
  val produtoGrade: String
    get() = this.prdno?.trim { it <= ' ' }.orEmpty().lpad(6, "0") + " " + this.grade
  
  fun getQuant(): BigDecimal {
    return BigDecimal.valueOf(this.quant ?: 0.00)
  }
}
