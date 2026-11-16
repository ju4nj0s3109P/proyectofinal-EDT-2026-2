package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.RolEmpleado;

/** Personal registrado por el administrador del alojamiento. */
public class Empleado implements Comparable<Empleado> {
    private final String documento;
    private final String nombre;
    private final String telefono;
    private final String correo;
    private final RolEmpleado rol;
    private boolean activo;

    public Empleado(String documento, String nombre, String telefono,
            String correo, RolEmpleado rol) {
        this.documento = validarTexto(documento, "documento");
        this.nombre = validarTexto(nombre, "nombre");
        this.telefono = validarTexto(telefono, "teléfono");
        this.correo = validarCorreo(correo);
        if (rol == null) {
            throw new IllegalArgumentException("El rol del empleado es obligatorio.");
        }
        this.rol = rol;
        this.activo = true;
    }

    @Override
    public int compareTo(Empleado otro) {
        if (otro == null) {
            throw new IllegalArgumentException("El empleado a comparar es obligatorio.");
        }
        return documento.compareTo(otro.documento);
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public RolEmpleado getRol() { return rol; }
    public boolean estaActivo() { return activo; }

    public void activar() { activo = true; }
    public void desactivar() { activo = false; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static String validarCorreo(String valor) {
        String correo = validarTexto(valor, "correo");
        int arroba = correo.indexOf('@');
        int punto = correo.lastIndexOf('.');
        if (arroba <= 0 || arroba != correo.lastIndexOf('@')
                || punto <= arroba + 1 || punto == correo.length() - 1
                || correo.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("El correo del empleado no es válido.");
        }
        return correo;
    }
}
