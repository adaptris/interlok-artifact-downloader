package com.adaptris.downloader.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.adaptris.downloader.config.InterlokStarterProperties;
import com.adaptris.downloader.services.BuildGradleFileService;
import com.adaptris.downloader.utils.ResourceUtils;
import com.adaptris.downloader.utils.VersionUtils;

@Service
public class BuildGradleFileServiceImpl implements BuildGradleFileService {

  private static final String BUILD_GRADLE_TEMPLATE = "gradle-templates/build.gradle.template";
  private static final String INTERLOK_VERSION = "interlokVersion";
  private static final String INTERLOK_BASE_FILESYSTEM_URL = "interlokBaseFilesystemUrl";

  @Inject
  private InterlokStarterProperties properties;

  @Override
  public List<GradleItem> generate(String version, List<String> artifacts) throws IOException {
    List<GradleItem> gradleItems = new ArrayList<>();
    // TODO Use better template engine
    gradleItems.add(createBuildGradleFile(artifacts, BUILD_GRADLE_TEMPLATE));
    gradleItems.add(createGradlePropertiesFile(version));

    return gradleItems;
  }

  private GradleItem createBuildGradleFile(List<String> artifacts, String buildGradleTemplateName) throws IOException {
    StringBuilder interlokRuntimeSb = new StringBuilder();
    StringBuilder interlokJavadocsSb = new StringBuilder();

    for (String artifact : artifacts) {
      interlokRuntimeSb.append("  interlokRuntime (\"com.adaptris:").append(artifact)
          .append(":$interlokVersion\") { changing=true }");
      interlokJavadocsSb.append("  interlokJavadocs (\"com.adaptris:").append(artifact)
          .append(":$interlokVersion:javadoc\") { changing=true; transitive=false }");
      if (artifacts.indexOf(artifact) != artifacts.size() - 1) {
        interlokRuntimeSb.append(System.lineSeparator());
        interlokJavadocsSb.append(System.lineSeparator());
      }
    }

    String buildGradleTemplate = ResourceUtils.toString(buildGradleTemplateName);

    String buildGradleContent = buildGradleTemplate.replace("#{interlokRuntime}", interlokRuntimeSb.toString());
    buildGradleContent = buildGradleContent.replace("#{interlokJavadocs}", interlokJavadocsSb.toString());

    final byte[] bytes = buildGradleContent.getBytes(StandardCharsets.UTF_8);

    return new GradleItem() {
      @Override
      public byte[] getPayload() {
        return bytes;
      }

      @Override
      public String getName() {
        return "build.gradle";
      }
    };
  }

  private GradleItem createGradlePropertiesFile(String interlokVersion) throws IOException {
    Properties gradleProperties = new Properties();
    gradleProperties.put(INTERLOK_VERSION, interlokVersion);
    if (VersionUtils.isSnapshot(interlokVersion)) {
      gradleProperties.put(INTERLOK_BASE_FILESYSTEM_URL,
          properties.getBaseFilesystemUrlNightly().replace("${today}", LocalDate.now().minusDays(1).toString()));
    }

    final byte[] bytes = propertiesToBytes(gradleProperties);

    return new GradleItem() {
      @Override
      public byte[] getPayload() {
        return bytes;
      }

      @Override
      public String getName() {
        return "gradle.properties";
      }
    };
  }

  private byte[] propertiesToBytes(Properties properties) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      properties.store(outputStream, "");
      return outputStream.toByteArray();
    }
  }

}
