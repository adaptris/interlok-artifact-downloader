# interlok-artifact-downloader [![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok-artifact-downloader.svg)](https://github.com/adaptris/interlok-artifact-downloader/tags) ![license](https://img.shields.io/github/license/adaptris/interlok-service-tester.svg) [![Build Status](https://travis-ci.org/adaptris/interlok-artifact-downloader.svg?branch=develop)](https://travis-ci.org/adaptris/interlok-artifact-downloader) [![codecov](https://codecov.io/gh/adaptris/interlok-artifact-downloader/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok-artifact-downloader) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/2a98063de9d04c49a9c5012b28bcc5b8)](https://www.codacy.com/app/adaptris/interlok-artifact-downloader?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=adaptris/interlok-artifact-downloader&amp;utm_campaign=Badge_Grade)

Helper application that allows you to download Interlok artifacts from a nexus server.

## Build

```
$ ./gradlew clean assemble
:clean
:compileJava
:processResources
:classes
:jar
:findMainClass
:startScripts
:distTar SKIPPED
:distZip SKIPPED
:bootRepackage
:assemble

BUILD SUCCESSFUL

Total time: ... secs
```

## Execute

```
$ java -jar ./build/libs/interlok-artifact-downloader(-version).jar
```

## Usage

By default a web application will be started and accessible on *http://localhost:8083/interlok-downloader*. See in the section below for how to change the port and the context path.

The web application has a simple UI with three text fields to enter the Interlok artifact group id (only com.adaptris and group starting with com.adaptris. are allowed), the artifact id and the version.

It also has a rest api accessible at *http://localhost:8083/interlok-downloader/api/artifacts/{group}/{artifact}/{version}*.
More details about the api can be found in the swagger file at *http://localhost:8083/interlok-downloader/api/swagger.json*.

## Help

You can also override the default properties by providing an application.properties file with *--spring.config.additional-location*:

```
$ java -jar ./build/libs/interlok-artifact-downloader(-version).jar --spring.config.additional-location=file:/path/to/application.properties
```
Example of properties to override:

```
# Server Configuration
server.contextPath=/interlok-downloader
server.port=8083

# Logging Configuration
logging.config=file:/path/to/log4j2.xml

# Artifact Downloader Configuration
artifact.downloader.repoBaseUrl=https://development.adaptris.net/nexus/content/repositories/
artifact.downloader.repos=public,releases,snapshots,thirdparty-snapshots
artifact.downloader.credentials.realm=
artifact.downloader.credentials.host=
artifact.downloader.credentials.username=
artifact.downloader.credentials.password=
artifact.downloader.resolverLogLevel=warn
artifact.downloader.destination=${user.home}
artifact.downloader.excludes=com.adaptris:adp-core,com.adaptris:interlok-core,com.adaptris:adp-core-apt,com.adaptris:interlok-core-apt,com.adaptris:adp-stubs,com.adaptris:interlok-stubs,com.adaptris:interlok-common,com.sun*,javax.servlet*,org.slf4j*,org.apache.log4j*,log4j*,commons-logging:commons-logging,maven-plugins

```

## Credentials

If the repositories are private you can specify the credentials using the following properties:

* artifact.downloader.credentials.realm=The credential realms (if ivy resolver is used)
* artifact.downloader.credentials.host=The host protected by the credentials, most likely the host of the repoBaseUrl (if ivy resolver is used)
* artifact.downloader.credentials.username=The username
* artifact.downloader.credentials.password=The password

## Artifact Resolvers

By default Interlok Artifact Resolver uses Ivy to resolve and download Interlok artifact and dependencies.

## Ivy Resolver Log Level

Ivy resolver logs a lot of info directly in the console. You can control the level of logs with *artifact.downloader.resolverLogLevel*.

The accepted values are:
* error
* warn
* info
* verbose
* debug

## Ignored

The following packages are always ignored to keep the number of dependencies down:

* com.adaptris:adp-core
* com.adaptris:interlok-core
* com.adaptris:adp-core-apt
* com.adaptris:interlok-core-apt
* com.adaptris:adp-stubs
* com.adaptris:interlok-stubs
* com.adaptris:interlok-common
* com.sun
* javax.servlet
* org.slf4j
* org.apache.log4j
* log4j
* commons-logging:commons-logging
* maven-plugins
