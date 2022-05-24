package rootheart.codes.weatherhistory.importer

import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import spock.genesis.Gen
import spock.lang.Specification

import java.util.regex.Pattern

class HtmlDirectoryParserSpec extends Specification implements SpecUtils {
    private static final STATIONS_FILENAME_GENERATOR = Gen.string(Pattern.compile("(TU|N|TD|FX|TF|SD|VV|FF|RR)_(Stunden|Tages)werte_Beschreibung_Stationen.txt"))
    private static final DATA_FILENAME_GENERATOR = Gen.string(Pattern.compile("(stunden|tages)werte_(TU|N|TD|FX|TF|SD|VV|FF|RR)_\\d{5}_(akt|[0-9]{8}-[0-9]{8}_hist)\\.zip"))
    private static final DIRECTORY_NAME_GENERATOR = Gen.string(Pattern.compile("[A-Za-z0-9_]{10,30}"))

    def """Test that parsing a HTML that contains three subdirectories with no content, three data files and a
           stations file produces the corresponding HtmlDirectory"""() {
        given:
        def randomPort = randomInt(8000, 10000)
        ClientAndServer.startClientAndServer(randomPort)
        def mockServer = new MockServerClient("127.0.0.1", randomPort)

        and:
        def stationDataFileName = STATIONS_FILENAME_GENERATOR.first()
        def dataFileNames = DATA_FILENAME_GENERATOR.take(5).collect()
        def directoryNames = DIRECTORY_NAME_GENERATOR.take(3).collect()

        mockServer.when(request("GET", "/"))
                .respond(respond(200, directoryListing(stationDataFileName, dataFileNames, directoryNames)))

        directoryNames.each {
            mockServer.when(request("GET", "/$it/"))
                    .respond(respond(200, directoryListing(null, [], [])))
        }

        when:
        def url = new URL("http://127.0.0.1:$randomPort/")
        def htmlDirectory = HtmlDirectoryParser.INSTANCE.parseHtml(url)

        then:
        htmlDirectory != null

        htmlDirectory.subDirectories != null
        htmlDirectory.subDirectories.size() == directoryNames.size()
        htmlDirectory.subDirectories.every { it.subDirectories != null }
        htmlDirectory.subDirectories.every { it.subDirectories.size() == 0 }

        htmlDirectory.zippedDataFiles != null
        htmlDirectory.zippedDataFiles.size() == dataFileNames.size()
        htmlDirectory.zippedDataFiles.every { dataFile ->
            dataFileNames.find { fileName ->
                println "$fileName / $dataFile.url"
                dataFile.url.toExternalForm().endsWith(fileName)
            }
        }
    }

    def """Test that parsing a HTML that contains one subdirectory with two data files and two subdirectories
           produces the corresponding HTML directory"""() {
        given:
        def randomPort = randomInt(8000, 10000)
        ClientAndServer.startClientAndServer(randomPort)
        def mockServer = new MockServerClient("127.0.0.1", randomPort)

        and:
        def mainDirectoryName = DIRECTORY_NAME_GENERATOR.first()

        mockServer.when(request("GET", "/"))
                .respond(respond(200, directoryListing(null, [], [mainDirectoryName])))

        and:
        def subDirectoryNames = DIRECTORY_NAME_GENERATOR.take(2).collect()
        def dataFileNames = DATA_FILENAME_GENERATOR.take(5).collect()

        mockServer.when(request("GET", "/$mainDirectoryName/"))
                .respond(respond(200, directoryListing(null, dataFileNames, subDirectoryNames)))

        subDirectoryNames.each {
            mockServer.when(request("GET", "/$mainDirectoryName/$it/"))
                    .respond(respond(200, directoryListing(null, [], [])))
        }

        when:
        def url = new URL("http://127.0.0.1:$randomPort/")
        def htmlDirectory = HtmlDirectoryParser.INSTANCE.parseHtml(url)

        then:
        htmlDirectory != null
        htmlDirectory.subDirectories != null
        htmlDirectory.subDirectories.size() == 1
        htmlDirectory.subDirectories[0].subDirectories != null
        htmlDirectory.subDirectories[0].subDirectories.size() == subDirectoryNames.size()

        htmlDirectory.subDirectories[0].zippedDataFiles != null
        htmlDirectory.subDirectories[0].zippedDataFiles.size() == dataFileNames.size()
        htmlDirectory.subDirectories[0].zippedDataFiles.every { dataFile ->
            dataFileNames.find { fileName ->
                dataFile.url.toExternalForm().endsWith(fileName)
            }
        }

        htmlDirectory.zippedDataFiles != null
        htmlDirectory.zippedDataFiles.size() == 0
    }



    private static String directoryListing(String stationDataFileName,
                                           Collection<String> dataFilenames,
                                           Collection<String> directoryNames) {
        def lines = [
                "<html>",
                "<head><title>Index of /some/directory/containing/zipped/files</title></head>",
                "<body>",
                "<h1>Index of /some/directory/containing/zipped/files</h1><hr><pre><a href=\"../\">../</a>",
                fileLink("some_pdf_file_containing_a_german_description.pdf"),
                fileLink("some_pdf_file_containing_an_english_description.pdf"),
        ]

        if (stationDataFileName) {
            lines.add(fileLink(stationDataFileName))

        }
        dataFilenames.forEach {
            lines.add(fileLink(it))
        }

        directoryNames.forEach {
            lines.add("<a href=\"$it/\">../</a>")
        }

        lines.add("</pre><hr></body>")
        lines.add("</html>")

        return lines.join('\r\n')
    }
}
