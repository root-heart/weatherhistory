package rootheart.codes.weatherhistory.importer

import groovy.sql.DataSet
import groovy.sql.Sql
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import rootheart.codes.weatherhistory.database.WeatherDb
import rootheart.codes.weatherhistory.importer.converter.BigDecimalProperty
import rootheart.codes.weatherhistory.importer.converter.IntProperty
import rootheart.codes.weatherhistory.importer.html.RecordType
import rootheart.codes.weatherhistory.importer.html.ZippedDataFile
import rootheart.codes.weatherhistory.model.StationId
import spock.genesis.Gen
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class StationDataImporterSpec extends Specification implements SpecUtils {
    private static final dateTimePattern = Pattern.compile('\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4} \\d{2}:\\d{2}')
    private static final stationFilenamePattern = Pattern.compile("[A-Z]{2}_(Stunden|Tages)werte_Beschreibung_Stationen\\.txt")
    private static final columnNamePattern = Pattern.compile("[A-Z_0-9]{4,10}")
    private static final stringValuePattern = Pattern.compile("[A-Za-z-_0-9]{2,10}")
    private static final random = new Random(System.currentTimeMillis())
    private static final dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")

    def setup() {
        WeatherDb.INSTANCE.createTables()
    }

    def cleanup() {
        WeatherDb.INSTANCE.dropTables()
    }

    private static byte[] getZippedDataFileBytes(ZippedDataFile zippedDataFile, List<Map<String, String>> measurements) {
        def columnNames = new ArrayList<>(zippedDataFile.recordType.columnNameMapping.keySet())
        columnNames.add("STATIONS_ID")
        columnNames.add("MESS_DATUM")
        columnNames.add("eor")

        def dataRows = measurements
                .collect { measurementToSsvString(zippedDataFile, columnNames, it) }
        def rows = []
        rows.add(columnNames.join(';'))
        rows.addAll(dataRows)
        def baos = new ByteArrayOutputStream()
        new ZipOutputStream(baos).with {
            putNextEntry(new ZipEntry("produkt_what_comes_here_doesnt_matter.txt"))
            write(rows.join('\r\n').getBytes())
            close()
        }
        return baos.toByteArray()
    }

    def 'Test that processing an index HTML file from DWD works'() {
        given: "A mocked web server"
        def randomPort = random.nextInt(10000) + 8000
        ClientAndServer.startClientAndServer(randomPort)
        def mockServer = new MockServerClient("127.0.0.1", randomPort)

        and: "Some randomized test data"
        def stationId = StationId.of(randomInt(1, 99999))
        def recordType = RecordType.AIR_TEMPERATURE
        def fileName = generateDataFileName(stationId)
        def file = new ZippedDataFile(fileName, stationId, recordType, false, new URL("http://localhost:$randomPort/$fileName"))
        def measurementStrings = [
                ['MESS_DATUM': '1990010101', 'TT_TU': '12.3', 'RF_TU': '23.4'],
                ['MESS_DATUM': '1990010102', 'TT_TU': '12.3', 'RF_TU': '23.4'],
                ['MESS_DATUM': '1990010201', 'TT_TU': '12.3', 'RF_TU': '23.4'],
                ['MESS_DATUM': '1990020101', 'TT_TU': '12.3', 'RF_TU': '23.4'],
                ['MESS_DATUM': '1990030101', 'TT_TU': '12.3', 'RF_TU': '23.4'],
                ['MESS_DATUM': '1991020101', 'TT_TU': '12.3', 'RF_TU': '23.4'],
                ['MESS_DATUM': '2000010101', 'TT_TU': '12.3', 'RF_TU': '23.4']
        ]

        and: "That serves the zipped data file for the data file name"
        mockServer.when(request("GET", "/$fileName"))
                .respond(respond(200, getZippedDataFileBytes(file, measurementStrings)))

        when:
        StationDataImporter.INSTANCE.import([file])

        then: "The downloaded data equals the data specified before"
        noExceptionThrown()
        def db = new Sql(WeatherDb.INSTANCE.dataSource)
        def records = db.rows("select * from summarized_measurements")

        records.size() > 0
        records.findAll { it['INTERVAL_TYPE'] == 'DAY' }.size() == 6
        records.findAll { it['INTERVAL_TYPE'] == 'MONTH' }.size() == 5
        records.findAll { it['INTERVAL_TYPE'] == 'SEASON' }.size() == 4
        records.findAll { it['INTERVAL_TYPE'] == 'YEAR' }.size() == 3
        records.findAll { it['INTERVAL_TYPE'] == 'DECADE' }.size() == 2
    }

    private static String generateDataFileName(StationId stationId) {
        def stationIdString = String.format("%05d", stationId.stationId)
        def dataFilenamePattern = Pattern.compile("(stunden|tages)werte_([A-Z]{2})_${stationIdString}_\\w{5}(akt|hist)\\.zip")
        Gen.string(dataFilenamePattern).first()
    }

    private static Map<String, Object> getRandomValues(RecordType recordType) {
        return recordType.columnNameMapping.collectEntries { name, property ->
            def value = ''
            if (property instanceof BigDecimalProperty) {
                value = randomDecimal(-100, 100, 1) as String
            } else if (property instanceof IntProperty) {
                value = randomInt(-100, 100) as String
            }
            [(name): value]
        }
    }

    private static measurementToSsvString(ZippedDataFile zippedDataFile, List<String> columnNames, Map<String, Object> valuesByColumnName) {
        def values = columnNames.collect {
            switch (it) {
                case "STATIONS_ID": return zippedDataFile.stationId.stationId as String
                case "eor": return "eor"
                default: return valuesByColumnName[it]
            }
        }
        return values.join(";")
    }

    private static LocalDateTime randomTime() {
        def month = Month.of(random.nextInt(12) + 1)
        def year = random.nextInt(100) + 1900
        def yearAndMonth = LocalDate.of(year, month, 1)
        def day = random.nextInt(month.maxLength()) + 1
        if (month == Month.FEBRUARY && day == 29 && !yearAndMonth.isLeapYear()) {
            day--
        }
        def date = LocalDate.of(year, month, day)
        def hour = random.nextInt(24)
        return date.atTime(hour, 0)
    }
}
