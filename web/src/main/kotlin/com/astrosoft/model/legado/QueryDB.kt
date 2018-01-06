package com.astrosoft.model.legado

import br.com.astrosoft.model.framework.utils.SystemUtils
import org.sql2o.Query
import org.sql2o.Sql2o

open class QueryDB(driver: String, url: String, username: String, password: String) {
  private val sql2o: Sql2o

  init {
    registerDriver(driver)
    this.sql2o = Sql2o(url, username, password)
  }

  private fun registerDriver(driver: String) {
    try {
      Class.forName(driver)
    } catch (e: ClassNotFoundException) {
      throw RuntimeException(e)
    }
  }

  protected fun <T> query(file: String, lambda: (Query) -> T): T {
    val sql = SystemUtils.readFile(file)
    val con = this.sql2o.open()
    val query = con.createQuery(sql)
    val ret = lambda(query)
    con.close()
    return ret
  }
}
