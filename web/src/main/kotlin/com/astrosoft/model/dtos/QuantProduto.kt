package com.astrosoft.model.dtos

import java.math.BigDecimal
import java.time.LocalDateTime

data class QuantProduto(
        var quantMov: BigDecimal? = null,
        var qt: Int? = null,
        var data: LocalDateTime? = null
                       )