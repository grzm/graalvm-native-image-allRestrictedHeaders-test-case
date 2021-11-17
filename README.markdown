# graalvm native-image vs jdk.httpclient.allowRestrictedHeaders

This is an example showing how graalvm native-image does not respect
the `jdk.httpclient.allowRestrictedHeaders` system property used by
java.net.http.HttpRequest.

## Background

The [`java.net.http.HttpRequest$Builder` `header`
method][header-method] restricts which headers can be included in an
HttpRequest per RFC 7230. As of JDK12, this can be overridden by
setting the `jdk.httpclient.allowRestrictedHeaders` system
property. See the following resolved JDK tickets for details.

* [Make restricted headers in HTTP Client configurable and remove Date by default](https://bugs.openjdk.java.net/browse/JDK-8213189)
* [Make restricted headers in HTTP Client configurable and remove Date by default](https://bugs.openjdk.java.net/browse/JDK-8213696)

OpenJDK17 and the JDK included with the GraalVM distribution
respect this property, allowing the setting of otherwise-restricted
headers.

[header-method]: https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.Builder.html#header(java.lang.String,java.lang.String)

 Native images built with GraalVM native-image detect the system
 property, but do not respect it: adding restricted headers continues
 to throw an exception.

## Comparison with OpenJDK and JDK bundled with GraalVM distribution

```java
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
        } catch(Throwable t) {
            System.err.println(t.getMessage());
        }
    }
}
```

I use jenv to conveniently switch between Java installations. Use
whatever works for you to set `JAVA_HOME` and the like.

### Using OpenJDK 17

```shell
jenv local 17
./java-ex.sh 
```
```
+ java -version
openjdk version "17.0.1" 2021-10-19
OpenJDK Runtime Environment Homebrew (build 17.0.1+0)
OpenJDK 64-Bit Server VM Homebrew (build 17.0.1+0, mixed mode, sharing)
+ javac JavaNetHttp.java
+ java JavaNetHttp
jdk.httpclient.allowRestrictedHeaders Not found
restricted header name: "host"
+ java -Djdk.httpclient.allowRestrictedHeaders=host JavaNetHttp
jdk.httpclient.allowRestrictedHeaders host
Hello, http-request!
```

### Using the OpenJDK 17 runtime included in the GraalVM CE 21.3 distribution

```shell
jenv local graalvm64-17.0.1
java-ex.sh
```
```
+ java -version
openjdk version "17.0.1" 2021-10-19
OpenJDK Runtime Environment GraalVM CE 21.3.0 (build 17.0.1+12-jvmci-21.3-b05)
OpenJDK 64-Bit Server VM GraalVM CE 21.3.0 (build 17.0.1+12-jvmci-21.3-b05, mixed mode, sharing)
+ javac JavaNetHttp.java
+ java JavaNetHttp
jdk.httpclient.allowRestrictedHeaders Not found
restricted header name: "host"
+ java -Djdk.httpclient.allowRestrictedHeaders=host JavaNetHttp
jdk.httpclient.allowRestrictedHeaders host
Hello, http-request!
```

In both OpenJDK 17 distributions, the "host" header is no longer considered restricted when the `jdk.httpclient.allowRestrictedHeaders` system property is set.


### Using GraalVM CE 21.3 native-image

```
./native-image-ex.sh
```

```
+ java -version
openjdk version "17.0.1" 2021-10-19
OpenJDK Runtime Environment GraalVM CE 21.3.0 (build 17.0.1+12-jvmci-21.3-b05)
OpenJDK 64-Bit Server VM GraalVM CE 21.3.0 (build 17.0.1+12-jvmci-21.3-b05, mixed mode, sharing)
+ javac JavaNetHttp.java
+ native-image JavaNetHttp
[javanethttp:2171]    classlist:     586.78 ms,  0.96 GB
[javanethttp:2171]        (cap):   1,618.34 ms,  0.96 GB
[javanethttp:2171]        setup:   2,816.76 ms,  0.96 GB
[javanethttp:2171]     (clinit):     165.19 ms,  2.35 GB
[javanethttp:2171]   (typeflow):   1,660.12 ms,  2.35 GB
[javanethttp:2171]    (objects):   3,370.30 ms,  2.35 GB
[javanethttp:2171]   (features):   1,343.62 ms,  2.35 GB
[javanethttp:2171]     analysis:   6,900.17 ms,  2.35 GB
[javanethttp:2171]     universe:     627.43 ms,  2.35 GB
[javanethttp:2171]      (parse):     517.89 ms,  2.35 GB
[javanethttp:2171]     (inline):     566.97 ms,  2.36 GB
[javanethttp:2171]    (compile):   5,108.36 ms,  4.17 GB
[javanethttp:2171]      compile:   6,862.57 ms,  4.17 GB
[javanethttp:2171]        image:   1,058.30 ms,  4.17 GB
[javanethttp:2171]        write:     332.94 ms,  4.17 GB
[javanethttp:2171]      [total]:  19,377.70 ms,  4.17 GB
# Printing build artifacts to: /Users/grzm/dev/jnh.x/javanethttp.build_artifacts.txt
+ ./javanethttp
jdk.httpclient.allowRestrictedHeaders Not found
restricted header name: "host"
+ ./javanethttp -Djdk.httpclient.allowRestrictedHeaders=host
jdk.httpclient.allowRestrictedHeaders host
restricted header name: "host"
```

The "host" header is still considered restricted even when the `jdk.httpclient.allowRestrictedHeaders` system property is set.


## System information

macOS Catalina 10.15.7, 2.4 GHz 8-Core Intel Core i9

```
$ uname -a
Darwin ryouanji 19.6.0 Darwin Kernel Version 19.6.0: Tue Oct 12 18:34:05 PDT 2021; root:xnu-6153.141.43~1/RELEASE_X86_64 x86_64
$ sw_vers
ProductName:	Mac OS X
ProductVersion:	10.15.7
BuildVersion:	19H1519
```
