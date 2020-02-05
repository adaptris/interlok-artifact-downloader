package com.adaptris.downloader.services;

import java.io.IOException;
import java.util.List;

public interface BuildGradleFileService {

  List<GradleItem> generate(String version, List<String> artifacts) throws IOException;

  public interface GradleItem {
    String getName();

    byte[] getPayload();
  }

}
