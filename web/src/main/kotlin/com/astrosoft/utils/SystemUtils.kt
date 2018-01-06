package com.astrosoft.utils

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Method
import org.imgscalr.Scalr.Mode
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import javax.imageio.ImageIO

object SystemUtils {
  private val enviroment = System.getenv()
  fun variable(`var`: String, def: String): String {
    val envResult = enviroment[`var`]
    return if (envResult == null || envResult.trim { it <= ' ' } == "") {
      def
    } else envResult
  }

  fun resize(imagem: ByteArray?, width: Int, height: Int): ByteArray? {
    return try {
      if (imagem == null) return null
      val bImagemIn = toBufferedImage(imagem)
      val bimage = Scalr.resize(bImagemIn, Method.QUALITY, Mode.AUTOMATIC, width, height)
      toByteArray(bimage)
    } catch(e: IOException) {
      ByteArray(0)
    }
  }

  @Throws(IOException::class)
  private fun toBufferedImage(imagem: ByteArray?): BufferedImage? {
    if (imagem == null) return null
    val `in` = ByteArrayInputStream(imagem)
    return ImageIO.read(`in`)
  }

  @Throws(IOException::class)
  private fun toByteArray(image: BufferedImage): ByteArray? {
    val baos = ByteArrayOutputStream()
    ImageIO.write(image, "jpg", baos)
    baos.flush()
    val imageInByte = baos.toByteArray()
    baos.close()
    return imageInByte
  }

  fun md5(md5: String): String? {
    return DigestUtils.md5Hex(md5)
  }

  fun getResourceAsStream(name: String?): InputStream? {
    var nameRet = name
    nameRet = resolveName(nameRet)
    val cl = SystemUtils::class.java.classLoader ?: return ClassLoader.getSystemResourceAsStream(nameRet)
    return cl.getResourceAsStream(nameRet)
  }

  private fun resolveName(name: String?): String? {
    var nameRet = name
    if (nameRet == null) {
      return nameRet
    }
    if (!nameRet.startsWith("/")) {
      var c: Class<*> = SystemUtils::class.java
      while (c.isArray) {
        c = c.componentType
      }
      val baseName = c.name
      val index = baseName.lastIndexOf('.')
      if (index != -1) {
        nameRet = baseName.substring(0, index).replace('.', '/') + "/" + nameRet
      }
    } else {
      nameRet = nameRet.substring(1)
    }
    return nameRet
  }

  fun readFile(file: String): String? {
    val classLoader = javaClass.classLoader
    val inputStream =classLoader.getResourceAsStream(file)
    return try {
      IOUtils.toString(inputStream, "UTF-8")
    } catch(e: URISyntaxException) {
      throw RuntimeException(e)
    } catch(e: IOException) {
      throw RuntimeException(e)
    }
  }
}
