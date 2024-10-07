package com.tylerkontra.chipless.http.protocol

import com.tylerkontra.chipless.model.Money
import com.tylerkontra.chipless.model.ShortCode
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ShortCodeDeserializer : Converter<String, ShortCode> {
    override fun convert(source: String): ShortCode {
        return ShortCode(source.filter { !it.isWhitespace() })
    }
}
