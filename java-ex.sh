#!/usr/bin/env bash

set -eux

java -version
javac JavaNetHttp.java
java JavaNetHttp
java -Djdk.httpclient.allowRestrictedHeaders=host JavaNetHttp
