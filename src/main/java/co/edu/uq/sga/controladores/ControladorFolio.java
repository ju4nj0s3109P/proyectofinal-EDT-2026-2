package co.edu.uq.sga.controladores;

import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.servicios.ServicioPanelReservas;

import java.math.BigDecimal;
import java.util.Objects;

public final class ControladorFolio {

    public interface VistaFolio {
        void mostrar(BigDecimal saldo, MovimientoFolio[] movimientos);
        void mostrarError(String mensaje);
    }

    private final ServicioPanelReservas servicio;
    private final String codigo;
    private final VistaFolio vista;

    public ControladorFolio(
            ServicioPanelReservas servicio,
            String codigo,
            VistaFolio vista
    ) {
        this.servicio = Objects.requireNonNull(servicio);
        this.codigo = Objects.requireNonNull(codigo);
        this.vista = Objects.requireNonNull(vista);
    }

    public void actualizar() {
        try {
            var folio = servicio.consultarFolio(codigo);
            vista.mostrar(folio.calcularSaldo(), folio.getMovimientos());
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void registrar(
            boolean esCargo,
            String textoMonto,
            String descripcion
    ) {
        try {
            BigDecimal monto;

            try {
                monto = new BigDecimal(textoMonto.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Escribe el valor sin separadores de miles. Ejemplo: 65000.50"
                );
            }

            if (esCargo) {
                servicio.registrarCargo(codigo, monto, descripcion);
            } else {
                servicio.registrarPago(codigo, monto, descripcion);
            }

            actualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void deshacer() {
        try {
            servicio.deshacerMovimiento(codigo);
            actualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }
}