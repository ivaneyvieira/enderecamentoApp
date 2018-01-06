package com.astrosoft.model.legado.beans

import br.com.astrosoft.model.enderecamento.jpaBeans.Produto
import br.com.astrosoft.model.enderecamento.services.ProdutoService
import br.com.astrosoft.model.framework.utils.lpad
import com.astrosoft.model.Produto
import com.astrosoft.utils.lpad
import java.math.BigDecimal

data class ProdutoSaci(var prdno: String? = null,
                       var grade: String? = null,
                       var nome: String? = null,
                       var codbar: String? = null,
                       var clno: Int? = null,
                       var vendno: Int? = null,
                       var quantVolumes: Int? = null,
                       var estoqueMinimo: Double? = null,
                       var custo: Double? = null,
                       var preco: Double? = null) {
  private fun getPrdnoSaci(): String {
    return this.prdno.orEmpty().lpad(16, " ")
  }
  
  fun saveProdutoSaci(): Produto {
    val prdno = getPrdnoSaci().lpad(16, " ")
    val grade = grade
    val produto = ProdutoService.findProduto(prdno, grade)
    val produtoNovo = produto ?: Produto()
    val produtoUpdate = updateProduto(produtoNovo)
    return ProdutoService.save(produtoUpdate)
  }
  
  private fun updateProduto(produto: Produto): Produto {
    produto.custo = BigDecimal.valueOf(custo ?: 0.000)
    produto.preco = BigDecimal.valueOf(preco ?: 0.00)
    produto.codbar = codbar ?: ""
    produto.grade = grade ?: ""
    produto.nome = nome ?: ""
    produto.prdno = getPrdnoSaci()
    produto.clno = clno ?: 0
    produto.vendno = vendno ?: 0
    produto.quantVolumes = quantVolumes ?: 0
    produto.estoqueMinimo = BigDecimal(estoqueMinimo ?: 0.00)
    return produto
  }
}
