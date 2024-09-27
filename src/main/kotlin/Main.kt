import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
    val ruta = Paths.get("src/ficheros/calificaciones.csv")

    // Leer las calificaciones de los alumnos
    val calificaciones = apellidosOrdenados(ruta)

    // Añadir la nota final a cada alumno
    val calificacionesConNotaFinal = agregarNotaFinal(calificaciones)

    // Comprobación con las notas finales
    calificacionesConNotaFinal.forEach { alumno ->
        println(alumno)
    }
    println("\n")

    // División de aprobados y suspensos.
    val (aprobados, suspensos) = clasificarAlumnos(calificacionesConNotaFinal)

    println("Alumnos aprobados:")
    aprobados.forEach { println(it) }

    println("\nAlumnos suspensos:")
    suspensos.forEach { println(it) }
}

fun apellidosOrdenados(ruta: Path): List<Map<String, String>> {
    val listaCalificaciones = mutableListOf<Map<String, String>>()

    Files.newBufferedReader(ruta).use { br ->
        val cabeceras = br.readLine().split(";")

        br.forEachLine { linea ->
            val datos = linea.split(";")
            val alumno = mutableMapOf<String, String>()

            // Creación del diccionario para cada alumno
            cabeceras.forEachIndexed { index, clave ->
                alumno[clave] = datos.getOrNull(index) ?: ""
            }

            listaCalificaciones.add(alumno)
        }
    }

    // Ordenamos la lista por apellidos
    return listaCalificaciones.sortedBy { it["Apellidos"] }
}

fun agregarNotaFinal(listaCalificaciones: List<Map<String, String>>): List<Map<String, String>> {
    return listaCalificaciones.map { alumno ->
        // Reemplazar comas por puntos y convertir a Float
        val parcial1Value = alumno["Parcial1"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val ordinario1Value = alumno["Ordinario1"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val parcial2Value = alumno["Parcial2"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val ordinario2Value = alumno["Ordinario2"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f

        // Cálculo de la nota final
        val comprobacion = parcial1Value + parcial2Value
        val mejorParcial1: Float
        val mejorParcial2: Float
        val practicaResultado: Float
        val notaFinal: Float

        if (comprobacion >= 10) {
            mejorParcial1 = parcial1Value
            mejorParcial2 = parcial2Value
        } else {
            mejorParcial1 = maxOf(parcial1Value, ordinario1Value)
            mejorParcial2 = maxOf(parcial2Value, ordinario2Value)
        }

        // Cálculo de las prácticas
        val practica = alumno["Practicas"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val ordinariaPractica = alumno["OrdinarioPracticas"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        practicaResultado = maxOf(practica, ordinariaPractica)


        // Cálculo con los porcentajes: 30% parcial1, 30% parcial2, 40% prácticas
        notaFinal = (mejorParcial1 * 0.3f) + (mejorParcial2 * 0.3f) + (practicaResultado * 0.4f)

        // Agregar la nota final al mapa del alumno
        alumno.toMutableMap().apply {
            this["NotaFinal"] = notaFinal.toString()
        }
    }
}

fun clasificarAlumnos(listaCalificaciones: List<Map<String, String>>): Pair<List<Map<String, String>>, List<Map<String, String>>> {
    val aprobados = mutableListOf<Map<String, String>>()
    val suspensos = mutableListOf<Map<String, String>>()

    listaCalificaciones.forEach { alumno ->
        // Declaración de variables
        val asistencia = alumno["Asistencia"]?.replace("%", "")?.toIntOrNull() ?: 0
        val parcial1Value = alumno["Parcial1"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val ordinario1Value = alumno["Ordinario1"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val parcial2Value = alumno["Parcial2"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val ordinario2Value = alumno["Ordinario2"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        // Cálculo del mejor parcial
        val comprobacion = parcial1Value + parcial2Value
        val mejorParcial1: Float
        val mejorParcial2: Float

        if (comprobacion >= 10) {
            mejorParcial1 = parcial1Value
            mejorParcial2 = parcial2Value
        } else {
            mejorParcial1 = maxOf(parcial1Value, ordinario1Value)
            mejorParcial2 = maxOf(parcial2Value, ordinario2Value)
        }

        val practica = alumno["Practicas"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val ordinariaPractica = alumno["OrdinarioPracticas"]?.replace(",", ".")?.toFloatOrNull() ?: 0.0f
        val practicaResultado = maxOf(practica, ordinariaPractica)


        // Nota final previamente calculada
        val notaFinal = alumno["NotaFinal"]?.toFloatOrNull() ?: 0.0f

        // Criterios de aprobación
        val asistenciaAprobada = asistencia >= 75
        val parcialesAprobados = mejorParcial1 >= 4.0f && mejorParcial2 >= 4.0f
        val practicaAprobada = practicaResultado >= 4.0f
        val notaFinalAprobada = notaFinal >= 5.0f

        // Clasificación del alumno
        if (asistenciaAprobada && parcialesAprobados && practicaAprobada && notaFinalAprobada) {
            aprobados.add(alumno)
        } else {
            suspensos.add(alumno)
        }
    }

    return Pair(aprobados, suspensos)
}
