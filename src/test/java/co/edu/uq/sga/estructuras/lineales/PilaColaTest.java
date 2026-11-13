package co.edu.uq.sga.estructuras.lineales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import org.junit.jupiter.api.Test;

class PilaColaTest {

    @Test
    void pilaDevuelveEnOrdenInversoAlDeEntrada() {
        Pila<String> pila = new Pila<>();
        pila.apilar("cargo");
        pila.apilar("pago");
        pila.apilar("cargo extra");

        assertEquals(3, pila.tamanio());
        assertEquals("cargo extra", pila.verTope());
        assertEquals("cargo extra", pila.desapilar());
        assertEquals("pago", pila.desapilar());
        assertEquals("cargo", pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    void pilaSeRecorreDelTopeAlFondoSinVaciarse() {
        Pila<Integer> pila = new Pila<>();
        for (int i = 1; i <= 3; i++) {
            pila.apilar(i);
        }
        StringBuilder sb = new StringBuilder();
        for (int n : pila) {
            sb.append(n);
        }
        assertEquals("321", sb.toString());
        assertEquals(3, pila.tamanio());
    }

    @Test
    void pilaVaciaLanzaExcepcionPropia() {
        Pila<String> pila = new Pila<>();
        assertThrows(EstructuraVaciaException.class, pila::desapilar);
        assertThrows(EstructuraVaciaException.class, pila::verTope);
        assertThrows(IllegalArgumentException.class, () -> pila.apilar(null));
    }

    @Test
    void colaAtiendeEnOrdenDeLlegada() {
        Cola<String> cola = new Cola<>();
        cola.encolar("solicitud-1");
        cola.encolar("solicitud-2");
        cola.encolar("solicitud-3");

        assertEquals("solicitud-1", cola.verFrente());
        assertEquals("solicitud-1", cola.desencolar());
        cola.encolar("solicitud-4");
        assertEquals("solicitud-2", cola.desencolar());
        assertEquals("solicitud-3", cola.desencolar());
        assertEquals("solicitud-4", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void colaPuedeReutilizarseTrasVaciarse() {
        Cola<Integer> cola = new Cola<>();
        cola.encolar(1);
        cola.desencolar();
        cola.encolar(2);
        assertEquals(2, cola.verFrente());
        assertEquals(1, cola.tamanio());
    }

    @Test
    void colaBuscaPorCondicionYSeRecorreSinVaciarse() {
        Cola<String> cola = new Cola<>();
        cola.encolar("F-001");
        cola.encolar("F-002");

        assertTrue(cola.contiene(f -> f.equals("F-002")));
        assertFalse(cola.contiene(f -> f.equals("F-999")));

        StringBuilder sb = new StringBuilder();
        for (String f : cola) {
            sb.append(f).append(';');
        }
        assertEquals("F-001;F-002;", sb.toString());
        assertEquals(2, cola.tamanio());
    }

    @Test
    void desencolarColaVaciaLanzaExcepcionPropia() {
        Cola<String> cola = new Cola<>();
        assertThrows(EstructuraVaciaException.class, cola::desencolar);
        assertThrows(EstructuraVaciaException.class, cola::verFrente);
    }
}
