package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.*;
import co.edu.uq.sga.estructuras.lineales.Conjunto;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Carga reservas de demostración y resuelve referencias mediante los catálogos en árbol. */
public final class CargadorReservas {
    private static final String[] ENCABEZADOS = {
        "folio", "documentoTitular", "apartamentos", "ocupantes",
        "fechaEntrada", "fechaSalida", "canal", "estado",
        "fechaHoraCreacion", "horaLlegada"
    };
    public record ReservaCargada(Reserva reserva, LocalDateTime llegadaEstimada) {}

    public ReservaCargada[] cargar(Path archivo, CatalogoApartamentosArbol apartamentos,
            CatalogoHuespedesArbol huespedes) throws IOException {
        if (apartamentos == null || huespedes == null) {
            throw new IllegalArgumentException("Los catálogos de apartamentos y huéspedes son obligatorios.");
        }
        var filas = new LectorCSV().leer(archivo);
        if (filas.estaVacia()) throw new IOException("El archivo de reservas está vacío: " + archivo);
        validarEncabezados(filas.primero());
        ReservaCargada[] temporal = new ReservaCargada[Math.max(0, filas.tamanio() - 1)];
        int cantidad = 0, registro = 0;
        Conjunto<String> folios = new Conjunto<>();
        for (String[] fila : filas) {
            registro++;
            if (registro == 1) continue;
            try {
                ReservaCargada cargada = interpretar(fila, apartamentos, huespedes);
                if (!folios.agregar(cargada.reserva().getFolio())) {
                    throw new IllegalArgumentException("El folio está duplicado.");
                }
                temporal[cantidad++] = cargada;
            } catch (RuntimeException e) {
                throw new IOException(archivo.getFileName() + ", registro de datos "
                        + (registro - 1) + ": " + e.getMessage(), e);
            }
        }
        if (cantidad == 0) throw new IOException("El CSV no contiene reservas.");
        ReservaCargada[] resultado = new ReservaCargada[cantidad];
        System.arraycopy(temporal, 0, resultado, 0, cantidad);
        return resultado;
    }

    private ReservaCargada interpretar(String[] fila, CatalogoApartamentosArbol apartamentos,
            CatalogoHuespedesArbol huespedes) {
        if (fila.length != ENCABEZADOS.length) {
            throw new IllegalArgumentException("Se esperaban " + ENCABEZADOS.length + " columnas.");
        }
        String[] c = fila.clone();
        for (int i = 0; i < c.length; i++) c[i] = c[i].trim();
        Huesped titular = huespedes.buscarPorDocumento(c[1])
                .orElseThrow(() -> new IllegalArgumentException("No existe el huésped titular " + c[1] + "."));
        String[] codigos = c[2].split("\\|", -1);
        AsignacionApartamento[] asignaciones = new AsignacionApartamento[codigos.length];
        for (int i = 0; i < codigos.length; i++) {
            String codigo = codigos[i];
            Apartamento apartamento = apartamentos.buscarPorCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException("No existe el apartamento " + codigo + "."));
            asignaciones[i] = new AsignacionApartamento(apartamento);
        }
        String[] datosOcupantes = c[3].split("\\|", -1);
        Ocupante[] ocupantes = new Ocupante[datosOcupantes.length];
        for (int i = 0; i < datosOcupantes.length; i++) {
            String[] datos = datosOcupantes[i].split(":", 2);
            if (datos.length != 2) throw new IllegalArgumentException("Cada ocupante debe usar nombre:fechaNacimiento.");
            ocupantes[i] = new Ocupante(datos[0], LocalDate.parse(datos[1]));
        }
        LocalDate entrada = LocalDate.parse(c[4]);
        LocalDate salida = LocalDate.parse(c[5]);
        CanalReserva canal = CanalReserva.valueOf(c[6].toUpperCase());
        EstadoReserva estado = EstadoReserva.valueOf(c[7].toUpperCase());
        LocalDateTime creacion = LocalDateTime.parse(c[8]);
        LocalTime horaLlegada = c[9].isEmpty() ? LocalTime.NOON : LocalTime.parse(c[9]);
        Reserva reserva = new Reserva(c[0], titular, ocupantes,
                new Estancia(entrada, salida), canal, asignaciones, creacion);
        aplicarEstado(reserva, estado);
        return new ReservaCargada(reserva, entrada.atTime(horaLlegada));
    }

    private static void aplicarEstado(Reserva reserva, EstadoReserva estado) {
        switch (estado) {
            case PENDIENTE -> { }
            case CONFIRMADA -> reserva.confirmar();
            case EN_CURSO -> { reserva.confirmar(); reserva.iniciar(); }
            case FINALIZADA -> { reserva.confirmar(); reserva.iniciar(); reserva.finalizar(); }
            case CANCELADA -> reserva.cancelar();
            case NO_SHOW -> { reserva.confirmar(); reserva.marcarNoShow(); }
            case RECHAZADA -> reserva.rechazar();
        }
    }

    private static void validarEncabezados(String[] encabezados) throws IOException {
        if (encabezados.length != ENCABEZADOS.length) {
            throw new IOException("El encabezado de reservas debe tener " + ENCABEZADOS.length + " columnas.");
        }
        for (int i = 0; i < ENCABEZADOS.length; i++) {
            if (!ENCABEZADOS[i].equals(encabezados[i].trim())) {
                throw new IOException("En la columna " + (i + 1) + " se esperaba " + ENCABEZADOS[i] + ".");
            }
        }
    }
}
