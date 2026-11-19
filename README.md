# Ajedrez Estructurado

**Proyecto final de Estructuras de Datos — Corporación Universitaria Autónoma del Cauca**  
**Estudiante indicada:** Lizeth Juliana Chavez Lopez  
**Otros integrantes:** pendientes de completar antes de entregar.  
**Lenguaje:** Java 17 | **Interfaz:** Swing | **IDE previsto:** Apache NetBeans 28 | **Gestión:** Maven.

## ¿Qué incluye?

Ajedrez completo para **dos personas** o **contra la computadora** (Fácil / Normal / Difícil), con tablero 8×8, las 6 piezas, solo movimientos legales, jaque, jaque mate, enroque, captura al paso, promoción, ahogado y todas las reglas de tablas (automáticas y reclamables).

Las estructuras evaluadas están programadas con **nodos y referencias propias** en `src/main/java/co/edu/ajedrez/estructuras`. No se usa `ArrayList`, `Stack`, `Queue`, `LinkedList` ni ninguna colección de Java en todo el proyecto.

| Estructura | Uso en el juego | Operaciones |
|---|---|---|
| Pila | Deshacer y rehacer jugadas (dos pilas) | `apilar`, `desapilar`, `verTope`, `estaVacia`, `tamano`, `limpiar` |
| Cola | Reproducción automática de la partida (FIFO) | `encolar`, `desencolar`, `verFrente`, `estaVacia`, `tamano`, `limpiar` |
| Lista circular | Turnos Blancas → Negras → Blancas (`GestorTurnos`) | `insertarInicio`, `insertarFinal`, `insertar`, `eliminar`, `eliminarEn`, `buscar`, `obtener`, `recorrer`, `avanzarTurno`, `retrocederTurno` |
| Lista doble | Registro de jugadas con revisión ◀ ▶, piezas vivas de cada jugador y repeticiones | `insertarInicio`, `insertarFinal`, `insertar`, `eliminar`, `eliminarEn`, `eliminarUltimo`, `buscar`, `recorrerAdelante`, `recorrerAtras` |
| Árbol BST | Archivo de partidas terminadas (clave = n.º de medias jugadas) | `insertar`, `buscar`, `buscarTodos`, `eliminar`, `inorden`, `preorden`, `posorden`, `altura` |
| Árbol de jugadas | La IA arma el árbol de jugadas posibles y elige con minimax | `agregarHijo`, `recorrerHijos`, `preorden` |

## Abrir en Apache NetBeans 28

1. Extrae **todo** el archivo ZIP en una carpeta (no lo abras desde dentro del ZIP).
2. Abre NetBeans 28 y entra a **File > Open Project**.
3. Selecciona la carpeta `AjedrezEstructurado-Lizeth` que contiene `pom.xml`; no selecciones solo `src`.
4. Verifica que NetBeans use un JDK **17 o superior** en Tools > Java Platforms.
5. Espera la importación de Maven la primera vez (puede descargar plugins de construcción).
6. Pulsa **F6 / Run Project**. Si pide la clase principal, escribe `co.edu.ajedrez.Main`.

## Ejecutar sin NetBeans

Con Java 17 o superior: `java -jar AjedrezEstructurado.jar`, o en Windows `scripts/ejecutar-windows.bat`.

## Pruebas

- En NetBeans: clic derecho sobre `src/test/java/co/edu/ajedrez/PruebasProyecto.java` > **Run File**. También se ejecutan solas con **Clean and Build** (fase `test` de Maven).
- Sin Maven, en Windows: `scripts\probar-windows.bat`.

Son **95 pruebas** sin JUnit: todas las operaciones de las estructuras, el motor de ajedrez con *perft* (conteo de jugadas comparado con valores oficiales: 20, 400, 8 902, 197 281…), reglas especiales, deshacer/rehacer, tablas, IA y archivo BST. Resultado: **95/95 correctas**.

## Instrucciones rápidas de juego

- En el menú elige el modo, escribe los nombres (opcional) y pulsa **NUEVA PARTIDA**.
- Haz clic en una pieza de tu turno y luego en una casilla marcada (o arrástrala).
- **DESHACER / REHACER** (Ctrl+Z / Ctrl+Y) usan pilas LIFO.
- **REPRODUCIR PARTIDA** usa la cola FIFO: la partida se repite sola; **PAUSA** y **PASO ▶** la controlan.
- **◀ ATRÁS / ADELANTE ▶** recorren el registro con la lista doble (o clic en una jugada). **SALIR REPR.** vuelve a la partida real sin cambios.
- **ESTRUCTURAS** muestra el estado real de cada estructura en ese momento.
- **RECLAMAR TABLAS** se habilita con triple repetición o regla de 50 jugadas.
- Cada partida terminada se guarda sola en el **ARCHIVO BST** (y en el archivo `ajedrez_estructurado_partidas.txt` de la carpeta del usuario, así que se conserva al cerrar el juego). Allí se ven los tres recorridos, se busca por número de jugadas, se reproduce, se revisa y se elimina.

## Organización

- `estructuras/`: Pila, Cola, ListaCircular, ListaDoble, ArbolBST, ArbolJugadas y sus nodos.
- `modelo/`: ColorPieza, Pieza, Movimiento, Jugador y PartidaGuardada.
- `logica/`: MotorAjedrez (tablero y reglas) e IA (minimax).
- `servicios/`: GestorPartida (une todas las estructuras), GestorTurnos (lista circular) y RegistroPartidas (BST + archivo).
- `interfaz/`: VentanaPrincipal, PanelMenu, PanelJuego, PanelTablero, PanelJugador, PanelPartidas, PanelReglas, Dialogo, Piezas y Tema (colores y botones).
- `src/main/resources/piezas/`: los 12 PNG de las piezas.
- `src/test/java/`: pruebas automáticas.

## GitHub / GitLab

El enunciado requiere un repositorio con commits de **todos los integrantes**. Antes de entregar, el equipo debe crear el repositorio, hacer commits reales, añadir los integrantes que faltan aquí y en los manuales, y poder explicar todo el código.

## Créditos de recursos

Los 12 iconos de piezas se generaron para este proyecto con los símbolos Unicode de GNU FreeSerif. Véase `RECURSOS_GRAFICOS.txt`.
