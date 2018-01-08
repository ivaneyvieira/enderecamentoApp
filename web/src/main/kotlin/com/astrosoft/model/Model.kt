package com.astrosoft.model

import com.astrosoft.model.dtos.NivelApto
import com.astrosoft.model.dtos.RuaPredio
import com.astrosoft.model.enums.ELado
import com.astrosoft.model.enums.EPalet
import com.astrosoft.model.enums.ETipoAltura
import com.astrosoft.model.enums.ETipoNivel
import com.astrosoft.model.util.scriptRunner
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
  companion object : Dao<Rua> {
    fun ruasPulmao(): List<Rua> {
      var sql = """
select distinct r.*
from ruas as r
  inner join predios as p
    on p.idRua = r.id
  inner join niveis as n
    on n.idPredio = p.id
where tipoNivel = 'PULMAO'
    """.trimIndent()
      return scriptRunner(Rua::class.java, sql)
    }
  }

  @get:JsonIgnore
  val predios
    get() = Predio.dataProvider.and { Predio::idRua eq id }

  val ruasPredioDeposito: List<RuaPredio> by lazy {
    val sql = """
select r.id as idRua, r.numero as numeroRua,
       p.id as idPredio, p.numero as numeroPredio, lado
from ruas as r
  inner join predios as p
    on p.idRua = r.id
where r.numero <> '00'
    """.trimIndent()

    scriptRunner(RuaPredio::class.java, sql)
  }


  private fun findNiveis(lado: ELado): List<Nivel> {
    val sql = """
select distinct n.*
from predios as p
  inner join niveis as n
    on p.id = n.idPredio
where lado = $lado
  and idRua = $id
    """.trimIndent()
    return scriptRunner(Nivel::class.java, sql)
  }

  fun findNivelAptos(lado: ELado): List<NivelApto> {
    val sql = """
select r.id as idRua, r.numero as numeroRua, p.id as idPredio, p.numero as numeroPredio, lado,
  n.id as idNivel, n.numero as numeroNivel, n.tipoNivel, altura, a.id as idApto, a.numero as numeroApto,
  IFNULL(SUM(saldoConfirmado), 0) as saldoConfirmado, IFNULL(SUM(saldoNConfirmado), 0) as saldoNConfirmado
from ruas as r
  inner join predios as p
    ON p.idRua = r.id
  inner join niveis as n
    ON n.idPredio = p.id
  inner join aptos as a
    ON a.idNivel = n.id
  inner join enderecos as e
    ON e.id = a.idEndereco
  left join saldos as s
    ON e.id = s.idEndereco
WHERE r.id = $id
  and lado = $lado
GROUP BY a.id
    """.trimIndent()
    return scriptRunner(NivelApto::class.java, sql)
  }
}

fun <T : Any> Any?.isNull() = NativeSqlFilter<T>("$this is null", mapOf())
