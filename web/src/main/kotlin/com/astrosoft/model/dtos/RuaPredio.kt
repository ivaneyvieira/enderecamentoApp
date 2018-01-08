package com.astrosoft.model.dtos

import com.astrosoft.model.enums.ELado

data class RuaPredio(
        var idRua: Long? = null,
        var numeroRua: String? = null,
        var idPredio: Long? = null,
        var numeroPredio: String? = null,
        var lado: ELado? = null
                    )