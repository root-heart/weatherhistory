package rootheart.codes.common.strings

fun splitAndTrimTokens(line: String): List<String?> = splitAndTrimTokens(line, ';', "-999", { s -> s })

fun <T> splitAndTrimTokens(
    line: String,
    separator: Char,
    nullString: String,
    constructor: (String) -> T,
    list: MutableList<T?> = ArrayList()
): List<T?> {
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

fun <T> splitAndTrimTokensToList(value: String, constructor: (String) -> T): List<T?> {
    var pos = 0
    var end = value.indexOf(',', pos)
    val list: MutableList<T?> = ArrayList()
    while (end >= 0) {
        while (value[pos] == ' ') {
            pos++
        }
        while (value[end - 1] == ' ') {
            end--
        }
        val column = value.substring(pos, end)
        if (column == "null") {
            list.add(null)
        } else {
            list.add(constructor(column))
        }
        pos = end + 1
        end = value.indexOf(',', pos)
    }
    end = value.length
    val column = value.substring(pos, end)
    if (column == "null") {
        list.add(null)
    } else {
        list.add(constructor(column))
    }
    return list
}

