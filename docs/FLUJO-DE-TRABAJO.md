# Flujo de trabajo del equipo

1. Una tarea por rama: `feat/reservas-disponibilidad`, `feat/arbol-busqueda`, etc.
2. No programar sobre `main`. Abrir Pull Request pequeño y pedir una revisión.
3. Una persona aprueba la interfaz de un módulo antes de que otros dependan de ella.
4. Evitar que dos ramas editen los mismos archivos. Si una modificación transversal es necesaria, acordar el cambio y hacer un PR coordinado.
5. Cada PR declara: propósito, archivos tocados, cómo se comprobó y decisiones relevantes.
6. Integrar por etapas: compilación limpia → estructuras → dominio → servicios → persistencia → GUI.
7. Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`. Los commits deben ser reales y describir cambios; no dividir artificialmente un cambio para alcanzar el mínimo.
8. Antes de cerrar una tarea: actualizar documentación/diagrama si cambió un contrato y revisar conflictos con `main`.
