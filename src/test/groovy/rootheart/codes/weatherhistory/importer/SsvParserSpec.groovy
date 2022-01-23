package rootheart.codes.weatherhistory.importer

import rootheart.codes.weatherhistory.importer.ssv.SsvParser
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class SsvParserSpec extends Specification {
    @Unroll
    def 'Test that parsing a string that contains newlines as line separators and semicolons as column separators is parsed correctly'() {
        given: 'a parser able to parse semicolon-separated values'
        def ssvParser = new SsvParser()

        and: 'lines with semicolon separated strings with a line that contains column names'
        def reader = new BufferedReader(new StringReader(fileContent))

        when: 'the ssv parser parsed the input'
        def parsed = ssvParser.parse(reader)

        then: 'the column names are extracted'
        parsed.columnNames == expectedColumnNames

        and: 'the values for each line as well'
        parsed.rows.collect(Collectors.toList()) == expectedLines

        where:
        fileContent                                             | expectedColumnNames               | expectedLines
        ''                                                      | []                                | []
        'column1'                                               | ['column1']                       | []
        'column1\n'                                             | ['column1']                       | []
        'column1\nvalue1'                                       | ['column1']                       | [['value1']]
        'column1\nvalue1\nvalue2\nvalue3'                       | ['column1']                       | [['value1'], ['value2'], ['value3']]
        'column1;column2\nvalue1;value2'                        | ['column1', 'column2']            | [['value1', 'value2']]
        'column1;column2\nvalue1.1;value1.2\nvalue2.1;value2.2' | ['column1', 'column2']            | [['value1.1', 'value1.2'], ['value2.1', 'value2.2']]
        'column1;column2;column3\nvalue1;value2;value3'         | ['column1', 'column2', 'column3'] | [['value1', 'value2', 'value3']]
        'column1;column2\nvalue1'                               | ['column1', 'column2']            | [['value1']]
        'column1;column2\nvalue1.1\nvalue2.1;value2.2'          | ['column1', 'column2']            | [['value1.1'], ['value2.1', 'value2.2']]
    }

    @Unroll
    def 'Test that values that represent null are interpreted as null'() {
        given: 'a parser able to parse semicolon-separated values'
        def ssvParser = new SsvParser()

        and: 'lines with semicolon separated strings with a line that contains column names'
        def reader = new BufferedReader(new StringReader(fileContent))

        when: 'the ssv parser parsed the input'
        def parsed = ssvParser.parse(reader)

        then: 'the column names are extracted'
        parsed.columnNames == expectedColumnNames

        and: 'the values for each line as well'
        parsed.rows.collect(Collectors.toList()) == expectedLines

        where:
        fileContent                     | expectedColumnNames    | expectedLines
        'column1\n-999'                 | ['column1']            | [[null]]
        'column1\nvalue1\n-999\nvalue3' | ['column1']            | [['value1'], [null], ['value3']]
        'column1;column2\nvalue1;-999'  | ['column1', 'column2'] | [['value1', null]]
    }
}
