package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.RepositorioFolios;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Abre el folio de cada reserva y registra sus cargos y pagos sin borrar movimientos. */
public class ServicioFolios {
    private final RepositorioFolios repositorio;

    public ServicioFolios(RepositorioFolios repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio de folios es obligatorio.");
        }
        this.repositorio = repositorio;
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

    private MovimientoFolio registrar(String codigoFolio, TipoMovimiento tipo, BigDecimal monto,
            String descripcion, LocalDateTime fechaHora) {
        Folio folio = obtener(codigoFolio);
        MovimientoFolio movimiento = new MovimientoFolio(
                siguienteCodigo(folio), tipo, monto, descripcion, fechaHora, null);
        folio.registrarMovimiento(movimiento);
        return movimiento;
    }

    /** Los movimientos nunca se borran, así que la cantidad actual + 1 no se repite. */
    private static String siguienteCodigo(Folio folio) {
        return folio.getCodigo() + "-M" + (folio.getMovimientos().length + 1);
    }
}
