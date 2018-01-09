package com.astrosoft.model

import com.astrosoft.model.enums.ELado
import com.astrosoft.model.enums.ETipoNivel
import com.astrosoft.model.enums.EYES_NO
import com.astrosoft.model.util.EntityId
import com.astrosoft.model.util.scriptRunner
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.findById
import com.github.vok.framework.sql2o.vaadin.getAll
import java.math.BigDecimal

@Table("saldos")
data class Saldo(
        override var id: Long? = null,
        var capacidade: BigDecimal? = BigDecimal.ZERO,
        var saldoConfirmado: BigDecimal? = BigDecimal.ZERO,
        var saldoNConfirmado: BigDecimal? = BigDecimal.ZERO,
        var idEndereco: Long? = 0,
        var idProduto: Long? = 0
                ) : EntityId() {
  companion object : Dao<Saldo>

  val endereco
    get() = idEndereco?.let { Endereco.findById(it) }

  val produto
    get() = idProduto?.let { Produto.findById(it) }


  fun savePicking(enderecoPiking: Endereco, quantidade: BigDecimal = BigDecimal.ZERO) {
    val mp = produto?.movProdutoPicking()
    val end = endereco
    val transferencias = mp?.transferencias?.getAll().orEmpty().filter { t ->
      t.enderecoSai?.equals(end) == true
    }
    if (transferencias.isNotEmpty()) {
      val transferencia = transferencias[0]
      val quant = transferencia.quantMov ?: BigDecimal.ZERO
      transferencia.quantMov = quant.add(quantidade)
      transferencia.save()
    }
    else {
      val transferencia = Transferencia(idMovProduto = mp?.id,
                                        idEnderecoEnt = enderecoPiking.id,
                                        idEnderecoSai = end?.id ?: 0,
                                        quantMov = quantidade,
                                        observacao = "",
                                        confirmacao = EYES_NO.N)
      transferencia.save()
    }
    mp?.produto?.let { it.recalculaSaldo() }
  }

  fun findSaldos(tipoNivel: ETipoNivel?,
                 rua: String?,
                 lado: ELado?,
                 predio: String?,
                 nivel: String?,
                 apto: String?): List<Saldo> {
    val sql = """
select distinct s.*
  from saldos as s
    inner join enderecos as e
      on s.idEndereco = e.id
    inner join aptos as a
      on a.idEndereco = e.id
    inner join niveis as n
      on n.id =- a.idNivel
    inner join predios as p
      on p.id = n.idPredio
    inner join ruas as r
      on r.id = p.idRua
where saldoConfirmado > 0
  and saldoNConfirmado > 0
  and (e.tipoNivel = '$tipoNivel' OR '${tipoNivel ?: ""}' = '')
  and (r.numero = '${rua.orEmpty()}' OR '${rua.orEmpty()}' = '')
  and (lado = '$lado' OR '${lado ?: ""}')
  and (p.numero = '${predio.orEmpty()}' OR '${predio.orEmpty()}' = '')
  and (n.numero = '${nivel.orEmpty()}' OR '${nivel.orEmpty()}' = '')
  and (a.numero = '${apto.orEmpty()}' OR '${apto.orEmpty()}' = '')
order by e.tipoNivel, e.localizacao
    """.trimIndent()
    return scriptRunner(Saldo::class.java, sql)
  }
}