package com.astrosoft.vok.utils

import com.vaadin.server.StreamResource

object BytesResource {
  fun makeResource(imagem: ByteArray, nome: String): StreamResource {
    val imagesource = BytesStreamSource(imagem)
    return StreamResource(imagesource, nome + ".jpg")
  }
}
