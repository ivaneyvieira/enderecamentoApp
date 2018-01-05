package com.astrosoft.model

import com.astrosoft.model.enums.*
import com.astrosoft.model.util.EntityId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.*
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import java.math.BigDecimal
import java.time.LocalDate

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

@Table("movimentacoes")
data class Movimentacao(
        override var id: Long? = null,
        var chave: String? = "",
        var documento: String? = "",
        var data: LocalDate? = LocalDate.now(),
        var observacao: String? = "",
        var tipoMov: EMovTipo? = EMovTipo.ENTRADA
                       ) : Entity<Long> {
  companion object : Dao<Movimentacao>

  @get:JsonIgnore
  val movProduto
    get() = MovProduto.dataProvider.and { MovProduto::idMovimentacao eq id }
}

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

@Table("produtos")
data class Produto(
        override var id: Long? = null,
        var codbar: String? = "",
        var grade: String? = "",
        var nome: String? = "",
        var prdno: String? = "",
        var clno: Int? = 0,
        var vendno: Int? = 0,
        var custo: BigDecimal? = BigDecimal.ZERO,
        var preco: BigDecimal? = BigDecimal.ZERO,
        var quantVolumes: Int? = 0,
        var estoqueMinimo: BigDecimal? = BigDecimal.ZERO
                  ) : Entity<Long> {
  companion object : Dao<Produto>

  @get:JsonIgnore
  val movProduto
    get() = MovProduto.dataProvider.and { MovProduto::idProduto eq id }

  @get:JsonIgnore
  val saldos
    get() = Saldo.dataProvider.and { Saldo::idProduto eq id }
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

@Table("saldos")
data class Saldo(
        override var id: Long? = null,
        var capacidade: BigDecimal? = BigDecimal.ZERO,
        var saldoConfirmado: BigDecimal? = BigDecimal.ZERO,
        var saldoNConfirmado: BigDecimal? = BigDecimal.ZERO,
        var idEndereco: Long? = 0,
        var idProduto: Long? = 0
                ) : Entity<Long> {
  companion object : Dao<Saldo>

  val endereco
    get() = idEndereco?.let { Endereco.findById(it) }

  val produto
    get() = idProduto?.let { Produto.findById(it) }
}

@Table("roles")
data class Role(
        override var id: Long? = null,
        var name: String?
               ) : Entity<Long> {
  companion object : Dao<Role>


  @get:JsonIgnore
  val users
    get() = UserRole.dataProvider.and { UserRole::roles_id eq id }
}

@Table("users")
data class User(
        override var id: Long? = null,
        var userName: String?,
        var fotoPerfil: ByteArray = ByteArray(0),
        var chapa: String?,
        var userSaci: String?,
        var firstName: String?,
        var lastName: String?,
        var title: String?,
        var passw: String?
               ) : EntityId() {
  companion object : Dao<User>

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as User

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (id?.hashCode() ?: 0)
    return result
  }


  @get:JsonIgnore
  val roles
    get() = UserRole.dataProvider.and { UserRole::users_id eq id }

  @get:JsonIgnore
  val transferencias
    get() = Transferencia.dataProvider.and { Transferencia::idUser eq id }
}

@Table("users_roles")
data class UserRole(
        override var id: Long? = null,
        var users_id: Long? = null,
        var roles_id: Long? = null
                   ) : EntityId() {
  companion object : Dao<UserRole>

  val user: User?
    get() = User.findById(users_id ?: 0)

  val role: Role?
    get() = Role.findById(roles_id ?: 0)
}

fun <T : Any> Any?.isNull() = NativeSqlFilter<T>("$this is null", mapOf())