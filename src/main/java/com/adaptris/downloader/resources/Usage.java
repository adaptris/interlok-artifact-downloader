package com.adaptris.downloader.resources;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Usage {

  private String link;

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

}
