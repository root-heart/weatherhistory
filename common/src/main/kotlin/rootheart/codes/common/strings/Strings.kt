package rootheart.codes.common.strings

fun splitAndTrimTokens(line: String): List<String?> = splitAndTrimTokens(line, ';', "-999", { s -> s })

fun <T> splitAndTrimTokens(line: String, separator: Char, nullString: String, constructor: (String) -> T, list: MutableList<T?> = ArrayList()): List<T?> {
    var pos = 0
    var end = line.indexOf(separator, pos)
    while (end >= 0) {
        while (line[pos] == ' ') {
            pos++
        }
        while (line[end - 1] == ' ') {
            end--
        }
        val column = line.substring(pos, end)
        if (column.endsWith(nullString)) {
            list.add(null)
        } else {
            list.add(constructor(column))
        }
        pos = end + 1
        end = line.indexOf(separator, pos)
    }
    return list
}

inline fun <reified T> splitAndTrimTokensToArrayWithLength24(value: String, constructor: (String) -> T): Array<T?> {
    var pos = 0
    var end = value.indexOf(',', pos)
    val array: Array<T?> = Array(24) { null }
    var index = 0
    while (end >= 0) {
        while (value[pos] == ' ') {
            pos++
        }
        while (value[end - 1] == ' ') {
            end--
        }
        val column = value.substring(pos, end)
        if (column == "null") {
            array[index++] = null
        } else {
            array[index++] = constructor(column)
        }
        pos = end + 1
        end = value.indexOf(',', pos)
    }
    end = value.length
    val column = value.substring(pos, end)
    if (column == "null") {
        array[index] = null
    } else {
        array[index] = constructor(column)
    }
    return array
}

