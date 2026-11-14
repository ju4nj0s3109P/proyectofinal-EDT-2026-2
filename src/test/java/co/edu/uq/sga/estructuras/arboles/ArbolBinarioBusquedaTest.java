package co.edu.uq.sga.estructuras.arboles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ArbolBinarioBusquedaTest {

    private static ArbolBinarioBusqueda<Integer> arbolEjemplo() {
        ArbolBinarioBusqueda<Integer> arbol = new ArbolBinarioBusqueda<>();
        for (int n : new int[] {50, 30, 70, 20, 40, 60, 80}) {
            arbol.insertar(n);
        }
        return arbol;
    }

    @Test
    void recorridosRecursivos() {
        ArbolBinarioBusqueda<Integer> arbol = arbolEjemplo();
        assertEquals("[20, 30, 40, 50, 60, 70, 80]", arbol.inorden().toString());
        assertEquals("[50, 30, 20, 40, 70, 60, 80]", arbol.preorden().toString());
        assertEquals("[20, 40, 30, 60, 80, 70, 50]", arbol.postorden().toString());
    }

    @Test
    void alturaYTamanio() {
        ArbolBinarioBusqueda<Integer> arbol = new ArbolBinarioBusqueda<>();
        assertEquals(0, arbol.altura());
        arbol.insertar(1);
        assertEquals(1, arbol.altura());
        assertEquals(3, arbolEjemplo().altura());
        assertEquals(7, arbolEjemplo().tamanio());
    }

    @Test
    void noAdmiteDuplicados() {
        ArbolBinarioBusqueda<Integer> arbol = arbolEjemplo();
        assertFalse(arbol.insertar(40));
        assertEquals(7, arbol.tamanio());
    }

    @Test
    void buscaPorSondaYDevuelveElElementoGuardado() {
        ArbolBinarioBusqueda<Persona> arbol = new ArbolBinarioBusqueda<>();
        arbol.insertar(new Persona("1094", "Ana"));
        arbol.insertar(new Persona("1001", "Beto"));

        Persona encontrada = arbol.buscar(new Persona("1094", null)).orElseThrow();
        assertEquals("Ana", encontrada.nombre());
        assertTrue(arbol.buscar(new Persona("9999", null)).isEmpty());
        assertTrue(arbol.contiene(new Persona("1001", null)));
    }

    @Test
    void eliminaHojaNodoConUnHijoYNodoConDosHijos() {
        ArbolBinarioBusqueda<Integer> arbol = arbolEjemplo();
        arbol.insertar(65);

        assertTrue(arbol.eliminar(20));
        assertTrue(arbol.eliminar(60));
        assertTrue(arbol.eliminar(50));
        assertFalse(arbol.eliminar(99));

        assertEquals("[30, 40, 65, 70, 80]", arbol.inorden().toString());
        assertEquals("[65, 30, 40, 70, 80]", arbol.preorden().toString());
        assertEquals(5, arbol.tamanio());
    }

    @Test
    void mantieneElOrdenTrasMuchasOperacionesAleatorias() {
        Random azar = new Random(7);
        ArbolBinarioBusqueda<Integer> arbol = new ArbolBinarioBusqueda<>();
        ListaEnlazada<Integer> referencia = new ListaEnlazada<>();
        for (int i = 0; i < 300; i++) {
            int n = azar.nextInt(200);
            if (arbol.insertar(n)) {
                referencia.agregar(n);
            }
        }
        for (int i = 0; i < 150; i++) {
            int n = azar.nextInt(200);
            assertEquals(referencia.eliminar(n), arbol.eliminar(n));
        }
        referencia.ordenar(Comparator.naturalOrder());
        assertEquals(referencia.toString(), arbol.inorden().toString());
        assertEquals(referencia.tamanio(), arbol.tamanio());
    }

    @Test
    void iteradorRecorreEnInorden() {
        StringBuilder sb = new StringBuilder();
        for (int n : arbolEjemplo()) {
            sb.append(n).append(' ');
        }
        assertEquals("20 30 40 50 60 70 80 ", sb.toString());
    }

    @Test
    void minimoYMaximo() {
        ArbolBinarioBusqueda<Integer> arbol = arbolEjemplo();
        assertEquals(20, arbol.minimo());
        assertEquals(80, arbol.maximo());
    }

    @Test
    void arbolVacioLanzaExcepcionPropia() {
        ArbolBinarioBusqueda<Integer> arbol = new ArbolBinarioBusqueda<>();
        assertThrows(EstructuraVaciaException.class, arbol::minimo);
        assertThrows(EstructuraVaciaException.class, arbol::maximo);
        assertThrows(EstructuraVaciaException.class, () -> arbol.iterator().next());
        assertThrows(IllegalArgumentException.class, () -> arbol.insertar(null));
    }

    @Test
    void rangoSemiabiertoDevuelveSoloLosQueCaenDentro() {
        ArbolBinarioBusqueda<Integer> arbol = arbolEjemplo();
        assertEquals("[40, 50, 60]", arbol.rango(35, 65).toString());
        assertEquals("[50]", arbol.rango(50, 60).toString(), "hasta es exclusivo");
        assertTrue(arbol.rango(81, 100).estaVacia());
        assertTrue(arbol.rango(60, 40).estaVacia());
    }

    @Test
    void anteriorYSiguienteSonEstrictos() {
        ArbolBinarioBusqueda<Integer> arbol = arbolEjemplo();
        assertEquals(40, arbol.anterior(50).orElseThrow());
        assertEquals(40, arbol.anterior(45).orElseThrow());
        assertTrue(arbol.anterior(20).isEmpty());
        assertEquals(60, arbol.siguiente(50).orElseThrow());
        assertEquals(50, arbol.siguiente(45).orElseThrow());
        assertTrue(arbol.siguiente(80).isEmpty());
    }

    @Test
    void detectaSolapamientosConRangoYAnterior() {
        ArbolBinarioBusqueda<Estadia> reservas = new ArbolBinarioBusqueda<>();
        reservas.insertar(Estadia.de(1, 4));
        reservas.insertar(Estadia.de(6, 9));
        reservas.insertar(Estadia.de(12, 15));

        assertFalse(choca(reservas, Estadia.de(4, 6)), "la salida es exclusiva");
        assertFalse(choca(reservas, Estadia.de(9, 12)));
        assertTrue(choca(reservas, Estadia.de(3, 5)), "empieza dentro de [1,4)");
        assertTrue(choca(reservas, Estadia.de(5, 7)), "termina dentro de [6,9)");
        assertTrue(choca(reservas, Estadia.de(7, 8)), "queda contenida en [6,9)");
        assertTrue(choca(reservas, Estadia.de(10, 20)), "contiene a [12,15)");
        assertFalse(choca(reservas, Estadia.de(15, 20)));
    }

    private static boolean choca(ArbolBinarioBusqueda<Estadia> arbol, Estadia nueva) {
        Estadia sondaSalida = new Estadia(nueva.salida(), nueva.salida());
        if (!arbol.rango(nueva, sondaSalida).estaVacia()) {
            return true;
        }
        return arbol.anterior(nueva).map(previa -> previa.salida().isAfter(nueva.entrada())).orElse(false);
    }

    private record Persona(String documento, String nombre) implements Comparable<Persona> {
        @Override
        public int compareTo(Persona otra) {
            return documento.compareTo(otra.documento);
        }
    }

    private record Estadia(LocalDate entrada, LocalDate salida) implements Comparable<Estadia> {
        static Estadia de(int diaEntrada, int diaSalida) {
            return new Estadia(LocalDate.of(2026, 3, diaEntrada), LocalDate.of(2026, 3, diaSalida));
        }

        @Override
        public int compareTo(Estadia otra) {
            return entrada.compareTo(otra.entrada);
        }
    }
}
