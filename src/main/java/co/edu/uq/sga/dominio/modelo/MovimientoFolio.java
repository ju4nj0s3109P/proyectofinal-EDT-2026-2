package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Movimiento inmutable del folio: cargo, pago o reversión. */
public class MovimientoFolio {
    private final String codigo;
    private final TipoMovimiento tipo;
    private final BigDecimal monto;
    private final String descripcion;
    private final LocalDateTime fechaHora;
    private final String codigoMovimientoReversado;

    public MovimientoFolio(
            String codigo,
            TipoMovimiento tipo,
            BigDecimal monto,
            String descripcion,
            LocalDateTime fechaHora,
            String codigoMovimientoReversado) {
        this.codigo = validarTexto(codigo, "codigo");
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        this.descripcion = validarTexto(descripcion, "descripcion");
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora son obligatorias.");
        }

        boolean esReversion = tipo == TipoMovimiento.REVERSO_CARGO
                || tipo == TipoMovimiento.REVERSO_PAGO;
        if (esReversion && (codigoMovimientoReversado == null
                || codigoMovimientoReversado.isBlank())) {
            throw new IllegalArgumentException(
                    "La reversión debe indicar el código del movimiento original.");
        }
        if (!esReversion && codigoMovimientoReversado != null) {
            throw new IllegalArgumentException(
                    "Un movimiento original no puede referenciar una reversión.");
        }

        this.tipo = tipo;
        this.monto = monto;
        this.fechaHora = fechaHora;
        this.codigoMovimientoReversado =
                esReversion ? codigoMovimientoReversado.trim() : null;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /** Devuelve null si este movimiento no es una reversión. */
    public String getCodigoMovimientoReversado() {
        return codigoMovimientoReversado;
    }

    private static String validarTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " es obligatorio.");
        }
        return valor.trim();
    }
}
