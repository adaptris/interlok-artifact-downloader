package com.adaptris.downloader.resources;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ArtifactAndDependendencies {

  private String artifact;

  private List<String> dependencies = new ArrayList<>();

  public String getArtifact() {
    return artifact;
  }

  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<String> dependencies) {
    this.dependencies = dependencies;
  }

  public void addDependency(String dependency) {
    dependencies.add(dependency);
  }

}
