package com.coworkhub.presentacion.dto;

import java.math.BigDecimal;

public record ResumenConsumo(Long miembroId, String miembro, String plan, String mes,
                             BigDecimal horasIncluidas, BigDecimal horasUsadas,
                             BigDecimal horasIncluidasUsadas, BigDecimal horasRestantes,
                             BigDecimal horasExcedentes) {
}