package rootheart.codes.weatherhistory.importer

import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.converter.RecordProperty
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import rootheart.codes.weatherhistory.model.QualityLevel
import rootheart.codes.weatherhistory.model.StationId
import spock.genesis.Gen
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class UrlDirectoryReaderSpec extends Specification {
    private static final dateTimePattern = Pattern.compile('\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4} \\d{2}:\\d{2}')
    private static final stationFilenamePattern = Pattern.compile("[A-Z]{2}_(Stunden|Tages)werte_Beschreibung_Stationen\\.txt")
    private static final columnNamePattern = Pattern.compile("[A-Z_0-9]{4,10}")
    private static final stringValuePattern = Pattern.compile("[A-Za-z-_0-9]{2,10}")
    private static final random = new Random(System.currentTimeMillis())
    private static final dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")

    private static class TestData {
        def stationId = Gen.using { StationId.of(randomInt(1, 99999)) }.first()
        def records = generateTestRecords(stationId, 10)
        def filename = generateDataFileName(stationId)

        byte[] getZippedDataFile(String qualityLvlColName, String stringColName, String numericColName) {
            def recordsStringList = [
                    "STATIONS_ID;MESS_DATUM;$qualityLvlColName;$stringColName;$numericColName;eor"
            ]
            recordsStringList += records.collect { testRecordToSsvString(it) }
            def dataFileStringContent = recordsStringList.join('\n')
            def baos = new ByteArrayOutputStream()
            new ZipOutputStream(baos).with {
                putNextEntry(new ZipEntry("produkt_what_comes_here_doesnt_matter.txt"))
                write(dataFileStringContent.getBytes())
                close()
            }
            return baos.toByteArray()
        }
    }

    @Unroll("Test Run #i")
    def 'Test that processing an index HTML file from DWD works'() {
        given: "Some randomized test data"
        def qualityLvlColName = randomColumnName()
        def stringColName = randomColumnName()
        def numericColName = randomColumnName()
        def stationDataFileName = Gen.string(stationFilenamePattern).first()
        def testData = (1..10).collect { new TestData() }

        and: "A mocked web server"
        def randomPort = random.nextInt(10000) + 8000
        ClientAndServer.startClientAndServer(randomPort)
        def mockServer = new MockServerClient("127.0.0.1", randomPort)

        and: "That serves a directory listing containing links to a station data file and a data file with randomly generated names"
        mockServer
                .when(request("GET", "/"))
                .respond(respond(200, generateDirectoryListing(stationDataFileName, testData)))

        and: "That serves the station data file for the station data file name"
        mockServer.when(request("GET", "/$stationDataFileName"))
                .respond(respond(200, stationDataFileContent(testData)))

        and: "That serves the zipped data file for the data file name"
        testData.each {
            mockServer.when(request("GET", "/$it.filename"))
                    .respond(respond(200, it.getZippedDataFile(qualityLvlColName, stringColName, numericColName)))
        }

//        println "Data file content ==================================================="
//        println testData.generateDataFileContent
//        println "====================================================================="

        and: "A converter able to populate TestRecords with data"
        def columnMapping = [
                (qualityLvlColName): { TestRecord r, String v -> r.qualityLevel = QualityLevel.of(v) } as RecordProperty<TestRecord>,
                (stringColName)    : { TestRecord r, String v -> r.stringValue = v } as RecordProperty<TestRecord>,
                (numericColName)   : { TestRecord r, String v -> r.numericValue = new BigDecimal(v) } as RecordProperty<TestRecord>
        ]
        def recordConverter = new RecordConverter<TestRecord>(TestRecord::new, columnMapping, 0, 0)

        when: "The data is downloaded from the server, parsed and converted"
        def url = new URL("http://127.0.0.1:$randomPort")
        def parsedRecords = [:] as Map<StationId, List<TestRecord>>
        new UrlDirectoryReader(url)
                .forEachDataLine((colNames, values) -> {
                    recordConverter.validateColumnNames(colNames)
                    recordConverter.determineIndicesOfColumnsAlwaysPresent(colNames)
                    def record = recordConverter.createRecord(colNames, values)
                    def records = parsedRecords[record.stationId]
                    if (records == null) {
                        records = []
                        parsedRecords[record.stationId] = records
                    }
                    records.add(record)
                })
//                .downloadAndParseData(recordConverter)

        then: "The downloaded data equals the data specified before"
        parsedRecords != null
        parsedRecords.size() == testData.size()
        parsedRecords.keySet().containsAll(testData*.stationId)

        parsedRecords.each { stationId, parsedRecordsForStation ->
//            def parsedRecordsForStation = recordsStream.collect() as List<TestRecord>
            def testRecordsForStation = testData.find { it.stationId == stationId }.records

            assert parsedRecordsForStation.size() == testRecordsForStation.size()
            parsedRecordsForStation.eachWithIndex { generatedRecord, index ->
                assert generatedRecord.stationId == stationId
                assert generatedRecord.measurementTime == testRecordsForStation[index].measurementTime
                assert generatedRecord.qualityLevel == testRecordsForStation[index].qualityLevel
                assert generatedRecord.stringValue == testRecordsForStation[index].stringValue
                assert generatedRecord.numericValue == testRecordsForStation[index].numericValue
            }
        }

        where:
        i << (1..10)
    }

    private static String generateDirectoryListing(String stationDataFileName, List<TestData> testData) {
        def lines = [
                "<html>",
                "<head><title>Index of /some/directory/containing/zipped/files</title></head>",
                "<body>",
                "<h1>Index of /some/directory/containing/zipped/files</h1><hr><pre><a href=\"../\">../</a>",
                fileLink("some_pdf_file_containing_a_german_description.pdf"),
                fileLink("some_pdf_file_containing_an_english_description.pdf"),
                fileLink(stationDataFileName),
        ]
        testData.forEach { lines.add(fileLink(it.filename)) }
        lines += "</pre><hr></body>"
        lines += "</html>"
        return lines.join('\n')
    }

    private static String generateDataFileName(StationId stationId) {
        def stationIdString = String.format("%05d", stationId.stationId)
        def dataFilenamePattern = Pattern.compile("(stunden|tages)werte_([A-Z]{2})_${stationIdString}_\\w{5}(akt|hist)\\.zip")
        Gen.string(dataFilenamePattern).first()
    }

    private static List<TestRecord> generateTestRecords(StationId stationId, int count) {
        return Gen.type(
                [stationId      : Gen.using { stationId },
                 measurementTime: Gen.using { randomMeasurementTime() },
                 qualityLevel   : Gen.using { randomQualityLevel() },
                 stringValue    : Gen.string(stringValuePattern),
                 numericValue   : Gen.double],
                TestRecord
        ).take(10).collect() as List<TestRecord>
    }

    static int randomInt(int min, int max) {
        return random.nextInt(max - min) + min
    }

    static String randomColumnName() {
        Gen.string(columnNamePattern).first()
    }

    static class TestRecord extends BaseRecord {
        String stringValue
        BigDecimal numericValue
    }

    private static QualityLevel randomQualityLevel() {
        QualityLevel.values()[random.nextInt(QualityLevel.values().length)]
    }

    private static LocalDateTime randomMeasurementTime() {
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


    private static stationDataFileContent(List<TestData> testData) {
        def fileContentLines = [
                "Stations_id von_datum bis_datum Stationshoehe geoBreite geoLaenge Stationsname Bundesland",
                "----------- --------- --------- ------------- --------- --------- ----------------------------------------- ----------"]
        testData.forEach {
            fileContentLines +=
                    "$it.stationId 19500401 20110401            202     50.7827    6.0941 Aachen                                   Nordrhein-Westfalen"
        }

        return fileContentLines.join('\n')
    }


    private static String testRecordToSsvString(TestRecord record) {
        return "$record.stationId.stationId;" +
                "${dateTimeFormatter.format(record.measurementTime)};" +
                "$record.qualityLevel.code;" +
                "$record.stringValue;" +
                "$record.numericValue;" +
                "eor"
    }

    private static request(String method, String path) {
        new HttpRequest()
                .withMethod(method)
                .withPath(path)
    }

    private static respond(int status, byte[] body) {
        new HttpResponse()
                .withStatusCode(status)
                .withBody(body)
    }

    private static respond(int status, String body) {
        new HttpResponse()
                .withStatusCode(status)
                .withBody(body)
    }

    private static String fileLink(String target) {
        "<a href=\"$target\">$target</a>         ${randomDateTime()}              ${randomSize()}"
    }

    private static String randomDateTime() {
        Gen.string(dateTimePattern).first()
    }

    private static String randomSize() {
        random.nextInt(100_000)
    }

    private static String randomSpace() {
        return " " * random.nextInt(30)
    }
}
