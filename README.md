# SGA — Sistema de Gestión de Alojamiento

Proyecto final de Estructura de Datos (2026-2).

## Estado
Estructura inicial para trabajar en equipo. Los módulos están intencionalmente incompletos.

## Antes de programar
1. Leer `docs/DECISIONES.md` y acordar cualquier cambio ahí antes de implementarlo.
2. Revisar `docs/CONTRATOS-MODULOS.md` para respetar interfaces y límites de cada módulo.
3. Trabajar en una rama propia y abrir Pull Request; no trabajar directamente en `main`.
4. No usar colecciones de `java.util` en el núcleo. Las estructuras del proyecto se implementan en `estructuras/`.

## Requisitos base
- Aplicación de escritorio para un único alojamiento.
- Java 17, Maven y paquetes bajo `co.edu.uq.sga`.
- Separación: dominio → estructuras → servicios → persistencia/interfaz.
- La salida de una estancia es exclusiva: estancia representada como intervalo [entrada, salida).
- Las reglas del negocio viven en servicios/dominio, nunca en la GUI.

## Construir
Requiere JDK 17 y Maven.

```sh
mvn test
mvn package
```

## Equipo
Registrar integrantes, responsabilidades acordadas y enlaces a PRs en `docs/EQUIPO.md`.
