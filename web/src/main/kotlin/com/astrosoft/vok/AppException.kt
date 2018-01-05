package com.astrosoft.vok

import br.com.astrosoft.model.framework.jpaBeans.EntityJPA
import java.sql.SQLException

fun causaSqlException(causa: Throwable?): SQLException? {
  if(causa == null) return null
  var exception = causa
  var cont = 0
  while(exception !is SQLException || cont > 5) {
    exception = exception?.cause
    cont += 1
  }
  return exception
}

open class AppException(causa: Throwable?, val menssagem: String) : Exception(causa) {
  val causaSqlException = causaSqlException(causa)

}

open class DevException(causa: Throwable?, menssagem: String) : AppException(causa, menssagem)

open class UserException(causa: Throwable?, menssagem: String) : AppException(causa, menssagem)

class TimeOutException(causa: Throwable?) : UserException(causa, "O banco de dados está demorando para responder")

class DeleteException(causa: Throwable?) : UserException(causa, "Não foi possível apagar o registro")

class InsertException(causa: Throwable?) : UserException(causa, "Não foi possível adicionar o registro")

class UpdateException(causa: Throwable?) : UserException(causa, "Não foi possível atualizar o registro")

class ViewException(menssagem: String) : UserException(null, menssagem)

class BancoDadosException(menssagem: String, val entityJPA: EntityJPA) : DevException(null, menssagem)


