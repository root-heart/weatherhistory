import org.joda.time.LocalDate
import org.joda.time.Months
import rootheart.codes.common.measureAndLogDuration
import java.io.File
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local

fun main() {
    val x = Array<Pair<Int, Int>?>(10) { null }
    println(x.isArrayOf<Pair<Int, Int>>())

//    println(Months.monthsBetween(LocalDate(2022, 1, 1), LocalDate(2022, 2,1)).months)
//    val dir = File("C:\\Users\\kai\\Documents\\Scans")
//    recursively(dir)
//    sumInts()
//    sumBigDecimals()
}

fun recursively(dir: File) {
    if (dir.absolutePath.startsWith("C:\\Users\\kai\\Documents\\Scans\\Arztdokumente\\Röntgenbilder gebrochener Finger")
            || dir.absolutePath.startsWith("C:\\Users\\kai\\Documents\\Scans\\Steuersachen")
            || dir.absolutePath.startsWith("C:\\Users\\kai\\Documents\\Scans\\fidor\\Überweisungsbelege")
            || dir.absolutePath.startsWith(" C:\\Users\\kai\\Documents\\Scans\\Arbeit\\Freiberuflichkeit\\Steuerberater")) {
        return
    }
    for (file in dir.listFiles()!!) {
        if (file.isDirectory) {
            recursively(file)
        } else {
//            val regex = Regex("(?<wer>.*)\\s\\+\\+\\s(?<betreff>.*)\\s\\+\\+\\s(?<datum>\\d{8})\\.pdf")
            val regex = Regex("(?<wer>.*)\\s\\+\\+\\s(?<datum>\\d{8})\\s\\+\\+\\s(?<betreff>.*)\\.pdf")
            val fileName = file.name
            val match = regex.find(fileName)
            if (match != null) {
                val namedMatch = match.groups as MatchNamedGroupCollection
                val newFileName = namedMatch["datum"]!!.value + " ++ " + namedMatch["wer"]!!.value + " ++ " + namedMatch["betreff"]!!.value + ".pdf"
                val newFile = File(file.parentFile, newFileName)
                println("${file.absolutePath} -> ${newFile.absolutePath}")
                file.renameTo(newFile)
            } else if (!fileName.matches(Regex("\\d{8}\\s\\+\\+\\s.*\\s\\+\\+\\s.*\\.pdf"))) {
                println("### ${file.absolutePath}")
            }
        }
    }

}

fun sumInts() {
    val r = Random(2000)
    repeat(100000) {
        {}.measureAndLogDuration("summing values") {
            var sum = 0
            for (i in 0..100000) {
                sum += r.nextInt()
            }
//            println(sum)
        }
    }
}

fun sumBigDecimals() {
    val r = Random(2000)
    repeat(100000) {
        {}.measureAndLogDuration("summing values") {
            var sum = BigDecimal(0)
            for (i in 0..100000) {
                sum += BigDecimal(r.nextInt())
            }
//            println(sum)
        }
    }
}