package com.astrosoft.model

import com.astrosoft.model.enums.*
import com.astrosoft.model.util.EntityId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.*
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import java.math.BigDecimal

@Table("aptos")
data class Apto(
        override var id: Long? = null,
        var numero: String?,
        var tipoPalet: EPalet? = EPalet.P,
        var tipoAltura: ETipoAltura? = ETipoAltura.BAIXA,
        var idNivel: Long?,
        var idEndereco: Long?
               ) : Entity<Long> {
  companion object : Dao<Apto>

  val nivel
    get() = idNivel?.let { Nivel.findById(it) }

  val endereco
    get() = idEndereco?.let { Endereco.findById(it) }
}

@Table("niveis")
data class Nivel(
        override var id: Long? = null,
        var numero: String? = "",
        var altura: BigDecimal? = BigDecimal.ZERO,
        var tipoNivel: ETipoNivel? = ETipoNivel.PULMAO,
        var idPredio: Long? = 0
                ) : Entity<Long> {
  companion object : Dao<Nivel>

  val predio
    get() = idPredio?.let { Predio.findById(it) }

  @get:JsonIgnore
  val aptos
    get() = Apto.dataProvider.and { Apto::idNivel eq id }

}

@Table("predios")
data class Predio(
        override var id: Long? = null,
        var numero: String? = "",
        var lado: ELado? = ELado.IMPAR,
        var idRua: Long? = 0
                 ) : Entity<Long> {
  companion object : Dao<Predio>

  val rua
    get() = idRua?.let { Rua.findById(it) }

  @get:JsonIgnore
  val niveis
    get() = Nivel.dataProvider.and { Nivel::idPredio eq id }
}

@Table("ruas")
data class Rua(
        override var id: Long? = null,
        var numero: String? = ""
              ) : Entity<Long> {
  companion object : Dao<Rua>

  @get:JsonIgnore
  val predios
    get() = Predio.dataProvider.and { Predio::idRua eq id }
}



fun <T : Any> Any?.isNull() = NativeSqlFilter<T>("$this is null", mapOf())