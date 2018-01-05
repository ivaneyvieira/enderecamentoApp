package com.astrosoft.vok.views.utils

import com.astrosoft.vok.utils.BytesResource
import com.vaadin.ui.Component
import com.vaadin.ui.CustomField
import com.vaadin.ui.Panel

class FotoField(caption: String) : CustomField<ByteArray>() {
  private val panel: Panel = Panel()
  private var value: ByteArray? = null

  init {
    panel.setWidth("80px")
    panel.setHeight("100px")
    setCaption(caption)
  }

  override fun initContent(): Component {
    return panel
  }

  override fun doSetValue(byteImagem: ByteArray) {
    value = byteImagem
    val image = BytesResource.makeResource(byteImagem, caption)
    panel.icon = image
  }

  override fun getValue(): ByteArray? {
    return value
  }
}
