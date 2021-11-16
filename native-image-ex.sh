#!/usr/bin/env bash

set -eux

java -version
javac JavaNetHttp.java
native-image JavaNetHttp
./javanethttp
./javanethttp -Djdk.httpclient.allowRestrictedHeaders=host
