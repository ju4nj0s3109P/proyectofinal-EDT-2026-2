package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.CalculadorRutas;
import co.edu.uq.sga.dominio.contratos.ColaPreparaciones;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import co.edu.uq.sga.estructuras.lineales.Conjunto;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.time.LocalDateTime;
import java.util.Optional;

/** Coordina tareas de preparación, conserva su historial y delega las rutas al grafo. */
public class ServicioPreparacion {
    private final ColaPreparaciones cola;
    private final CalculadorRutas calculadorRutas;
    private final Conjunto<String> apartamentosConTarea = new Conjunto<>();
    private final ListaEnlazada<TareaPreparacion> tareasCompletadas = new ListaEnlazada<>();
    private TareaPreparacion tareaEnProceso;

    public ServicioPreparacion(ColaPreparaciones cola, CalculadorRutas calculadorRutas) {
        if (cola == null) throw new IllegalArgumentException("La cola de preparación es obligatoria.");
        if (calculadorRutas == null) throw new IllegalArgumentException("El calculador de rutas es obligatorio.");
        this.cola = cola;
        this.calculadorRutas = calculadorRutas;
    }

    /** Encola sin cambiar el estado: mientras espera, el apartamento sigue pendiente. */
    public void encolar(TareaPreparacion tarea) {
        if (tarea == null) throw new IllegalArgumentException("La tarea es obligatoria.");
        if (tarea.getEstado() != EstadoTareaPreparacion.ENCOLADA) {
            throw new ExcepcionDominio("Solo se pueden encolar tareas nuevas.");
        }
        Apartamento apartamento = tarea.getApartamento();
        String codigo = apartamento.getCodigo();
        if (apartamento.getEstadoOperativo() != EstadoApartamento.PENDIENTE_PREPARACION) {
            throw new ExcepcionDominio("El apartamento debe estar pendiente de preparación.");
        }
        if (apartamentosConTarea.contiene(codigo)) {
            throw new ExcepcionDominio("El apartamento ya tiene una tarea encolada o en proceso.");
        }
        cola.encolar(tarea);
        apartamentosConTarea.agregar(codigo);
    }

    public Optional<TareaPreparacion> despacharSiguiente() {
        return despacharSiguiente(LocalDateTime.now());
    }

    public Optional<TareaPreparacion> despacharSiguiente(LocalDateTime fechaHoraInicio) {
        if (fechaHoraInicio == null) throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        if (tareaEnProceso != null) {
            throw new ExcepcionDominio("Debe completarse la tarea actual antes de despachar otra.");
        }
        if (cola.estaVacia()) return Optional.empty();
        TareaPreparacion siguiente = cola.desencolar();
        if (siguiente == null || siguiente.getEstado() != EstadoTareaPreparacion.ENCOLADA
                || siguiente.getApartamento().getEstadoOperativo() != EstadoApartamento.PENDIENTE_PREPARACION) {
            throw new ExcepcionDominio("La cola contiene una tarea que no puede despacharse.");
        }
        siguiente.iniciar(fechaHoraInicio);
        siguiente.getApartamento().cambiarEstadoOperativo(EstadoApartamento.EN_PREPARACION);
        tareaEnProceso = siguiente;
        return Optional.of(siguiente);
    }

    public void completar(TareaPreparacion tarea) {
        completar(tarea, LocalDateTime.now());
    }

    public void completar(TareaPreparacion tarea, LocalDateTime fechaHoraFinalizacion) {
        if (tarea == null) throw new IllegalArgumentException("La tarea es obligatoria.");
        if (tareaEnProceso != tarea || tarea.getEstado() != EstadoTareaPreparacion.EN_PROCESO) {
            throw new ExcepcionDominio("La tarea no es la que está en proceso.");
        }
        tarea.completar(fechaHoraFinalizacion);
        tarea.getApartamento().cambiarEstadoOperativo(EstadoApartamento.PREPARADO);
        apartamentosConTarea.eliminar(tarea.getApartamento().getCodigo());
        tareasCompletadas.agregarAlFinal(tarea);
        tareaEnProceso = null;
    }

    /** Historial en orden de finalización, usado por los reportes de tiempos de preparación. */
    public TareaPreparacion[] consultarCompletadas() {
        return tareasCompletadas.aArreglo(TareaPreparacion[]::new);
    }

    public String[] calcularRuta(String origen, String destino) {
        validarUbicacion(origen, "origen");
        validarUbicacion(destino, "destino");
        String inicio = origen.trim(), fin = destino.trim();
        String[] ruta = calculadorRutas.calcularRuta(inicio, fin);
        validarRuta(ruta, inicio, fin);
        return ruta.clone();
    }

    /** Calcula una ruta mínima por los destinos recibidos en orden de prioridad. */
    public String[] calcularRecorrido(String origen, String[] destinos) {
        validarUbicacion(origen, "origen");
        if (destinos == null) throw new IllegalArgumentException("Los destinos son obligatorios.");
        if (destinos.length == 0) return new String[]{origen.trim()};
        String[] ruta = calculadorRutas.calcularRecorrido(origen.trim(), destinos.clone());
        if (ruta == null || ruta.length == 0) {
            throw new ExcepcionDominio("No existe un recorrido que conecte todos los apartamentos pendientes.");
        }
        if (!origen.trim().equals(ruta[0])
                || !destinos[destinos.length - 1].trim().equals(ruta[ruta.length - 1])) {
            throw new ExcepcionDominio("El recorrido no conecta el origen con la última parada.");
        }
        return ruta.clone();
    }

    private void validarRuta(String[] ruta, String origen, String destino) {
        if (ruta == null || ruta.length == 0) {
            throw new ExcepcionDominio("No existe una ruta entre las ubicaciones indicadas.");
        }
        for (String ubicacion : ruta) validarUbicacion(ubicacion, "ubicación de ruta");
        if (!origen.equals(ruta[0]) || !destino.equals(ruta[ruta.length - 1])) {
            throw new ExcepcionDominio("La ruta devuelta no conecta el origen con el destino.");
        }
    }

    private void validarUbicacion(String ubicacion, String nombre) {
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException("La " + nombre + " es obligatoria.");
        }
    }
}
