package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;
import co.edu.uq.sga.estructuras.lineales.Conjunto;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Objects;

/**
 * Mantiene favoritos e historial por documento de huésped usando exclusivamente
 * las estructuras propias del proyecto.
 */
public final class ServicioPerfilesHuesped {
    private final DiccionarioArbol<String, Conjunto<Apartamento>> favoritos =
            new DiccionarioArbol<>();
    private final DiccionarioArbol<String, ListaEnlazada<Reserva>> historial =
            new DiccionarioArbol<>();

    /** Agrega un apartamento favorito; compara por código para evitar duplicados lógicos. */
    public boolean agregarFavorito(Huesped huesped, Apartamento apartamento) {
        validarPerfil(huesped);
        Objects.requireNonNull(apartamento, "El apartamento es obligatorio.");
        Conjunto<Apartamento> conjunto = favoritos.obtenerOCrear(
                huesped.getDocumento(), Conjunto::new);
        for (Apartamento existente : conjunto) {
            if (existente.getCodigo().equals(apartamento.getCodigo())) {
                return false;
            }
        }
        return conjunto.agregar(apartamento);
    }

    public boolean quitarFavorito(Huesped huesped, String codigoApartamento) {
        validarPerfil(huesped);
        if (codigoApartamento == null || codigoApartamento.isBlank()) {
            throw new IllegalArgumentException("El código del apartamento es obligatorio.");
        }
        Conjunto<Apartamento> conjunto = favoritos.obtener(huesped.getDocumento()).orElse(null);
        if (conjunto == null) {
            return false;
        }
        for (Apartamento apartamento : conjunto) {
            if (apartamento.getCodigo().equals(codigoApartamento.trim())) {
                return conjunto.eliminar(apartamento);
            }
        }
        return false;
    }

    public ListaEnlazada<Apartamento> consultarFavoritos(Huesped huesped) {
        validarPerfil(huesped);
        return favoritos.obtener(huesped.getDocumento())
                .map(Conjunto::aLista).orElseGet(ListaEnlazada::new);
    }

    /**
     * Registra una estancia finalizada. Se conserva la reserva para que el historial
     * incluya fechas, apartamentos, ocupantes y folio, no solo el rango de fechas.
     */
    public boolean registrarEstanciaFinalizada(Reserva reserva) {
        Objects.requireNonNull(reserva, "La reserva es obligatoria.");
        if (reserva.getEstado() != EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("Solo las reservas finalizadas ingresan al historial.");
        }
        String documento = reserva.getTitular().getDocumento();
        ListaEnlazada<Reserva> estancias = historial.obtenerOCrear(
                documento, ListaEnlazada::new);
        for (Reserva existente : estancias) {
            if (existente.getFolio().equals(reserva.getFolio())) {
                return false;
            }
        }
        estancias.agregarAlFinal(reserva);
        return true;
    }

    public ListaEnlazada<Reserva> consultarHistorial(Huesped huesped) {
        validarPerfil(huesped);
        return historial.obtener(huesped.getDocumento())
                .map(ListaEnlazada::copiar).orElseGet(ListaEnlazada::new);
    }

    private static void validarPerfil(Huesped huesped) {
        Objects.requireNonNull(huesped, "El huésped es obligatorio.");
    }
}
