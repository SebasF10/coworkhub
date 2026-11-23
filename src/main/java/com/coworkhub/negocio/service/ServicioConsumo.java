package com.coworkhub.negocio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.ResumenConsumo;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.persistence.repository.RepositorioMiembros;
import com.coworkhub.persistence.repository.RepositorioReservas;

@Service
@Transactional(readOnly = true)
public class ServicioConsumo {

    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioReservas repositorioReservas;
    private final Clock reloj;

    public ServicioConsumo(RepositorioMiembros repositorioMiembros, RepositorioReservas repositorioReservas,
                           Clock reloj) {
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioReservas = repositorioReservas;
        this.reloj = reloj;
    }

    // RF-10: resumen mensual del consumo de horas del plan. Si no se indica el mes, usa el actual.
    public ResumenConsumo resumenMensual(Long miembroId, YearMonth mes) {
        Miembro miembro = repositorioMiembros.findById(miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", miembroId));
        YearMonth mesConsultado = mes != null ? mes : YearMonth.now(reloj);

        long minutosUsados = repositorioReservas.sumarMinutosConsumidos(miembroId,
                mesConsultado.atDay(1).atStartOfDay(),
                mesConsultado.plusMonths(1).atDay(1).atStartOfDay());
        long minutosIncluidos = miembro.getPlan().getHorasIncluidasMes() * 60L;

        return new ResumenConsumo(
                miembro.getId(),
                miembro.getNombre(),
                miembro.getPlan().getNombre(),
                mesConsultado.toString(),
                horas(minutosIncluidos),
                horas(minutosUsados),
                horas(Math.min(minutosUsados, minutosIncluidos)),
                horas(Math.max(0, minutosIncluidos - minutosUsados)),
                horas(Math.max(0, minutosUsados - minutosIncluidos)));
    }

    private BigDecimal horas(long minutos) {
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}