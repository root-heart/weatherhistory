package rootheart.codes.common.strings

fun splitAndTrimTokens(line: String, list: MutableList<String?> = ArrayList()): List<String?> {
    var pos = 0
    var end = line.indexOf(';', pos)
    while (end >= 0) {
        while (line[pos] == ' ') {
            pos++
        }
        while (line[end - 1] == ' ') {
            end--
        }
        val column: String = line.substring(pos, end)
        if (column.endsWith("-999")) {
            list.add(null)
        } else {
            list.add(column)
        }
        pos = end + 1
        end = line.indexOf(';', pos)
    }
    return list
}

