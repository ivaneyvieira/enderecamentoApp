package com.astrosoft.vok.rests

import com.astrosoft.model.Endereco
import com.github.vok.framework.sql2o.findById
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/enderecos")
class EnderecoRest {
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  fun get(@PathParam("id") id: Long): Endereco = Endereco.findById(id) ?: throw NotFoundException("No article with id $id")

}