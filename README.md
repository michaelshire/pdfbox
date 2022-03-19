[![Maven Package](https://github.com/michaelshire/pdfbox/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/michaelshire/pdfbox/actions/workflows/maven-publish.yml)

# pdfbox
Test Repo to use GitHub Action with Java code and Maven and output to GitHub Packages

To run the program download the full dependencies package

Set three environment variables:
PDFBOX_USERNAME
PDFBOX_PASSWORD
PDFBOX_HOSTNAME

and run the following commandline:
```
java -cp cb_pdf-1.0-<build>-1-jar-with-dependencies.jar com.couchbase.se.CouchbasePDF <PDFFile>
```
