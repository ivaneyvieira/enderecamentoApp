package com.astrosoft.vok.views.usuarioView

import com.astrosoft.model.User
import com.astrosoft.vok.views.utils.CrudField
import com.astrosoft.vok.views.utils.CrudView
import com.astrosoft.vok.views.utils.FotoField
import com.github.vok.framework.sql2o.findAll
import com.github.vok.karibudsl.AutoView
import org.vaadin.crudui.crud.impl.GridCrud
import org.vaadin.crudui.form.impl.form.factory.GridLayoutCrudFormFactory

@AutoView
class UsuarioView : CrudView<User>(User::class) {
  override val crudFields: List<CrudField>
    get() = listOf(CrudField("fotoPerfil", "Foto"),
                   CrudField("userName", "Usuário"),
                   CrudField("chapa", "Chapa"),
                   CrudField("title", "Cargo"),
                   CrudField("firstName", "Nome"),
                   CrudField("lastName", "Sobrenome"),
                   CrudField("roles", "Grupos"))

  override fun configCrud(crud: GridCrud<User>) {
    val grid = crud.grid
    grid.removeAllColumns()
    grid.addColumn<String>({ it.chapa }).caption = "Chapa"
    grid.addColumn<String>({ it.userName }).caption = "Usuário"
    grid.addColumn<String>({ it.firstName }).caption = "Nome"
    grid.addColumn<String>({ it.lastName }).caption = "Sobrenome"
    grid.addColumn<String>({ it.title }).caption = "Cargo"
  }

  override fun configFormFactory(formFactory: GridLayoutCrudFormFactory<User>) {
    formFactory.setFieldProvider("fotoPerfil") {
      val fotoField = FotoField("Foto")
      fotoField
    }
  }

  override fun findAll(): List<User> {
    return User.findAll()
  }

  override fun save(bean: User): User {
    bean.save()
    return bean
  }

  override fun delete(bean: User): User {
    bean.delete()
    return bean
  }
}