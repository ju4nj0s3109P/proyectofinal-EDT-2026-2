package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.ConflictoReserva;

/** Puerto para guardar conflictos de solicitudes externas para reportes. */
public interface RegistroConflictosReservas {
    void registrar(ConflictoReserva conflicto);
}
