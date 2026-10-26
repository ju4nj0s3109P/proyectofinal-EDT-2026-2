package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.CalculadorRutas;
import co.edu.uq.sga.dominio.contratos.ColaPreparaciones;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import java.util.Optional;

/** Coordina las tareas de preparación y delega FIFO y rutas a sus estructuras. */
public class ServicioPreparacion {
    private final ColaPreparaciones cola;
    private final CalculadorRutas calculadorRutas;
    private TareaPreparacion tareaEnProceso;

    public ServicioPreparacion(ColaPreparaciones cola, CalculadorRutas calculadorRutas) {
        if (cola == null) {
            throw new IllegalArgumentException("La cola de preparación es obligatoria.");
        }
        if (calculadorRutas == null) {
            throw new IllegalArgumentException("El calculador de rutas es obligatorio.");
        }
        this.cola = cola;
        this.calculadorRutas = calculadorRutas;
    }

    /** Encola la tarea y marca el apartamento para impedir duplicados. */
    public void encolar(TareaPreparacion tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea es obligatoria.");
        }
        if (tarea.getEstado() != EstadoTareaPreparacion.ENCOLADA) {
            throw new ExcepcionDominio("Solo se pueden encolar tareas nuevas.");
        }
        Apartamento apartamento = tarea.getApartamento();
        if (apartamento.getEstadoOperativo() != EstadoApartamento.PENDIENTE_PREPARACION) {
            throw new ExcepcionDominio(
                    "El apartamento debe estar pendiente de preparación y no tener otra tarea.");
        }
        cola.encolar(tarea);
        apartamento.cambiarEstadoOperativo(EstadoApartamento.EN_PREPARACION);
    }

    /** Despacha en FIFO; permite una tarea en proceso a la vez. */
    public Optional<TareaPreparacion> despacharSiguiente() {
        if (tareaEnProceso != null) {
            throw new ExcepcionDominio("Debe completarse la tarea actual antes de despachar otra.");
        }
        if (cola.estaVacia()) {
            return Optional.empty();
        }
        TareaPreparacion siguiente = cola.desencolar();
        if (siguiente == null || siguiente.getEstado() != EstadoTareaPreparacion.ENCOLADA
                || siguiente.getApartamento().getEstadoOperativo()
                        != EstadoApartamento.EN_PREPARACION) {
            throw new ExcepcionDominio("La cola contiene una tarea que no puede despacharse.");
        }
        siguiente.iniciar();
        tareaEnProceso = siguiente;
        return Optional.of(siguiente);
    }

    /** Completa la tarea actual y deja el apartamento preparado. */
    public void completar(TareaPreparacion tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea es obligatoria.");
        }
        if (tareaEnProceso != tarea
                || tarea.getEstado() != EstadoTareaPreparacion.EN_PROCESO) {
            throw new ExcepcionDominio("La tarea no es la que está en proceso.");
        }
        tarea.completar();
        tarea.getApartamento().cambiarEstadoOperativo(EstadoApartamento.PREPARADO);
        tareaEnProceso = null;
    }

    /** Calcula y valida una ruta entre dos ubicaciones. */
    public String[] calcularRuta(String origen, String destino) {
        validarUbicacion(origen, "origen");
        validarUbicacion(destino, "destino");
        String inicio = origen.trim();
        String fin = destino.trim();
        String[] ruta = calculadorRutas.calcularRuta(inicio, fin);
        if (ruta == null || ruta.length == 0) {
            throw new ExcepcionDominio("No existe una ruta entre las ubicaciones indicadas.");
        }
        String[] copia = ruta.clone();
        for (int i = 0; i < copia.length; i++) {
            validarUbicacion(copia[i], "ubicación de ruta");
            copia[i] = copia[i].trim();
        }
        if (!inicio.equals(copia[0]) || !fin.equals(copia[copia.length - 1])) {
            throw new ExcepcionDominio("La ruta devuelta no conecta el origen con el destino.");
        }
        return copia;
    }

    private void validarUbicacion(String ubicacion, String nombre) {
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException("La " + nombre + " es obligatoria.");
        }
    }
}
