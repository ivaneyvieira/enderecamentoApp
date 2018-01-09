package com.astrosoft.model

import br.com.pintos.legado.QueryTotvs
import com.astrosoft.model.legado.QuerySaci
import com.astrosoft.model.util.EntityId
import com.astrosoft.utils.SystemUtils
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.vok.framework.sql2o.Dao
import com.github.vok.framework.sql2o.Entity
import com.github.vok.framework.sql2o.Table
import com.github.vok.framework.sql2o.findById
import com.github.vok.framework.sql2o.vaadin.and
import com.github.vok.framework.sql2o.vaadin.dataProvider
import com.github.vok.framework.sql2o.vaadin.getAll

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
  companion object : Dao<User> {
    fun findUser(name: String?): User? {
      return User.dataProvider.and { User::userName eq name }
              .getAll().firstOrNull()
    }
  }

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


  override fun save() {
    val foto = totvs.imagemChapa(chapa)
    foto.let { imagem ->
      val fotoByte: ByteArray? = SystemUtils.resize(imagem?.imagem, 80, 100)
      fotoPerfil = fotoByte ?: ByteArray(0)
    }
    val senha = saci.userSenha(userSaci ?: "")?.senha ?: ""
    passw = senha
    userSaci = userName
    super.save()
  }
}

@Table("roles")
data class Role(
        override var id: Long? = null,
        var name: String?
               ) : EntityId() {
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