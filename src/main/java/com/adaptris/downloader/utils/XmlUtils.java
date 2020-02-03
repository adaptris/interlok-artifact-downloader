package com.adaptris.downloader.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlUtils {

  private XmlUtils() {
  }

  public static Document getDocument(String url) throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
    return newDocumentBuilder().parse(new URL(url).openStream());
  }

  private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setXIncludeAware(true);
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder;
  }

}
