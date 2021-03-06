# GDI-BY DownloadClient [![Build Status](https://travis-ci.org/gdi-by/downloadclient.svg?branch=master)](https://travis-ci.org/gdi-by/downloadclient)

A Java 8 / JavaFX based desktop client to download geo data.

## Build

    $ mvn clean compile

## Bundle

    $ mvn clean package

## Run
To start with UI:

    $ mvn exec:java

To start in headless mode:

    $ mvn exec:java -Dexec.args=-headless [download-steps.xml ...]


With download-steps.xml being one or more download step files.
If you have built the jar file, run:

    $ java -jar target/downloadclient-1.0-SNAPSHOT.jar

or in headless mode:

    $ java -jar target/downloadclient-1.0-SNAPSHOT.jar --headless [download-steps.xml]


## License

This is Free Software covered by the terms of the Apache License 2.0.  
See LICENSE file for details.


## Environments

### Windows

The software requires Java 8 with JavaFX.
Download a recent JRE from http://www.oracle.com/technetwork/java/javase/downloads/index.html

In order to process data after downloading it, you might require additional
tools, such as ogr2ogr.
One solution to have this tool available is to install gdal.

### Linux

The software was developed using OpenJDK on Debian and Arch Linux, we expect it
to run fine if you are using a recent OpenJDK.

In order to process data after downloading it, you might require additional
tools, such as ogr2ogr. Use the Package Manager of your distribution to install
the required applications.
