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
            MovimientoFolio reverso = new MovimientoFolio(siguienteCodigo(folio), contrario,
                    ultima.getMonto(), "Deshace " + ultima.getCodigo(), fechaHora, ultima.getCodigo());
            folio.registrarMovimiento(reverso);
            return reverso;
        } catch (RuntimeException e) {
            // Si el reverso no se pudo registrar, la operación debe seguir disponible para deshacer.
            deshacer.apilar(folio.getCodigo(), ultima);
            throw e;
        }
    }

    private MovimientoFolio registrar(String codigoFolio, TipoMovimiento tipo, BigDecimal monto,
            String descripcion, LocalDateTime fechaHora) {
        Folio folio = obtener(codigoFolio);
        MovimientoFolio movimiento = new MovimientoFolio(
                siguienteCodigo(folio), tipo, monto, descripcion, fechaHora, null);
        folio.registrarMovimiento(movimiento);
        deshacer.apilar(folio.getCodigo(), movimiento);
        return movimiento;
    }

    /** Los movimientos nunca se borran, así que la cantidad actual + 1 no se repite. */
    private static String siguienteCodigo(Folio folio) {
        return folio.getCodigo() + "-M" + (folio.getMovimientos().length + 1);
    }
}
