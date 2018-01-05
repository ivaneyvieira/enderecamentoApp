package com.astrosoft.vok.views.utils

import com.astrosoft.model.util.EntityId
import org.vaadin.crudui.crud.impl.GridCrud
import org.vaadin.crudui.form.impl.form.factory.GridLayoutCrudFormFactory
import kotlin.reflect.KClass


abstract class CrudView<T : EntityId>(classe: KClass<T>) : FormView() {
  private val crud = GridCrud<T>(classe.java)

  init {
    addComponentsAndExpand(crud)

    crud.setFindAllOperation { findAll() }
    crud.setAddOperation { save(it) }
    crud.setUpdateOperation { save(it) }
    crud.setDeleteOperation { delete(it) }

    val formFactory = GridLayoutCrudFormFactory<T>(classe.java, 2, 2)
    formFactory.setUseBeanValidation(true)
    crud.crudFormFactory = formFactory
    configFormFactory(formFactory)
    configCrud(crud)

    val properties = crudFields.map { it.nome }.toTypedArray()
    formFactory.setVisibleProperties(*properties)
  }

  abstract val crudFields: List<CrudField>

  abstract fun configCrud(crud: GridCrud<T>)

  abstract fun configFormFactory(formFactory: GridLayoutCrudFormFactory<T>)

  abstract fun findAll(): List<T>
  abstract fun save(bean: T): T
  abstract fun delete(bean: T): T
}

data class CrudField(
        val nome: String,
        val descricao: String
                    )