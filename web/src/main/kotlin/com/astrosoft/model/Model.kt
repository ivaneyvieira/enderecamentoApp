package com.astrosoft.model

import com.astrosoft.model.dtos.RuaPredio
import com.astrosoft.model.enums.*
import com.astrosoft.model.util.EntityId
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
  companion object : Dao<Rua>

  @get:JsonIgnore
  val predios
    get() = Predio.dataProvider.and { Predio::idRua eq id }

  val ruasPredioDeposito: List<RuaPredio> by lazy {
    val sql ="""
select r.id as idRua, r.numero as numeroRua,
       p.id as idPredio, p.numero as numeroPredio, lado
from ruas as r
  inner join predios as p
    on p.idRua = r.id
where r.numero <> '00'
    """.trimIndent()

    scriptRunner(RuaPredio::class.java, sql)
  }

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

  fun findNivelAptos(bean: Rua, lado: ELado): List<NivelApto> {
    val r = QRua.rua
    val p = QPredio.predio
    val n = QNivel.nivel
    val a = QApto.apto
    val e = QEndereco.endereco
    val s = QSaldo.saldo

    val saldoConfirmado = CaseBuilder().
            `when`(s.saldoConfirmado.gt(BigDecimal.ZERO)).then(s.saldoConfirmado).
            otherwise(BigDecimal.ZERO).sum()
    val saldoNConfirmado = CaseBuilder().
            `when`(s.saldoNConfirmado.gt(BigDecimal.ZERO)).then(s.saldoNConfirmado).
            otherwise(BigDecimal.ZERO).sum()

    return fetch { q ->
      q.select(n, a, saldoNConfirmado, saldoConfirmado).
              from(r).
              innerJoin(p).on(p.idRua.eq(r.id)).
              innerJoin(n).on(n.idPredio.eq(p.id)).
              innerJoin(a).on(a.idNivel.eq(n.id)).
              innerJoin(e).on(a.idEndereco.eq(e.id)).
              leftJoin(s).on(s.idEndereco.eq(e.id)).
              where(r.eq(bean).and(p.lado.eq(lado))).
              groupBy(a)
    }.mapNotNull { t ->
      val nivel = t.get(n) ?: return@mapNotNull null
      val apto = t.get(a) ?: return@mapNotNull null
      val saldoCon = t.get(saldoConfirmado) ?: return@mapNotNull null
      val saldoNCon = t.get(saldoNConfirmado) ?: return@mapNotNull null
      NivelApto(nivel, apto, saldoNCon, saldoCon)
    }
  }
}

fun <T : Any> Any?.isNull() = NativeSqlFilter<T>("$this is null", mapOf())
