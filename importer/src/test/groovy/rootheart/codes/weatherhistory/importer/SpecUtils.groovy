package rootheart.codes.weatherhistory.importer

import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import spock.genesis.Gen

import java.util.regex.Pattern

trait SpecUtils {
    private static final dateTimePattern = Pattern.compile('\\d{2}-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4} \\d{2}:\\d{2}')
    private static final random = new Random(System.currentTimeMillis())

    List<BigDecimal> allBigDecimalsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? new BigDecimal(it[index]) : null }
    }

    List<Integer> allIntsOf(List<List<String>> values, int index) {
        values.collect { it[index] != null ? Integer.parseInt(it[index]) : null }
    }

    static HttpRequest request(String method, String path) {
        new HttpRequest()
                .withMethod(method)
                .withPath(path)
    }

    static HttpResponse respond(int status, String body) {
        new HttpResponse()
                .withStatusCode(status)
                .withBody(body)
    }

    static HttpResponse respond(int status, byte[] body) {
        new HttpResponse()
                .withStatusCode(status)
                .withBody(body)
    }

    static String fileLink(String target) {
        "<a href=\"$target\">$target</a>         ${randomDateTime()}              ${randomSize()}"
    }

    static String randomDateTime() {
        Gen.string(dateTimePattern).first()
    }

    static String randomSize() {
        random.nextInt(100_000)
    }

    int randomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    BigDecimal randomDecimal(int min, int max, int scale) {
        long unscaled = randomInt(min, max)
        for (int i = 0; i < scale; i++) {
            unscaled *= 10
        }
        return BigDecimal.valueOf(unscaled, scale)
    }
}