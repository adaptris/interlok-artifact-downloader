package com.adaptris.downloader.resolvers.ivy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.util.Credentials;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.MessageLogger;
import org.apache.ivy.util.url.CredentialsStore;
import org.apache.ivy.util.url.URLHandlerDispatcher;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.adaptris.downloader.config.ArtifactDownloaderCredentialsProperties;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.resolvers.DependenciesResolverException;

public class IvyDependenciesResolverTest {

  @Test
  public void testInitMessageLogger() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    assertTrue(messageLogger instanceof DefaultMessageLogger);
    assertEquals(Message.MSG_ERR, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerError() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("error");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    assertTrue(messageLogger instanceof DefaultMessageLogger);
    assertEquals(Message.MSG_ERR, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerWarn() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("warn");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    assertTrue(messageLogger instanceof DefaultMessageLogger);
    assertEquals(Message.MSG_WARN, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerInfo() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("info");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    assertTrue(messageLogger instanceof DefaultMessageLogger);
    assertEquals(Message.MSG_INFO, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerVerbose() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("verbose");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    assertTrue(messageLogger instanceof DefaultMessageLogger);
    assertEquals(Message.MSG_VERBOSE, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerDebug() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("debug");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    assertTrue(messageLogger instanceof DefaultMessageLogger);
    assertEquals(Message.MSG_DEBUG, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testAddReposOnlyDefault() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setRepoBaseUrl("http://base-repo");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    IvySettings ivySettings = new IvySettings();
    resolver.addRepos(null, ivySettings);

    assertEquals("chain", ivySettings.getDefaultResolver().getName());
    assertTrue(ivySettings.getDefaultResolver() instanceof ChainResolver);
    List<?> resolvers = ((ChainResolver) ivySettings.getDefaultResolver()).getResolvers();
    assertEquals(2, resolvers.size());
    assertEquals("central", ((DependencyResolver) resolvers.get(0)).getName());
    assertEquals("public", ((DependencyResolver) resolvers.get(1)).getName());
  }

  @Test
  public void testAddReposOnlyDefaultWithCredentials() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setRepoBaseUrl("http://base-repo");
    ArtifactDownloaderCredentialsProperties credentialsProperties = new ArtifactDownloaderCredentialsProperties();
    credentialsProperties.setRealm("Sonatype Nexus Repository Manager");
    credentialsProperties.setHost("base-repo");
    credentialsProperties.setUsername("username");
    credentialsProperties.setPassword("password");
    properties.setCredentials(credentialsProperties);
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    assertFalse(CredentialsStore.INSTANCE.hasCredentials("base-repo"), "This host should not have any credentials yet");

    IvySettings ivySettings = new IvySettings();
    resolver.addRepos(null, ivySettings);

    assertEquals("chain", ivySettings.getDefaultResolver().getName());
    assertTrue(ivySettings.getDefaultResolver() instanceof ChainResolver);
    List<?> resolvers = ((ChainResolver) ivySettings.getDefaultResolver()).getResolvers();
    assertEquals(2, resolvers.size());
    assertEquals("central", ((DependencyResolver) resolvers.get(0)).getName());
    assertEquals("public", ((DependencyResolver) resolvers.get(1)).getName());

    Credentials credentials = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "base-repo");
    assertNotNull(credentials);
    assertEquals("username", credentials.getUserName());
    assertEquals("password", credentials.getPasswd());

    assertTrue(URLHandlerRegistry.getDefault() instanceof URLHandlerDispatcher);
  }

  @Test
  public void testAddReposCustomRepo() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setRepoBaseUrl("http://base-repo");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    IvySettings ivySettings = new IvySettings();
    resolver.addRepos("http://custom-repo", ivySettings);

    assertEquals("chain", ivySettings.getDefaultResolver().getName());
    assertTrue(ivySettings.getDefaultResolver() instanceof ChainResolver);
    List<?> resolvers = ((ChainResolver) ivySettings.getDefaultResolver()).getResolvers();
    assertEquals(3, resolvers.size());
    assertEquals("central", ((DependencyResolver) resolvers.get(0)).getName());
    assertEquals("custom", ((DependencyResolver) resolvers.get(1)).getName());
    assertEquals("public", ((DependencyResolver) resolvers.get(2)).getName());
  }

  @Test
  public void testAddExcludes() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    DefaultModuleDescriptor moduleDescriptor = DefaultModuleDescriptor
        .newDefaultInstance(ModuleRevisionId.newInstance("group", "artifact-caller", "working"));
    DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(moduleDescriptor,
        ModuleRevisionId.newInstance("group", "artifact", "version"), false, false, true);

    resolver.addExcludes(dependencyDescriptor, "group1:artifact1", "group2", "group3:*", "*:artifact4");

    ExcludeRule[] excludeRules = dependencyDescriptor.getExcludeRules("default");
    assertEquals(4, excludeRules.length);
    ModuleId firstRuleModuleId = excludeRules[0].getId().getModuleId();
    assertEquals("group1", firstRuleModuleId.getOrganisation());
    assertEquals("artifact1", firstRuleModuleId.getName());

    ModuleId secondRuleModuleId = excludeRules[1].getId().getModuleId();
    assertEquals("group2", secondRuleModuleId.getOrganisation());
    assertEquals("*", secondRuleModuleId.getName());

    ModuleId thirdRuleModuleId = excludeRules[2].getId().getModuleId();
    assertEquals("group3", thirdRuleModuleId.getOrganisation());
    assertEquals("*", thirdRuleModuleId.getName());

    ModuleId fourthRuleModuleId = excludeRules[3].getId().getModuleId();
    assertEquals("*", fourthRuleModuleId.getOrganisation());
    assertEquals("artifact4", fourthRuleModuleId.getName());
  }

  @Test
  public void testAddExcludesNoExcludes() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    DefaultModuleDescriptor moduleDescriptor = DefaultModuleDescriptor
        .newDefaultInstance(ModuleRevisionId.newInstance("group", "artifact-caller", "working"));
    DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(moduleDescriptor,
        ModuleRevisionId.newInstance("group", "artifact", "version"), false, false, true);

    resolver.addExcludes(dependencyDescriptor);

    ExcludeRule[] excludeRules = dependencyDescriptor.getExcludeRules("default");
    assertEquals(0, excludeRules.length);
  }

  @Test
  public void testHandleErrorsWithNoError() throws DependenciesResolverException {
    ResolveReport report = buildReport();
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.handleErrors(report);
  }

  @Test
  public void testHandleErrorsWithErrors() throws DependenciesResolverException {
    // Has error
    ConfigurationResolveReport configurationResolveReport = mock(ConfigurationResolveReport.class);
    doReturn(true).when(configurationResolveReport).hasError();

    // Problem message
    ResolveReport report = buildReport();
    report.setProblemMessages(Collections.singletonList("a problem occured"));

    // Failed artifact reoort
    Artifact artifact = mock(Artifact.class);
    doReturn(buildModuleRevisionId()).when(artifact).getModuleRevisionId();
    ArtifactDownloadReport artifactDownloadReport = mock(ArtifactDownloadReport.class);
    doReturn(artifact).when(artifactDownloadReport).getArtifact();
    ArtifactDownloadReport[] artifactDownloadReports = new ArtifactDownloadReport[] {artifactDownloadReport};
    doReturn(artifactDownloadReports).when(configurationResolveReport).getArtifactsReports(DownloadStatus.FAILED, true);

    // Unresolved depedency
    IvyNode ivyNode = mock(IvyNode.class);
    doReturn(ModuleRevisionId.newInstance("com.adaptris", "artifact-caller", "working")).when(ivyNode).getId();
    doReturn("not found").when(ivyNode).getProblemMessage();
    IvyNode[] ivyNodes = new IvyNode[] {ivyNode};
    doReturn(ivyNodes).when(configurationResolveReport).getUnresolvedDependencies();

    report.addReport("default", configurationResolveReport);
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    assertException(() -> {
      resolver.handleErrors(report);
    });

    verify(configurationResolveReport).hasError();
    verify(artifact).getModuleRevisionId();
    verify(artifactDownloadReport, times(2)).getArtifact();
    verify(configurationResolveReport).getArtifactsReports(DownloadStatus.FAILED, true);
    verify(ivyNode).getId();
    verify(configurationResolveReport).getUnresolvedDependencies();
  }

  @Test
  public void testHandleErrorsWithErrorsNoErrorMessage() throws DependenciesResolverException {
    // Has error
    ConfigurationResolveReport configurationResolveReport = mock(ConfigurationResolveReport.class);
    doReturn(true).when(configurationResolveReport).hasError();

    // Problem message
    ResolveReport report = buildReport();
    report.setProblemMessages(Collections.singletonList("a problem occured"));

    // Failed artifact reoort
    Artifact artifact = mock(Artifact.class);
    doReturn(buildModuleRevisionId()).when(artifact).getModuleRevisionId();
    ArtifactDownloadReport artifactDownloadReport = mock(ArtifactDownloadReport.class);
    doReturn(artifact).when(artifactDownloadReport).getArtifact();
    ArtifactDownloadReport[] artifactDownloadReports = new ArtifactDownloadReport[] { artifactDownloadReport };
    doReturn(artifactDownloadReports).when(configurationResolveReport).getArtifactsReports(DownloadStatus.FAILED, true);

    // Unresolved depedency
    IvyNode ivyNode = mock(IvyNode.class);
    doReturn(ModuleRevisionId.newInstance("com.adaptris", "artifact-caller", "working")).when(ivyNode).getId();
    doReturn("").when(ivyNode).getProblemMessage();
    IvyNode[] ivyNodes = new IvyNode[] { ivyNode };
    doReturn(ivyNodes).when(configurationResolveReport).getUnresolvedDependencies();

    report.addReport("default", configurationResolveReport);
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    assertException(() -> {
      resolver.handleErrors(report);
    });

    verify(configurationResolveReport).hasError();
    verify(artifact).getModuleRevisionId();
    verify(artifactDownloadReport, times(2)).getArtifact();
    verify(configurationResolveReport).getArtifactsReports(DownloadStatus.FAILED, true);
    verify(ivyNode).getId();
    verify(configurationResolveReport).getUnresolvedDependencies();
  }

  private void assertException(Executable executable) {
    DependenciesResolverException exception = assertThrows(DependenciesResolverException.class, executable);
    assertEquals("Dependency resolved with errors.", exception.getMessage());
    assertEquals(3, exception.getDependencyProblemMessages().size(),
        "The DependenciesResolverException should bave 3 dependency problem messages");
  }

  private ResolveReport buildReport() {
    DefaultModuleDescriptor moduleDescriptor =
        DefaultModuleDescriptor.newDefaultInstance(buildModuleRevisionId());

    ResolveReport report = new ResolveReport(moduleDescriptor);
    return report;
  }

  private ModuleRevisionId buildModuleRevisionId() {
    return ModuleRevisionId.newInstance("com.adaptris", "artifact", "version");
  }

}
