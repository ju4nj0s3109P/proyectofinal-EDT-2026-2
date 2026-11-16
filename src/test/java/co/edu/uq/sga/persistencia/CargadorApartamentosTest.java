package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CargadorApartamentosTest {

    @TempDir
    Path carpetaTemporal;

    private static final String ENCABEZADO =
            "codigo;nombre;bloque;piso;dormitorios;capacidad;"
                    + "caracteristicas;tarifaBaja;tarifaMedia;tarifaAlta;"
                    + "estado;motivo\n";

    @Test
    void cargaApartamentosYPermiteBuscarlosPorCodigo() throws IOException {
        Path archivo = crearCsv(
                "A102;Apartamento 102;A;1;1;2;Wifi;55000;75000;95000;"
                        + "FUERA_DE_SERVICIO;Mantenimiento\n"
                        + "A101;Apartamento 101;A;1;2;4;Wifi|Cocina;"
                        + "65000;85000;110000;PREPARADO;\n"
        );

        var catalogo = new CargadorApartamentos().cargar(archivo);

        assertEquals(2, catalogo.cantidad());

        var apartamento = catalogo.buscarPorCodigo("A101").orElseThrow();

        assertEquals("Apartamento 101", apartamento.getNombre());
        assertEquals(4, apartamento.getCapacidadMaxima());
        assertEquals(new BigDecimal("65000"), apartamento.getTarifaBaja());
        assertEquals(EstadoApartamento.PREPARADO,
                apartamento.getEstadoOperativo());

        assertArrayEquals(
                new String[]{"Wifi", "Cocina"},
                apartamento.getCaracteristicas()
        );

        assertTrue(catalogo.buscarPorCodigo("NO-EXISTE").isEmpty());

        var listado = catalogo.listarPorCodigo();

        assertEquals("A101", listado[0].getCodigo());
        assertEquals("A102", listado[1].getCodigo());

        var fueraDeServicio =
                catalogo.buscarPorCodigo("A102").orElseThrow();

        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO,
                fueraDeServicio.getEstadoOperativo());
        assertEquals("Mantenimiento",
                fueraDeServicio.getMotivoFueraDeServicio());
    }

    @Test
    void rechazaCodigosDuplicados() throws IOException {
        String fila =
                "A101;Apartamento 101;A;1;2;4;Wifi;"
                        + "65000;85000;110000;PREPARADO;\n";

        Path archivo = crearCsv(fila + fila);

        IOException error = assertThrows(
                IOException.class,
                () -> new CargadorApartamentos().cargar(archivo)
        );

        assertTrue(error.getMessage().contains("A101"));
        assertTrue(error.getMessage().contains("registro de datos 2"));
    }

    @Test
    void rechazaFueraDeServicioSinMotivo() throws IOException {
        Path archivo = crearCsv(
                "A101;Apartamento 101;A;1;2;4;Wifi;"
                        + "65000;85000;110000;FUERA_DE_SERVICIO;\n"
        );

        assertThrows(
                IOException.class,
                () -> new CargadorApartamentos().cargar(archivo)
        );
    }

    @Test
    void rechazaCapacidadInvalida() throws IOException {
        Path archivo = crearCsv(
                "A101;Apartamento 101;A;1;2;0;Wifi;"
                        + "65000;85000;110000;PREPARADO;\n"
        );

        assertThrows(
                IOException.class,
                () -> new CargadorApartamentos().cargar(archivo)
        );
    }

    @Test
    void admiteSeparadoresComillasYSaltosDentroDeUnCampo()
            throws IOException {

        Path archivo = crearCsv(
                "A101;\"Apartamento \"\"Mirador\"\"; norte\n"
                        + "con balcón\";A;1;2;4;Wifi;"
                        + "65000;85000;110000;PREPARADO;\n"
        );

        var catalogo = new CargadorApartamentos().cargar(archivo);
        var apartamento = catalogo.buscarPorCodigo("A101").orElseThrow();

        assertEquals(
                "Apartamento \"Mirador\"; norte\ncon balcón",
                apartamento.getNombre()
        );
    }

    @Test
    void rechazaComillasSinCerrar() throws IOException {
        Path archivo = crearCsv(
                "A101;\"Apartamento sin cierre;A;1;2;4;Wifi;"
                        + "65000;85000;110000;PREPARADO;"
        );

        assertThrows(
                IOException.class,
                () -> new CargadorApartamentos().cargar(archivo)
        );
    }

    @Test
    void rechazaEncabezadoIncorrecto() throws IOException {
        Path archivo = carpetaTemporal.resolve("encabezado-invalido.csv");

        Files.writeString(
                archivo,
                ENCABEZADO.replace("codigo;", "identificador;"),
                StandardCharsets.UTF_8
        );

        assertThrows(
                IOException.class,
                () -> new CargadorApartamentos().cargar(archivo)
        );
    }

    @Test
    void rechazaArchivoSinApartamentos() throws IOException {
        Path archivo = crearCsv("");

        assertThrows(
                IOException.class,
                () -> new CargadorApartamentos().cargar(archivo)
        );
    }

    private Path crearCsv(String contenido) throws IOException {
        Path archivo = carpetaTemporal.resolve("apartamentos.csv");

        Files.writeString(
                archivo,
                ENCABEZADO + contenido,
                StandardCharsets.UTF_8
        );

        return archivo;
    }
}