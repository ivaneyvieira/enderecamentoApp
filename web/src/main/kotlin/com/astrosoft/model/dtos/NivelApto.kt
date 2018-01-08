package com.astrosoft.model.dtos

import com.astrosoft.model.enums.ELado
import com.astrosoft.model.enums.ETipoNivel
import java.math.BigDecimal

data class NivelApto(
        var idRua: Long? = null,
        var numeroRua: String? = null,
        var idPredio: Long? = null,
        var numeroPredio: String? = null,
        var lado: ELado?=null,
        var idNivel: Long? = null,
        var numeroNivel: String? = null,
        var tipoNivel: ETipoNivel?=null,
        var altura : BigDecimal? = null,
        var idApto: Long? = null,
        var numeroApto: String? = null,
        var saldoConfirmado: BigDecimal? = null,
        var saldoNConfirmado: BigDecimal? = null
                    )