package com.astrosoft.model

import com.astrosoft.model.enums.EPalet
import com.astrosoft.vok.ViewException
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.findById
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("transferencias")
data class Transferencia(
        override var id: Long? = null,
        var dataHoraMov: LocalDateTime? = LocalDateTime.now(),
        var observacao: String? = "",
        var quantMov: BigDecimal? = BigDecimal.ZERO,
        var confirmacao: Boolean? = false,
        var idEnderecoEnt: Long? = 0,
        var idMovProduto: Long? = 0,
        var idEnderecoSai: Long? = 0,
        var idUser: Long? = null
                        ) : Entity<Long> {
  companion object : Dao<Transferencia>

  val user
    get() = idUser?.let { User.findById(it) }

  val enderecoSai
    get() = idEnderecoSai?.let { Endereco.findById(it) }

  val enderecoEnt
    get() = idEnderecoEnt?.let { Endereco.findById(it) }

  val movProduto
    get() = idMovProduto?.let { MovProduto.findById(it) }

  fun paletE(): EPalet? {
    val apto = enderecoEnt?.apto
    return apto?.tipoPalet
  }

  fun tipoAltura(): String {
    val apto = enderecoEnt?.apto
    return apto?.tipoAltura?.toString() ?: ""
  }

  override fun delete() {
    if (confirmacao == true) throw ViewException("Não é possível remover uma transferencia confirmada")
    val mov = movProduto ?: throw ViewException("A tranferencia não possui produto")

    val produto = mov.produto ?: throw ViewException("A tranferencia não possui produto")
    super.delete()
    produto.recalculaSaldo()
  }

  fun save(palet: EPalet) {
    val isInsert = id == 0L
    super.save()
    val movProduto = movProduto ?: throw ViewException("A tranferencia não possui produto")
    val produto = movProduto.produto ?: throw ViewException("A tranferencia não possui produto")
    val enderecoE = enderecoEnt ?: throw ViewException("A tranferencia não endereco de entrada")
    if (isInsert) {
      val apto = enderecoE.apto
      apto?.let {
        apto.tipoPalet = palet
        apto.save()
      }
    }
    produto.recalculaSaldo()
  }

  fun findOrdemServicoUser(idUser: Long): List<Transferencia> {
    return dataProvider.and { Transferencia::idUser eq idUser }.getAll()
  }

  fun findOrdemServico(confirmado: Boolean?,
                       empilhador: User?,
                       rua: Rua?,
                       produto: Produto?): List<Transferencia> {
    return Transferencia.dataProvider
            .and { Transferencia::confirmacao eq confirmado or confirmado.isNull() }
            .and { Transferencia::idUser eq empilhador?.id or empilhador.isNull() }
            .getAll()
            .filter { transferencia ->

              val entS = transferencia.enderecoSai

              true
            }
  }
}