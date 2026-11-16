package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.PilaDeshacerFolios;
import co.edu.uq.sga.dominio.contratos.RepositorioFolios;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Abre el folio de cada reserva, registra cargos y pagos y deshace sin borrar movimientos. */
public class ServicioFolios {
    private final RepositorioFolios repositorio;
    private final PilaDeshacerFolios deshacer;

    public ServicioFolios(RepositorioFolios repositorio, PilaDeshacerFolios deshacer) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio de folios es obligatorio.");
        }
        if (deshacer == null) {
            throw new IllegalArgumentException("La pila para deshacer es obligatoria.");
        }
        this.repositorio = repositorio;
        this.deshacer = deshacer;
    }

    /** Abre el folio de la reserva con el mismo código de la reserva. */
    public Folio abrir(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        Folio folio = new Folio(reserva.getFolio());
        if (!repositorio.agregarSiNoExiste(folio)) {
            throw new ExcepcionDominio("La reserva ya tiene un folio abierto.");
        }
        return folio;
    }

    /** Devuelve el folio con ese código o lanza una excepción de dominio si no existe. */
    public Folio obtener(String codigoFolio) {
        if (codigoFolio == null || codigoFolio.isBlank()) {
            throw new IllegalArgumentException("El código del folio es obligatorio.");
        }
        String codigo = codigoFolio.trim();
        return repositorio.buscar(codigo)
                .orElseThrow(() -> new ExcepcionDominio("No existe el folio " + codigo + "."));
    }

    /** Registra un cargo; el saldo pendiente del huésped aumenta. */
    public MovimientoFolio registrarCargo(
            String codigoFolio, BigDecimal monto, String descripcion, LocalDateTime fechaHora) {
        return registrar(codigoFolio, TipoMovimiento.CARGO, monto, descripcion, fechaHora);
    }

    /** Registra un pago; el saldo pendiente disminuye y puede quedar a favor del huésped. */
    public MovimientoFolio registrarPago(
            String codigoFolio, BigDecimal monto, String descripcion, LocalDateTime fechaHora) {
        return registrar(codigoFolio, TipoMovimiento.PAGO, monto, descripcion, fechaHora);
    }

    /**
     * Deshace la última operación (cargo o pago) del folio registrando el movimiento contrario.
     * El movimiento original no se borra ni se modifica.
     */
    public MovimientoFolio deshacerUltima(String codigoFolio, LocalDateTime fechaHora) {
        Folio folio = obtener(codigoFolio);
        MovimientoFolio ultima = deshacer.desapilar(folio.getCodigo())
                .orElseThrow(() -> new ExcepcionDominio("No hay operaciones para deshacer."));
        TipoMovimiento contrario = ultima.getTipo() == TipoMovimiento.CARGO
                ? TipoMovimiento.REVERSO_CARGO
                : TipoMovimiento.REVERSO_PAGO;
        try {
            return agregar(folio, contrario, ultima.getMonto(),
                    "Deshace " + ultima.getCodigo(), fechaHora, ultima.getCodigo());
        } catch (RuntimeException e) {
            // Si el reverso no se pudo registrar, la operación debe seguir disponible para deshacer.
            deshacer.apilar(folio.getCodigo(), ultima);
            throw e;
        }
    }

    /**
     * Registra dinero devuelto al huésped. No puede superar su saldo a favor y,
     * como no es un cargo ni un pago, no entra en la pila de deshacer.
     */
    public MovimientoFolio registrarDevolucion(
            String codigoFolio, BigDecimal monto, String descripcion, LocalDateTime fechaHora) {
        Folio folio = obtener(codigoFolio);
        BigDecimal saldoAFavor = folio.calcularSaldo().negate();
        if (monto != null && monto.compareTo(saldoAFavor) > 0) {
            throw new ExcepcionDominio("La devolución supera el saldo a favor del huésped.");
        }
        return agregar(folio, TipoMovimiento.DEVOLUCION, monto, descripcion, fechaHora, null);
    }

    /** Dinero recibido del huésped: pagos menos pagos reversados y devoluciones. */
    public BigDecimal totalPagado(String codigoFolio) {
        return totalPagado(obtener(codigoFolio));
    }

    /**
     * Igual que {@link #totalPagado(String)} para un folio que ya se tiene en la mano.
     * Es la base del anticipo del 30 % y de la devolución al cancelar.
     */
    public static BigDecimal totalPagado(Folio folio) {
        if (folio == null) {
            throw new IllegalArgumentException("El folio es obligatorio.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (MovimientoFolio movimiento : folio.getMovimientos()) {
            switch (movimiento.getTipo()) {
                case PAGO -> total = total.add(movimiento.getMonto());
                case REVERSO_PAGO, DEVOLUCION -> total = total.subtract(movimiento.getMonto());
                default -> { }
            }
        }
        return total;
    }

    /**
     * Cierra el folio de una reserva que se cancela: reversa los cargos que siguen
     * vigentes y devuelve todo el dinero recibido. Nada se borra.
     *
     * @return el monto devuelto al huésped, cero si no había pagos
     */
    public BigDecimal cerrarPorCancelacion(String codigoFolio, LocalDateTime fechaHora) {
        Folio folio = obtener(codigoFolio);
        MovimientoFolio[] movimientos = folio.getMovimientos();
        for (MovimientoFolio movimiento : movimientos) {
            if (movimiento.getTipo() == TipoMovimiento.CARGO
                    && !fueReversado(movimiento.getCodigo(), movimientos)) {
                agregar(folio, TipoMovimiento.REVERSO_CARGO, movimiento.getMonto(),
                        "Cancelación de la reserva", fechaHora, movimiento.getCodigo());
            }
        }

        BigDecimal devuelto = totalPagado(folio);
        if (devuelto.signum() > 0) {
            agregar(folio, TipoMovimiento.DEVOLUCION, devuelto,
                    "Devolución por cancelación", fechaHora, null);
        }
        // El folio queda cerrado: deshacer operación por operación ya no tiene sentido.
        deshacer.limpiar(folio.getCodigo());
        return devuelto;
    }

    private static boolean fueReversado(String codigoMovimiento, MovimientoFolio[] movimientos) {
        for (MovimientoFolio movimiento : movimientos) {
            if (codigoMovimiento.equals(movimiento.getCodigoMovimientoReversado())) {
                return true;
            }
        }
        return false;
    }

    private MovimientoFolio registrar(String codigoFolio, TipoMovimiento tipo, BigDecimal monto,
            String descripcion, LocalDateTime fechaHora) {
        Folio folio = obtener(codigoFolio);
        MovimientoFolio movimiento = agregar(folio, tipo, monto, descripcion, fechaHora, null);
        deshacer.apilar(folio.getCodigo(), movimiento);
        return movimiento;
    }

    private static MovimientoFolio agregar(Folio folio, TipoMovimiento tipo, BigDecimal monto,
            String descripcion, LocalDateTime fechaHora, String codigoReversado) {
        MovimientoFolio movimiento = new MovimientoFolio(
                siguienteCodigo(folio), tipo, monto, descripcion, fechaHora, codigoReversado);
        folio.registrarMovimiento(movimiento);
        return movimiento;
    }

    /** Los movimientos nunca se borran, así que la cantidad actual + 1 no se repite. */
    private static String siguienteCodigo(Folio folio) {
        return folio.getCodigo() + "-M" + (folio.getMovimientos().length + 1);
    }
}
