package com.adaptris.downloader.controllers;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public abstract class AbstractController {

  protected static final String APPLICATION_ZIP = "application/zip";
  protected static final String VERSION = "version";
  protected static final String VERSION_DESC = "Artifact version";

  protected final Response buildZipResponse(byte[] byteArray, String name) {
    return Response.ok(byteArray).type(APPLICATION_ZIP)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".zip\"").build();
  }

}
