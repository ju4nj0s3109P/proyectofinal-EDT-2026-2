package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.adaptadores.IndiceReservasArbol;
import co.edu.uq.sga.adaptadores.ReservasDelIndice;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioDisponibilidadTest {

    private static final Estancia DICIEMBRE_10_AL_12 =
            new Estancia(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));

    private final IndiceReservasArbol indice = new IndiceReservasArbol();
    private final ReservasDelIndice reservasDelIndice = new ReservasDelIndice(indice);
    private final ServicioReservas reservas = new ServicioReservas(indice);
    private final ServicioDisponibilidad servicio =
            new ServicioDisponibilidad(reservasDelIndice);

    @Test
    void ofreceSoloLosLibresQueAlojanAlGrupoEnElOrdenPedido() {
        Apartamento barato = apartamento("A-101", 2, 1, "80");
        Apartamento grande = apartamento("A-102", 6, 3, "150");
        Apartamento caro = apartamento("A-103", 4, 2, "200");
        Apartamento[] catalogo = {barato, grande, caro};

        ListaEnlazada<Apartamento> porPrecio = servicio.buscarDisponibles(
                catalogo, DICIEMBRE_10_AL_12, 3, ComparadoresApartamento.porPrecio());
        assertEquals(2, porPrecio.tamanio(), "el de capacidad 2 no sirve para 3 personas");
        assertEquals("A-102", porPrecio.obtener(0).getCodigo());
        assertEquals("A-103", porPrecio.obtener(1).getCodigo());

        ListaEnlazada<Apartamento> porDormitorios = servicio.buscarDisponibles(
                catalogo, DICIEMBRE_10_AL_12, 3, ComparadoresApartamento.porDormitorios());
        assertEquals("A-103", porDormitorios.obtener(0).getCodigo());

        ListaEnlazada<Apartamento> porCapacidad = servicio.buscarDisponibles(
                catalogo, DICIEMBRE_10_AL_12, 1, ComparadoresApartamento.porCapacidad());
        assertEquals("A-101", porCapacidad.obtener(0).getCodigo());
        assertEquals(3, porCapacidad.tamanio());
    }

    @Test
    void dejaDeOfrecerLoReservadoYLoQueEstaFueraDeServicio() {
        Apartamento ocupado = apartamento("A-201", 4, 2, "100");
        Apartamento averiado = apartamento("A-202", 4, 2, "100");
        Apartamento libre = apartamento("A-203", 4, 2, "100");
        reservas.registrarDirecta(reserva("R-1", ocupado, DICIEMBRE_10_AL_12));
        averiado.marcarFueraDeServicio("reparación");

        assertFalse(servicio.estaLibre(ocupado, DICIEMBRE_10_AL_12));
        assertFalse(servicio.estaLibre(averiado, DICIEMBRE_10_AL_12));
        assertTrue(servicio.estaLibre(libre, DICIEMBRE_10_AL_12));

        // La noche de salida no se ocupa: una estancia que empieza el 12 sí cabe.
        Estancia despues = new Estancia(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 14));
        assertTrue(servicio.estaLibre(ocupado, despues));

        ListaEnlazada<Apartamento> disponibles = servicio.buscarDisponibles(
                new Apartamento[] {ocupado, averiado, libre}, DICIEMBRE_10_AL_12, 2, null);
        assertEquals(1, disponibles.tamanio());
        assertEquals("A-203", disponibles.obtener(0).getCodigo());
    }

    @Test
    void ordenaPorNumeroDeReservasYValidaArgumentos() {
        Apartamento muyReservado = apartamento("A-301", 4, 2, "100");
        Apartamento sinReservas = apartamento("A-302", 4, 2, "100");
        reservas.registrarDirecta(reserva("R-2", muyReservado, DICIEMBRE_10_AL_12));

        ListaEnlazada<Apartamento> ordenados = servicio.buscarDisponibles(
                new Apartamento[] {sinReservas, muyReservado},
                new Estancia(LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 12)),
                2,
                ComparadoresApartamento.porNumeroDeReservas(reservasDelIndice));
        assertEquals("A-301", ordenados.obtener(0).getCodigo(), "el más reservado primero");

        assertThrows(IllegalArgumentException.class, () -> servicio.buscarDisponibles(
                new Apartamento[] {sinReservas}, DICIEMBRE_10_AL_12, 0, null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.buscarDisponibles(null, DICIEMBRE_10_AL_12, 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.estaLibre(sinReservas, null));
        assertThrows(IllegalArgumentException.class, () -> new ServicioDisponibilidad(null));
        assertThrows(IllegalArgumentException.class,
                () -> ComparadoresApartamento.porNumeroDeReservas(null));
    }

    @Test
    void asignaUnGrupoGrandeConElMenorNumeroDeApartamentos() {
        Apartamento dos = apartamento("A-401", 2, 1, "80");
        Apartamento tres = apartamento("A-402", 3, 2, "100");
        Apartamento cuatro = apartamento("A-403", 4, 2, "120");
        Apartamento[] catalogo = {dos, tres, cuatro};

        ListaEnlazada<Apartamento> grupoDeSiete =
                servicio.asignarGrupo(catalogo, DICIEMBRE_10_AL_12, 7);
        assertEquals(2, grupoDeSiete.tamanio(), "4 + 3 alcanza para siete personas");
        assertEquals(7, capacidad(grupoDeSiete));

        ListaEnlazada<Apartamento> grupoDeCuatro =
                servicio.asignarGrupo(catalogo, DICIEMBRE_10_AL_12, 4);
        assertEquals(1, grupoDeCuatro.tamanio(), "cabe en uno solo");
        assertEquals("A-403", grupoDeCuatro.obtener(0).getCodigo());

        ListaEnlazada<Apartamento> todos =
                servicio.asignarGrupo(catalogo, DICIEMBRE_10_AL_12, 9);
        assertEquals(3, todos.tamanio());

        assertTrue(servicio.asignarGrupo(catalogo, DICIEMBRE_10_AL_12, 10).estaVacia(),
                "el grupo no cabe ni usando todos los apartamentos");
    }

    @Test
    void noAsignaApartamentosYaReservadosAlGrupo() {
        Apartamento libre = apartamento("A-501", 4, 2, "100");
        Apartamento reservado = apartamento("A-502", 4, 2, "100");
        reservas.registrarDirecta(reserva("R-3", reservado, DICIEMBRE_10_AL_12));

        ListaEnlazada<Apartamento> grupo = servicio.asignarGrupo(
                new Apartamento[] {libre, reservado}, DICIEMBRE_10_AL_12, 6);
        assertTrue(grupo.estaVacia(), "solo queda libre un apartamento de 4 cupos");

        ListaEnlazada<Apartamento> grupoPequenio = servicio.asignarGrupo(
                new Apartamento[] {libre, reservado}, DICIEMBRE_10_AL_12, 3);
        assertEquals(1, grupoPequenio.tamanio());
        assertEquals("A-501", grupoPequenio.obtener(0).getCodigo());
    }

    private static int capacidad(ListaEnlazada<Apartamento> apartamentos) {
        int total = 0;
        for (Apartamento apartamento : apartamentos) {
            total += apartamento.getCapacidadMaxima();
        }
        return total;
    }

    private static Apartamento apartamento(
            String codigo, int capacidad, int dormitorios, String tarifaBaja) {
        return new Apartamento(codigo, "Suite " + codigo, "A", 1, dormitorios, capacidad,
                new String[0], new BigDecimal(tarifaBaja), new BigDecimal("300"),
                new BigDecimal("400"));
    }

    private static Reserva reserva(String folio, Apartamento apartamento, Estancia estancia) {
        Huesped titular = new Huesped("Titular", "109" + folio, LocalDate.of(1990, 1, 1),
                "3001234567", "titular@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Titular", LocalDate.of(1990, 1, 1))};
        return new Reserva(folio, titular, ocupantes, estancia, CanalReserva.DIRECTA,
                new AsignacionApartamento[] {new AsignacionApartamento(apartamento)},
                LocalDateTime.of(2026, 11, 1, 9, 0));
    }
}
