import rootheart.codes.weatherhistory.importer.splitColumns
import rootheart.codes.weatherhistory.importer.splitWithKotlin
import spock.genesis.Gen
import java.util.regex.Pattern

const val warmupCount = 50
const val runCount = 10
val timeFunction = System::currentTimeMillis

fun main() {
    val functions = listOf(::mySplitImplementation, ::kotlinStringSplitImplementation, ::mySplitImplementationReusingResultList)
    val execTimes = functions.associateWith { Array<Long>(runCount) { 0 } }

    println("warming up")
    for (function in functions) {
        repeat(warmupCount) { function() }
    }

    println("Executing")
    for ((function, execTimeArray) in execTimes) {
        println(function.name)
        repeat(runCount) { run ->
            val s = timeFunction()
            function()
            val e = timeFunction()
            execTimeArray[run] = e - s
        }
    }

    val longestFunctionNameLength = functions.maxByOrNull { it.name.length }!!.name.length
    val format = "%${longestFunctionNameLength}s    %8.1f    " + (0 until runCount).joinToString("    ") { "%5d" }

    for (execTime in execTimes) {
        val formattedLine = String.format(format, execTime.key.name, execTime.value.average(), *execTime.value)
        execTime.value.average()
        println(formattedLine)
    }
}

val lines: List<String> by lazy { initializeLines() }

fun initializeLines(): List<String> {
    println("Initialize lines")
    val separatedStringValuePattern = Pattern.compile("[A-Za-z0-9.-]{5,15}")!!
    val generator = Gen.string(separatedStringValuePattern)
    val lines = (0 until 200_000).map {
        generator.take(20).joinToString(";")
    }
    return lines
}

fun mySplitImplementation() {
    for (line in lines) {
        splitColumns(line)
    }
}


fun mySplitImplementationReusingResultList() {
    val list = ArrayList<String>()
    for (line in lines) {
        splitColumns(line, list)
        list.clear()
    }
}

fun kotlinStringSplitImplementation() {
    for (line in lines) {
        splitWithKotlin(line)
    }
}
