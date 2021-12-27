package rootheart.codes.weatherhistory.importer


import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import rootheart.codes.weatherhistory.importer.converter.RecordConverter
import rootheart.codes.weatherhistory.importer.converter.RecordProperty
import rootheart.codes.weatherhistory.importer.records.BaseRecord
import spock.genesis.Gen
import spock.lang.Specification

import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class UrlDirectoryReaderSpec extends Specification {
    private static final dateTimePattern = Pattern.compile('\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4} \\d{2}:\\d{2}')
    private static final dataFilenamePattern = Pattern.compile("(stunden|tages)werte_([A-Z]{2})_(\\d{5})_\\w{5}(akt|hist)\\.zip")
    private static final stationFilenamePattern = Pattern.compile("[A-Z]{2}_(Stunden|Tages)werte_Beschreibung_Stationen\\.txt")
    private static final random = new Random(System.currentTimeMillis())

    def 'Test that processing an index HTML file from DWD works'() {
        given: "A mocked web server"
        ClientAndServer.startClientAndServer(8383)
        def mockServer = new MockServerClient("127.0.0.1", 8383)

        and: "That serves a directory listing containing links to a station data file and a data file with randomly generated names"
        def stationDataFileName = Gen.string(stationFilenamePattern).first()
        def dataFileName = Gen.string(dataFilenamePattern).first()
        def directoryListing = directoryListing(stationDataFileName, dataFileName)
        def response = new HttpResponse()
                .withStatusCode(200)
                .withBody(directoryListing)
        mockServer.when(request("GET", "/"))
                .respond(response)

        and: "That serves the station data file for the station data file name"
        def stationDataFileContent = stationDataFileContent()
        response = new HttpResponse()
                .withStatusCode(200)
                .withBody(stationDataFileContent.getBytes())
        mockServer.when(request("GET", "/$stationDataFileName"))
                .respond(response)

        and: "That serves the zipped data file for the data file name"
        def dataFileContent = dataFileContent()
        def baos = new ByteArrayOutputStream()
        def entry = new ZipEntry("produkt_what_comes_next_doesnt_matter.txt")
        new ZipOutputStream(baos).with {
            putNextEntry(entry)
            write(dataFileContent.getBytes())
            close()
        }
        response = new HttpResponse()
                .withStatusCode(200)
                .withBody(baos.toByteArray())
        mockServer.when(request("GET", "/$dataFileName"))
                .respond(response)

        and:
        RecordProperty<TestRecord> property1 = { TestRecord record, String value ->
            record.setProperty1(value)
        }
        RecordProperty<TestRecord> property2 = { TestRecord record, String value ->
            record.setProperty2(value)
        }
        RecordProperty<TestRecord> property3 = { TestRecord record, String value ->
            record.setProperty3(value)
        }
        def columnMapping = ["column1": property1, "column2": property2, "column3": property3]
        def constructor = TestRecord::new
        def recordConverter = new RecordConverter(constructor, columnMapping, 0, 0)

        when: "The data is downloaded from the server and parsed"
        def reader = new UrlDirectoryReader(new URL("http://127.0.0.1:8383"))

        def recordsStream = reader
                .createStreamForDownloadingAndConvertingZippedDataFiles(recordConverter)
        def records = recordsStream.collect(Collectors.toList())

        then: "The downloaded data equals the data specified before"
        records != null
        records.size() == 1

        records[0] != null
        records[0] instanceof TestRecord
        def testRecord = (TestRecord) records[0]
        testRecord.property1 == '1'
        testRecord.property2 == '6.3'
        testRecord.property3 == '3.0'
    }

    static class TestRecord extends BaseRecord {
        String property1
        String property2
        String property3
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

    private static String dataFileContent() {
        return [
                "STATIONS_ID;MESS_DATUM;column1;column2;column3;eor",
                "3;1979110421;    1;    6.3;    3.0;eor"
        ].join('\n')
    }

    private static request(String method, String path) {
        new HttpRequest()
                .withMethod("GET")
                .withPath(path)
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
