# Guía rápida: Apache NetBeans 28

1. **Extrae el ZIP** completo. Mantén `pom.xml`, `nbactions.xml`, `src/` y el JAR dentro de la carpeta del proyecto.
2. En **Tools > Java Platforms**, confirma que existe un JDK 17 o superior. Un JRE sin `javac` no es suficiente para compilar.
3. En **File > Open Project**, elige `AjedrezEstructurado-Lizeth` (la carpeta del `pom.xml`).
4. El proyecto debe aparecer como proyecto **Maven**. Si aparece una advertencia de complementos, revisa la conexión a internet: los plugins de Maven pueden descargarse solo la primera vez.
5. Pulsa **F6**. Si NetBeans pide Main Class, escribe `co.edu.ajedrez.Main`.
6. Si no aparece la ventana, abre la pestaña **Output** y busca el primer error de Maven/Java. Puedes probar también el JAR ya compilado con `java -jar AjedrezEstructurado.jar` desde terminal.
7. Para las pruebas sin Maven, ejecuta `scripts/probar-windows.bat` (con JDK en PATH).

## Errores frecuentes

- **Project not found**: seleccionaste la carpeta equivocada; debe contener `pom.xml`.
- **release version 17 not supported**: estás usando un JDK anterior a 17.
- **NoClassDefFoundError**: abre el proyecto completo y vuelve a compilar; no ejecutes solo una clase aislada sin el resto.
- **Plugin resolution error**: Maven no pudo obtener su plugin; verifica el acceso a Maven Central o ejecuta el JAR incluido.
- **Las partidas guardadas no aparecen**: se guardan en `ajedrez_estructurado_partidas.txt` en la carpeta del usuario (por ejemplo `C:\Users\Nombre`). Si se borra ese archivo, el archivo BST empieza vacío.
- **No se ven imágenes**: deben estar dentro de `src/main/resources/piezas/`; nunca uses rutas absolutas del computador.

## Sustentación

La docente puede pedir una modificación breve; los tres paquetes más útiles para explicar son `estructuras`, `logica` y `servicios`. Cada integrante debe poder ejecutar `PruebasProyecto` (Run File) y explicar por qué cada estructura se usa de verdad. El botón **ESTRUCTURAS** de la pantalla de juego muestra el estado real de cada una en ese momento.
