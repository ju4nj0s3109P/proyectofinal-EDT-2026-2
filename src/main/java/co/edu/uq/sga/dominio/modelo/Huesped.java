package co.edu.uq.sga.dominio.modelo;

import java.time.LocalDate;

/** Perfil personal de un huésped titular o registrado en el alojamiento. */
public class Huesped {
    private final String nombre;
    private final String documento;
    private final LocalDate fechaNacimiento;
    private final String telefono;
    private final String correo;
    private final String rutaFotografia;

    public Huesped(
            String nombre,
            String documento,
            LocalDate fechaNacimiento,
            String telefono,
            String correo,
            String rutaFotografia) {
        this.nombre = validarTexto(nombre, "nombre");
        this.documento = validarTexto(documento, "documento");

        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }

        this.fechaNacimiento = fechaNacimiento;
        this.telefono = validarTexto(telefono, "telefono");
        this.correo = validarCorreo(correo);
        this.rutaFotografia = normalizarFotografia(rutaFotografia);
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    /** Puede ser null cuando el huésped no ha registrado fotografía. */
    public String getRutaFotografia() {
        return rutaFotografia;
    }

    private static String validarTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static String validarCorreo(String correo) {
        String valor = validarTexto(correo, "correo");
        int arroba = valor.indexOf('@');
        int puntoDespuesDeArroba = valor.lastIndexOf('.');

        if (arroba <= 0
                || arroba != valor.lastIndexOf('@')
                || puntoDespuesDeArroba <= arroba + 1
                || puntoDespuesDeArroba == valor.length() - 1
                || valor.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        return valor;
    }

    private static String normalizarFotografia(String rutaFotografia) {
        if (rutaFotografia == null || rutaFotografia.isBlank()) {
            return null;
        }
        return rutaFotografia.trim();
    }
}
