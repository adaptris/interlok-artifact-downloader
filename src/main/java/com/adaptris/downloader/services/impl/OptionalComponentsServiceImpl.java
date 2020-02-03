package com.adaptris.downloader.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adaptris.downloader.config.ArtifactDownloaderException;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.services.OptionalComponentsService;
import com.adaptris.downloader.utils.VersionUtils;
import com.adaptris.downloader.utils.XmlUtils;

@Service
public class OptionalComponentsServiceImpl implements OptionalComponentsService {
  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String NEXUS_BASE_URL_TKN = "${artifact.downloader.nexusBaseUrl}";
  private static final String REPOSITORY_TKN = "${repository}";
  private static final String ARTIFACT_VERSION_TKN = "${artifact.version}";

  @Inject
  private ArtifactDownloaderProperties properties;

  public OptionalComponentsServiceImpl() {
  }

  @Override
  public List<String> loadArtifacts(String version) throws ArtifactDownloaderException {
    List<String> artifacts = extractArtifacts(getXmlDocument(version));
    Collections.sort(artifacts);
    return artifacts;
  }

  protected Document getXmlDocument(String version) throws ArtifactDownloaderException {
    try {
      return XmlUtils.getDocument(getNexusIndexUrl(version));
    } catch (SAXException | IOException | ParserConfigurationException expts) {
      throw new ArtifactDownloaderException(expts);
    }
  }

  protected final String getNexusIndexUrl(String version) {
    return properties.getIndexUrl().replace(NEXUS_BASE_URL_TKN, properties.getNexusBaseUrl())
        .replace(REPOSITORY_TKN, getRepository(version))
        .replace(ARTIFACT_VERSION_TKN, version);
  }

  protected final String getRepository(String version) {
    if (VersionUtils.isSnapshot(version)) {
      return properties.getRepositorySnapshots();
    }
    return properties.getRepositoryReleases();
  }

  protected final List<String> extractArtifacts(Document index) throws ArtifactDownloaderException {
    try {
      List<String> artifacts = new ArrayList<>();
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPathExpression expr = xPathfactory.newXPath().compile(properties.getIndexArtifactIdXpath());
      NodeList nodes = (NodeList) expr.evaluate(index, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        String artifactId = nodes.item(i).getNodeValue();
        if (notUnwanted(artifactId)) {
          artifacts.add(artifactId);
        }
      }
      return artifacts;
    } catch (XPathExpressionException | DOMException expts) {
      throw new ArtifactDownloaderException(expts);
    }
  }

  private boolean notUnwanted(String artifactId) {
    return !properties.getUnwanted().contains(artifactId);
  }

}
