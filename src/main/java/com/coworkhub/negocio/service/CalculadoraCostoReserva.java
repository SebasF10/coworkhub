package com.coworkhub.negocio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.coworkhub.negocio.model.DetalleReserva;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.negocio.model.Sala;

// Bean que concentra las fórmulas de costo (RN-07 y RN-08). No accede a la base de datos:
// recibe los datos que necesita y devuelve resultados, por eso es fácil de probar.
@Component
public class CalculadoraCostoReserva {

    public record CostoSala(int minutosConsumidos, BigDecimal costoSala) {
    }

    // RN-07: los minutos cubiertos por las horas incluidas del plan no se cobran;
    // los excedentes se cobran a tarifa × (1 − descuento del plan).
    public CostoSala calcularCostoSala(Sala sala, PlanMembresia plan, long minutosReserva,
                                       long minutosYaConsumidosEnElMes) {
        long minutosIncluidos = plan.getHorasIncluidasMes() * 60L;
        long minutosDisponibles = Math.max(0, minutosIncluidos - minutosYaConsumidosEnElMes);
        long minutosCubiertos = Math.min(minutosReserva, minutosDisponibles);
        long minutosExcedentes = minutosReserva - minutosCubiertos;

        BigDecimal factorDescuento = BigDecimal.valueOf(100 - plan.getDescuentoExcedente())
                .divide(BigDecimal.valueOf(100));
        BigDecimal costo = sala.getTarifaPorHora()
                .multiply(BigDecimal.valueOf(minutosExcedentes))
                .divide(BigDecimal.valueOf(60), 10, RoundingMode.HALF_UP)
                .multiply(factorDescuento)
                .setScale(2, RoundingMode.HALF_UP);

        return new CostoSala((int) minutosReserva, costo);
    }

    // RN-07: suma cantidad × precio vigente al momento de reservar.
    public BigDecimal calcularCostoServicios(List<DetalleReserva> detalles) {
        return detalles.stream()
                .map(DetalleReserva::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // RN-08: entre 24 h y 2 h antes del inicio se cobra el 50 % del costo de la sala.
    public BigDecimal calcularCargoCancelacionTardia(BigDecimal costoSala) {
        return costoSala.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
    }
}