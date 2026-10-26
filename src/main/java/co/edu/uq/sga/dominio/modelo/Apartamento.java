package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import java.math.BigDecimal;

/** Modelo de un apartamento y sus datos operativos. */
public class Apartamento {
    private final String codigo;
    private final String nombre;
    private final String bloque;
    private final int piso;
    private final int numeroDormitorios;
    private final int capacidadMaxima;
    private final String[] caracteristicas;
    private final BigDecimal tarifaBaja;
    private final BigDecimal tarifaMedia;
    private final BigDecimal tarifaAlta;
    private EstadoApartamento estadoOperativo;
    private String motivoFueraDeServicio;

    public Apartamento(
            String codigo,
            String nombre,
            String bloque,
            int piso,
            int numeroDormitorios,
            int capacidadMaxima,
            String[] caracteristicas,
            BigDecimal tarifaBaja,
            BigDecimal tarifaMedia,
            BigDecimal tarifaAlta) {
        this.codigo = validarTexto(codigo, "codigo");
        this.nombre = validarTexto(nombre, "nombre");
        this.bloque = validarTexto(bloque, "bloque");

        if (piso < 0) {
            throw new IllegalArgumentException("El piso no puede ser negativo.");
        }
        if (numeroDormitorios < 1) {
            throw new IllegalArgumentException("El apartamento debe tener al menos un dormitorio.");
        }
        if (capacidadMaxima < 1) {
            throw new IllegalArgumentException("La capacidad debe ser al menos una persona.");
        }

        this.piso = piso;
        this.numeroDormitorios = numeroDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristicas = validarCaracteristicas(caracteristicas);
        this.tarifaBaja = validarTarifa(tarifaBaja, "tarifaBaja");
        this.tarifaMedia = validarTarifa(tarifaMedia, "tarifaMedia");
        this.tarifaAlta = validarTarifa(tarifaAlta, "tarifaAlta");
        this.estadoOperativo = EstadoApartamento.PREPARADO;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBloque() {
        return bloque;
    }

    public int getPiso() {
        return piso;
    }

    public int getNumeroDormitorios() {
        return numeroDormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    /** Devuelve una copia para proteger las características internas. */
    public String[] getCaracteristicas() {
        return caracteristicas.clone();
    }

    public BigDecimal getTarifaBaja() {
        return tarifaBaja;
    }

    public BigDecimal getTarifaMedia() {
        return tarifaMedia;
    }

    public BigDecimal getTarifaAlta() {
        return tarifaAlta;
    }

    public EstadoApartamento getEstadoOperativo() {
        return estadoOperativo;
    }

    public String getMotivoFueraDeServicio() {
        return motivoFueraDeServicio;
    }

    /**
     * Cambia el estado operativo. Para marcarlo fuera de servicio se debe usar
     * {@link #marcarFueraDeServicio(String)} y registrar el motivo.
     */
    public void cambiarEstadoOperativo(EstadoApartamento nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado operativo es obligatorio.");
        }
        if (nuevoEstado == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo usando marcarFueraDeServicio.");
        }

        this.estadoOperativo = nuevoEstado;
        this.motivoFueraDeServicio = null;
    }

    public void marcarFueraDeServicio(String motivo) {
        this.motivoFueraDeServicio = validarTexto(motivo, "motivoFueraDeServicio");
        this.estadoOperativo = EstadoApartamento.FUERA_DE_SERVICIO;
    }

    private static String validarTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static BigDecimal validarTarifa(BigDecimal tarifa, String nombreCampo) {
        if (tarifa == null || tarifa.signum() < 0) {
            throw new IllegalArgumentException(nombreCampo + " debe ser un valor no negativo.");
        }
        return tarifa;
    }

    private static String[] validarCaracteristicas(String[] caracteristicas) {
        if (caracteristicas == null) {
            throw new IllegalArgumentException("Las características son obligatorias.");
        }

        String[] copia = caracteristicas.clone();
        for (int i = 0; i < copia.length; i++) {
            copia[i] = validarTexto(copia[i], "caracteristica");
        }
        return copia;
    }
}
