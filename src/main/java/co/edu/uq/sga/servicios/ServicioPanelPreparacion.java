package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.ColaPreparacionesPorPrioridad;
import co.edu.uq.sga.dominio.contratos.CalculadorRutas;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public final class ServicioPanelPreparacion {

    public record ResumenTarea(
            String codigo,
            String apartamento,
            LocalDateTime llegada
    ) {}

    private final CatalogoApartamentosArbol catalogo;
    private final ColaPreparacionesPorPrioridad cola;
    private final ServicioPreparacion preparacion;

    private final LocalDate fechaOperacion;
    private TareaPreparacion tareaActual;
    private long consecutivo = 1;

    public ServicioPanelPreparacion(
            CatalogoApartamentosArbol catalogo,
            CalculadorRutas calculadorRutas,
            LocalDate fechaOperacion
    ) {
        this.catalogo = Objects.requireNonNull(
                catalogo, "El catálogo es obligatorio."
        );

        this.fechaOperacion = Objects.requireNonNull(
                fechaOperacion, "La fecha de operación es obligatoria."
        );

        cola = new ColaPreparacionesPorPrioridad(fechaOperacion);
        preparacion = new ServicioPreparacion(cola, calculadorRutas);
    }

    public LocalDate consultarFechaOperacion() {
        return fechaOperacion;
    }

    public void encolarPendiente(
            String codigoApartamento,
            LocalDateTime proximaLlegada
    ) {
        var apartamento = catalogo.buscarPorCodigo(codigoApartamento)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el apartamento " + codigoApartamento
                ));

        TareaPreparacion tarea = new TareaPreparacion(
                "PREP-" + consecutivo,
                apartamento,
                LocalDateTime.now(),
                proximaLlegada
        );

        preparacion.encolar(tarea);
        consecutivo++;
    }

    public ResumenTarea[] consultarPendientes() {
        ResumenTarea[] pendientes = new ResumenTarea[cola.tamanio()];
        int indice = 0;

        for (TareaPreparacion tarea : cola) {
            pendientes[indice++] = resumir(tarea);
        }

        return pendientes;
    }

    public Optional<ResumenTarea> consultarActual() {
        return Optional.ofNullable(tareaActual).map(this::resumir);
    }

    public Optional<ResumenTarea> despacharSiguiente() {
        Optional<TareaPreparacion> siguiente =
                preparacion.despacharSiguiente();

        tareaActual = siguiente.orElse(null);

        return siguiente.map(this::resumir);
    }

    public void completarActual() {
        if (tareaActual == null) {
            throw new IllegalStateException(
                    "No hay una tarea en proceso."
            );
        }

        preparacion.completar(tareaActual);
        tareaActual = null;
    }

    private ResumenTarea resumir(TareaPreparacion tarea) {
        return new ResumenTarea(
                tarea.getCodigo(),
                tarea.getApartamento().getCodigo(),
                tarea.getFechaHoraLlegada()
        );
    }
}