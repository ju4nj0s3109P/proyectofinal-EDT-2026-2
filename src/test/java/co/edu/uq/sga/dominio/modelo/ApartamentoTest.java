package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ApartamentoTest {

    @Test
    void creaApartamentoConEstadoPreparadoYConservaSusDatos() {
        Apartamento apartamento = crearApartamento();

        assertEquals("A-101", apartamento.getCodigo());
        assertEquals("Suite familiar", apartamento.getNombre());
        assertEquals("A", apartamento.getBloque());
        assertEquals(1, apartamento.getPiso());
        assertEquals(2, apartamento.getNumeroDormitorios());
        assertEquals(4, apartamento.getCapacidadMaxima());
        assertArrayEquals(new String[] {"balcon", "cocina"}, apartamento.getCaracteristicas());
        assertEquals(new BigDecimal("100000"), apartamento.getTarifaBaja());
        assertEquals(new BigDecimal("125000"), apartamento.getTarifaMedia());
        assertEquals(new BigDecimal("150000"), apartamento.getTarifaAlta());
        assertEquals(EstadoApartamento.PREPARADO, apartamento.getEstadoOperativo());
        assertNull(apartamento.getMotivoFueraDeServicio());
    }

    @Test
    void copiaLasCaracteristicasAlCrearYAlConsultar() {
        String[] caracteristicas = {"balcon"};
        Apartamento apartamento = crearApartamento(caracteristicas);

        caracteristicas[0] = "modificada";
        String[] resultado = apartamento.getCaracteristicas();
        resultado[0] = "tambien modificada";

        assertArrayEquals(new String[] {"balcon"}, apartamento.getCaracteristicas());
    }

    @Test
    void rechazaCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                " ", "Suite", "A", 1, 1, 2, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void rechazaPisoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                "A-1", "Suite", "A", -1, 1, 2, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void rechazaCeroDormitoriosOCapacidad() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 0, 2, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 1, 0, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void rechazaCaracteristicasNulasOVacias() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 1, 2, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> crearApartamento(new String[] {" "}));
    }

    @Test
    void rechazaTarifasNulasONegativas() {
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 1, 2, new String[0],
                null, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Apartamento(
                "A-1", "Suite", "A", 1, 1, 2, new String[0],
                new BigDecimal("-1"), BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void cambiaEstadoOperativoYExigeMotivoParaFueraDeServicio() {
        Apartamento apartamento = crearApartamento();

        apartamento.cambiarEstadoOperativo(EstadoApartamento.OCUPADO);
        assertEquals(EstadoApartamento.OCUPADO, apartamento.getEstadoOperativo());

        assertThrows(
                IllegalArgumentException.class,
                () -> apartamento.cambiarEstadoOperativo(EstadoApartamento.FUERA_DE_SERVICIO));
        assertThrows(IllegalArgumentException.class, () -> apartamento.marcarFueraDeServicio(" "));

        apartamento.marcarFueraDeServicio("mantenimiento");
        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, apartamento.getEstadoOperativo());
        assertEquals("mantenimiento", apartamento.getMotivoFueraDeServicio());

        apartamento.cambiarEstadoOperativo(EstadoApartamento.PREPARADO);
        assertEquals(EstadoApartamento.PREPARADO, apartamento.getEstadoOperativo());
        assertNull(apartamento.getMotivoFueraDeServicio());
    }

    private Apartamento crearApartamento() {
        return crearApartamento(new String[] {"balcon", "cocina"});
    }

    private Apartamento crearApartamento(String[] caracteristicas) {
        return new Apartamento(
                "A-101",
                "Suite familiar",
                "A",
                1,
                2,
                4,
                caracteristicas,
                new BigDecimal("100000"),
                new BigDecimal("125000"),
                new BigDecimal("150000"));
    }
}
