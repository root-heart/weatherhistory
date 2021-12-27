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
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class UrlDirectoryReaderSpec extends Specification {
    private static final dateTimePattern = Pattern.compile('\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4} \\d{2}:\\d{2}')
    private static final dataFilenamePattern = Pattern.compile("(stunden|tages)werte_([A-Z]{2})_(\\d{5})_\\w{5}(akt|hist)\\.zip")
    private static final stationFilenamePattern = Pattern.compile("[A-Z]{2}_(Stunden|Tages)werte_Beschreibung_Stationen\\.txt")
    private static final columnNamePattern = Pattern.compile("[A-Z_0-9]{4,10}")
    private static final stringValuePattern = Pattern.compile("[A-Za-z-_0-9]{2,10}")
    private static final random = new Random(System.currentTimeMillis())
    private static final dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")

    @Unroll("Test run #i")
    def 'Test that processing an index HTML file from DWD works'() {
        given: "Some test records"
        def recordCount = random.nextInt(10) + 2
        def stationId = random.nextInt(100) + 100
        def randomTestRecords = (0..recordCount).collect { randomTestRecord(stationId) }

        and: "A mocked web server"
        def randomPort = random.nextInt(10000) + 8000
        ClientAndServer.startClientAndServer(randomPort)
        def mockServer = new MockServerClient("127.0.0.1", randomPort)

        and: "That serves a directory listing containing links to a station data file and a data file with randomly generated names"
        def stationDataFileName = Gen.string(stationFilenamePattern).first()
        def dataFileName = Gen.string(dataFilenamePattern).first()
        def directoryListing = directoryListing(stationDataFileName, dataFileName)
        mockServer
                .when(request("GET", "/"))
                .respond(respond(200, directoryListing))

        and: "That serves the station data file for the station data file name"
        mockServer.when(request("GET", "/$stationDataFileName"))
                .respond(respond(200, stationDataFileContent()))

        and: "That serves the zipped data file for the data file name"
        def qualityLevelColumnName = Gen.string(columnNamePattern).first()
        def stringValueColumnName = Gen.string(columnNamePattern).first()
        def numericValueColumnName = Gen.string(columnNamePattern).first()
        def dataFileContent = dataFileContent(randomTestRecords, qualityLevelColumnName, stringValueColumnName, numericValueColumnName)
        def baos = new ByteArrayOutputStream()
        def entry = new ZipEntry("produkt_what_comes_here_doesnt_matter.txt")
        new ZipOutputStream(baos).with {
            putNextEntry(entry)
            write(dataFileContent.getBytes())
            close()
        }
        mockServer.when(request("GET", "/$dataFileName"))
                .respond(respond(200, baos.toByteArray()))

        println "Data file content ==================================================="
        println dataFileContent
        println "====================================================================="

        and: "A converter able to populate TestRecords with data"
        def columnMapping = [
                (qualityLevelColumnName): { TestRecord r, String v -> r.qualityLevel = QualityLevel.of(v) } as RecordProperty<TestRecord>,
                (stringValueColumnName) : { TestRecord r, String v -> r.stringValue = v } as RecordProperty<TestRecord>,
                (numericValueColumnName): { TestRecord r, String v -> r.numericValue = new BigDecimal(v) } as RecordProperty<TestRecord>
        ]
        def recordConverter = new RecordConverter(TestRecord::new, columnMapping, 0, 0)

        when: "The data is downloaded from the server and parsed"
        def records = new UrlDirectoryReader(new URL("http://127.0.0.1:$randomPort"))
                .createStreamForDownloadingAndConvertingZippedDataFiles(recordConverter)
                .collect(Collectors.toList())

        then: "The downloaded data equals the data specified before"
        records != null
        records.size() == randomTestRecords.size()
        records.eachWithIndex { record, index ->
            assert record != null
            assert record instanceof TestRecord
            assert record.stationId.stationId == stationId
            assert record.measurementTime == randomTestRecords[index].measurementTime
            assert record.qualityLevel == randomTestRecords[index].qualityLevel
            assert record.stringValue == randomTestRecords[index].stringValue
            assert record.numericValue == randomTestRecords[index].numericValue
        }

        where:
        i << (1..10)
    }

    static class TestRecord extends BaseRecord {
        String stringValue
        BigDecimal numericValue
    }

    private static randomTestRecord(int stationId) {
        def record = new TestRecord()
        record.stationId = StationId.of(stationId)
        record.measurementTime = randomMeasurementTime()
        record.qualityLevel = QualityLevel.values()[random.nextInt(QualityLevel.values().length)]
        record.numericValue = random.nextDouble()
        record.stringValue = Gen.string(stringValuePattern).first()
        return record
    }

    private static LocalDateTime randomMeasurementTime() {
        def month = Month.of(random.nextInt(12) + 1)
        def year = random.nextInt(100) + 1900
        def yearAndMonth = LocalDate.of(year, month, 1)
        def day = random.nextInt(month.maxLength()) + 1
        if (month == Month.FEBRUARY && day == 29 && yearAndMonth.isLeapYear()) {
            day--
        }
        def date = LocalDate.of(year, month, day)
        def hour = random.nextInt(24)
        return date.atTime(hour, 0)
    }

    private static String directoryListing(String stationDataFileName, String dataFileName) {
        return [
                "<html>",
                "<head><title>Index of /some/directory/containing/zipped/files</title></head>",
                "<body>",
                "<h1>Index of /some/directory/containing/zipped/files</h1><hr><pre><a href=\"../\">../</a>",
                fileLink("some_pdf_file_containing_a_german_description.pdf"),
                fileLink("some_pdf_file_containing_an_english_description.pdf"),
                fileLink(stationDataFileName),
                fileLink(dataFileName),
                "</pre><hr></body>",
                "</html>"
        ].join('\n')
    }

    private static stationDataFileContent() {
        return [
                "Stations_id von_datum bis_datum Stationshoehe geoBreite geoLaenge Stationsname Bundesland",
                "----------- --------- --------- ------------- --------- --------- ----------------------------------------- ----------",
                "00003 19500401 20110401            202     50.7827    6.0941 Aachen                                   Nordrhein-Westfalen"
        ].join('\n')
    }

    private static String dataFileContent(List<TestRecord> records, String qualityLevelColumnName,
                                          String stringPropertyColumnName, String numericPropertyColumnMame) {
        def recordsStringList = [
                "STATIONS_ID;MESS_DATUM;$qualityLevelColumnName;$stringPropertyColumnName;$numericPropertyColumnMame;eor"
        ]
        recordsStringList += records.collect { testRecordToSsvString(it) }
        return recordsStringList.join('\n')
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
