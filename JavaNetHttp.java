import java.net.http.HttpRequest;
import java.net.URI;
import static java.util.Optional.ofNullable;

public class JavaNetHttp {
    public static void main(String[] args) {
        System.out.printf("jdk.httpclient.allowRestrictedHeaders %s\n",
                          ofNullable(System.getProperty("jdk.httpclient.allowRestrictedHeaders"))
                          .orElse("Not found"));
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(new URI("https://www.graalvm.org"))
                .headers("host", "some-host")
                .build();
            System.out.println("Hello, http-request!");
            System.exit(1);
        } catch(Throwable t) {
            System.err.println(t.getMessage());
        }
    }
}
