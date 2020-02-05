# interlok-artifact-downloader 

[![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok-artifact-downloader.svg)](https://github.com/adaptris/interlok-artifact-downloader/tags) ![license](https://img.shields.io/github/license/adaptris/interlok-artifact-downloade.svg) [![Build Status](https://travis-ci.org/adaptris/interlok-artifact-downloader.svg?branch=develop)](https://travis-ci.org/adaptris/interlok-artifact-downloader) [![CircleCI](https://circleci.com/gh/adaptris/interlok-artifact-downloader/tree/develop.svg?style=svg)](https://circleci.com/gh/adaptris/interlok-artifact-downloader/tree/develop) [![codecov](https://codecov.io/gh/adaptris/interlok-artifact-downloader/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok-artifact-downloader) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/adaptris/interlok-artifact-downloader.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/adaptris/interlok-artifact-downloader/context:java) [![Total alerts](https://img.shields.io/lgtm/alerts/g/adaptris/interlok-artifact-downloader.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/adaptris/interlok/alerts/)

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

## Downloader Usage

By default a web application will be started and accessible on *http://localhost:8083/interlok-downloader*. See in the section below for how to change the port and the context path.

The web application has a simple UI with three text fields to enter the Interlok artifact group id (only com.adaptris and group starting with com.adaptris. are allowed), the artifact id and the version.

It also has a rest api accessible at *http://localhost:8083/interlok-downloader/api/artifacts/{group}/{artifact}/{version}*.
More details about the api can be found in the swagger file at *http://localhost:8083/interlok-downloader/api/swagger.json*.

## Starter Usage

The Interlok starter feature make it easier to generate a gradle file to use in an Interlok project to build an Interlok instance with the required optional components and dependencies.

To access the Interlok starter page you have to click on the Interlok Starter menu item.

The web application has a simple UI with two text fields to enter the Interlok version and the list of artifacts id you want to have in the generated build.gradle file.

It also has a rest api accessible at *http://localhost:8083/interlok-downloader/api/starter/generate/{version}?artifacts=interlok-..,interlok-...*.
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
artifact.downloader.nexusBaseUrl=https://nexus.adaptris.net/nexus
artifact.downloader.indexUrl=${artifact.downloader.nexusBaseUrl}/service/local/lucene/search?repositoryId=${repository}&g=com.adaptris&v=${artifact.version}&p=jar
artifact.downloader.repoBaseUrl=${artifact.downloader.nexusBaseUrl}/content/repositories/
artifact.downloader.repos=public,releases,snapshots,thirdparty-snapshots
artifact.downloader.repositoryReleases=releases
artifact.downloader.repositorySnapshots=snapshots
artifact.downloader.credentials.realm=
artifact.downloader.credentials.host=
artifact.downloader.credentials.username=
artifact.downloader.credentials.password=
artifact.downloader.resolverLogLevel=warn
artifact.downloader.destination=${user.home}
artifact.downloader.excludes=com.adaptris:adp-core,com.adaptris:interlok-core,com.adaptris:adp-core-apt,com.adaptris:interlok-core-apt,com.adaptris:adp-stubs,com.adaptris:interlok-stubs,com.adaptris:interlok-common,com.sun*,javax.servlet*,org.slf4j*,org.apache.log4j*,log4j*,commons-logging:commons-logging,maven-plugins

artifact.downloader.unwanted=interlok,adapter-web-gui,interlok-core,interlok-core-apt,interlok-boot,jaxrs-client-proxy,interlok-client,interlok-client-jmx,interlok-common,interlok-logging,interlok-ui-swagger-codegen

artifact.downloader.indexArtifactIdXpath=/searchNGResponse/data/artifact/artifactId/text()

interlok.starter.baseFilesystemUrlNightly=https://development.adaptris.net/nightly_builds/v3.x/${today}/base-filesystem.zip


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
