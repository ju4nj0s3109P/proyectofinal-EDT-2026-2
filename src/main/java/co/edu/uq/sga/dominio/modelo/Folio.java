package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import java.math.BigDecimal;

/** Cuenta inalterable de cargos, pagos y reversiones de una reserva. */
public class Folio {
    private final String codigo;
    private MovimientoFolio[] movimientos;

    public Folio(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del folio es obligatorio.");
        }
        this.codigo = codigo.trim();
        this.movimientos = new MovimientoFolio[0];
    }

    public String getCodigo() {
        return codigo;
    }

    public MovimientoFolio[] getMovimientos() {
        return movimientos.clone();
    }

    public void registrarMovimiento(MovimientoFolio movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento es obligatorio.");
        }
        if (buscarPorCodigo(movimiento.getCodigo()) != null) {
            throw new IllegalArgumentException("El código del movimiento ya está registrado.");
        }

        validarReversion(movimiento);
        MovimientoFolio[] nuevos = new MovimientoFolio[movimientos.length + 1];
        for (int i = 0; i < movimientos.length; i++) {
            nuevos[i] = movimientos[i];
        }
        nuevos[movimientos.length] = movimiento;
        movimientos = nuevos;
    }

    /** Saldo positivo indica un valor pendiente de pago. */
    public BigDecimal calcularSaldo() {
        BigDecimal saldo = BigDecimal.ZERO;
        for (MovimientoFolio movimiento : movimientos) {
            BigDecimal monto = movimiento.getMonto();
            switch (movimiento.getTipo()) {
                case CARGO, REVERSO_PAGO -> saldo = saldo.add(monto);
                case PAGO, REVERSO_CARGO -> saldo = saldo.subtract(monto);
            }
        }
        return saldo;
    }

    private void validarReversion(MovimientoFolio reversion) {
        TipoMovimiento tipo = reversion.getTipo();
        if (tipo != TipoMovimiento.REVERSO_CARGO && tipo != TipoMovimiento.REVERSO_PAGO) {
            return;
        }

        MovimientoFolio original = buscarPorCodigo(reversion.getCodigoMovimientoReversado());
        if (original == null) {
            throw new IllegalArgumentException(
                    "El movimiento original que se quiere revertir no existe en el folio.");
        }

        TipoMovimiento tipoOriginal = original.getTipo();
        boolean revierteCargo = tipo == TipoMovimiento.REVERSO_CARGO
                && tipoOriginal == TipoMovimiento.CARGO;
        boolean reviertePago = tipo == TipoMovimiento.REVERSO_PAGO
                && tipoOriginal == TipoMovimiento.PAGO;
        if (!revierteCargo && !reviertePago) {
            throw new IllegalArgumentException(
                    "El tipo de reversión debe corresponder al movimiento original.");
        }
        if (original.getMonto().compareTo(reversion.getMonto()) != 0) {
            throw new IllegalArgumentException(
                    "La reversión debe tener el mismo monto que el movimiento original.");
        }
        if (yaFueRevertido(original.getCodigo())) {
            throw new IllegalArgumentException("El movimiento original ya fue revertido.");
        }
    }

    private boolean yaFueRevertido(String codigoMovimiento) {
        for (MovimientoFolio movimiento : movimientos) {
            if (codigoMovimiento.equals(movimiento.getCodigoMovimientoReversado())) {
                return true;
            }
        }
        return false;
    }

    private MovimientoFolio buscarPorCodigo(String codigoMovimiento) {
        for (MovimientoFolio movimiento : movimientos) {
            if (movimiento.getCodigo().equals(codigoMovimiento)) {
                return movimiento;
            }
        }
        return null;
    }
}
