package com.astrosoft.model

import com.astrosoft.model.util.EntityId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.findById
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider

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

  private var totvs = QueryTotvs()
  private var saci = QuerySaci()

  fun findUser(name: String?): User? {
    val u = QUser.user
    return fetchOne { query -> query.selectFrom(u).where(u.userName.eq(name)) }
  }

  fun update(bean: User): User {
    val foto = totvs.imagemChapa(bean.chapa)
    foto.let { imagem ->
      val fotoByte: ByteArray? = SystemUtils.resize(imagem?.imagem, 80, 100)
      bean.fotoPerfil = fotoByte?: ByteArray(0)
    }
    val senha = saci.userSenha(bean.userSaci)?.senha ?: ""
    bean.passw = senha
    bean.userSaci = bean.userName
    return super.update(bean)
  }

  fun insert(bean: User): User {
    val foto = totvs.imagemChapa(bean.chapa)
    foto.let { imagem ->
      val fotoByte = SystemUtils.resize(imagem?.imagem, 80, 100)
      bean.fotoPerfil = fotoByte?: kotlin.ByteArray(0)
    }
    bean.userSaci = bean.userName
    val senha = saci.userSenha(bean.userSaci)?.senha ?: ""
    bean.passw = senha
    return super.insert(bean)
  }

  fun findEmpilhadores(): List<User> {
    val users = findAll()
    return users.filter { it.roles.any { it.name == "Empilhador" } }
  }
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