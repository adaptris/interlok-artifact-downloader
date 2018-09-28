package com.adaptris.downloader.resolvers.ivy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.adaptris.downloader.config.ArtifactDownloaderCredentialsProperties;
import com.adaptris.downloader.config.ArtifactDownloaderProperties;
import com.adaptris.downloader.resolvers.DependenciesResolverException;

public class IvyDependenciesResolverTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testInitMessageLogger() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    Assert.assertTrue(messageLogger instanceof DefaultMessageLogger);
    Assert.assertEquals(Message.MSG_ERR, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerError() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("error");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    Assert.assertTrue(messageLogger instanceof DefaultMessageLogger);
    Assert.assertEquals(Message.MSG_ERR, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerWarn() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("warn");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    Assert.assertTrue(messageLogger instanceof DefaultMessageLogger);
    Assert.assertEquals(Message.MSG_WARN, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerInfo() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("info");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    Assert.assertTrue(messageLogger instanceof DefaultMessageLogger);
    Assert.assertEquals(Message.MSG_INFO, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerVerbose() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("verbose");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    Assert.assertTrue(messageLogger instanceof DefaultMessageLogger);
    Assert.assertEquals(Message.MSG_VERBOSE, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testInitMessageLoggerDebug() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setResolverLogLevel("debug");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    resolver.initMessageLogger();

    MessageLogger messageLogger = Message.getDefaultLogger();

    Assert.assertTrue(messageLogger instanceof DefaultMessageLogger);
    Assert.assertEquals(Message.MSG_DEBUG, ((DefaultMessageLogger) messageLogger).getLevel());
  }

  @Test
  public void testAddReposOnlyDefault() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setRepoBaseUrl("http://base-repo");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    IvySettings ivySettings = new IvySettings();
    resolver.addRepos(null, ivySettings);

    Assert.assertEquals("chain", ivySettings.getDefaultResolver().getName());
    Assert.assertTrue(ivySettings.getDefaultResolver() instanceof ChainResolver);
    List<?> resolvers = ((ChainResolver) ivySettings.getDefaultResolver()).getResolvers();
    Assert.assertEquals(2, resolvers.size());
    Assert.assertEquals("central", ((DependencyResolver) resolvers.get(0)).getName());
    Assert.assertEquals("public", ((DependencyResolver) resolvers.get(1)).getName());
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

    Assert.assertFalse("This host should not have any credentials yet", CredentialsStore.INSTANCE.hasCredentials("base-repo"));

    IvySettings ivySettings = new IvySettings();
    resolver.addRepos(null, ivySettings);

    Assert.assertEquals("chain", ivySettings.getDefaultResolver().getName());
    Assert.assertTrue(ivySettings.getDefaultResolver() instanceof ChainResolver);
    List<?> resolvers = ((ChainResolver) ivySettings.getDefaultResolver()).getResolvers();
    Assert.assertEquals(2, resolvers.size());
    Assert.assertEquals("central", ((DependencyResolver) resolvers.get(0)).getName());
    Assert.assertEquals("public", ((DependencyResolver) resolvers.get(1)).getName());

    Credentials credentials = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "base-repo");
    Assert.assertNotNull(credentials);
    Assert.assertEquals("username", credentials.getUserName());
    Assert.assertEquals("password", credentials.getPasswd());

    Assert.assertTrue(URLHandlerRegistry.getDefault() instanceof URLHandlerDispatcher);
  }

  @Test
  public void testAddReposCustomRepo() throws DependenciesResolverException {
    ArtifactDownloaderProperties properties = new ArtifactDownloaderProperties();
    properties.setRepoBaseUrl("http://base-repo");
    IvyDependenciesResolver resolver = new IvyDependenciesResolver(properties);

    IvySettings ivySettings = new IvySettings();
    resolver.addRepos("http://custom-repo", ivySettings);

    Assert.assertEquals("chain", ivySettings.getDefaultResolver().getName());
    Assert.assertTrue(ivySettings.getDefaultResolver() instanceof ChainResolver);
    List<?> resolvers = ((ChainResolver) ivySettings.getDefaultResolver()).getResolvers();
    Assert.assertEquals(3, resolvers.size());
    Assert.assertEquals("central", ((DependencyResolver) resolvers.get(0)).getName());
    Assert.assertEquals("custom", ((DependencyResolver) resolvers.get(1)).getName());
    Assert.assertEquals("public", ((DependencyResolver) resolvers.get(2)).getName());
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
    Assert.assertEquals(4, excludeRules.length);
    ModuleId firstRuleModuleId = excludeRules[0].getId().getModuleId();
    Assert.assertEquals("group1", firstRuleModuleId.getOrganisation());
    Assert.assertEquals("artifact1", firstRuleModuleId.getName());

    ModuleId secondRuleModuleId = excludeRules[1].getId().getModuleId();
    Assert.assertEquals("group2", secondRuleModuleId.getOrganisation());
    Assert.assertEquals("*", secondRuleModuleId.getName());

    ModuleId thirdRuleModuleId = excludeRules[2].getId().getModuleId();
    Assert.assertEquals("group3", thirdRuleModuleId.getOrganisation());
    Assert.assertEquals("*", thirdRuleModuleId.getName());

    ModuleId fourthRuleModuleId = excludeRules[3].getId().getModuleId();
    Assert.assertEquals("*", fourthRuleModuleId.getOrganisation());
    Assert.assertEquals("artifact4", fourthRuleModuleId.getName());
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
    Assert.assertEquals(0, excludeRules.length);
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
    assertException();

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

    resolver.handleErrors(report);

    verify(configurationResolveReport).hasError();
    verify(artifact).getModuleRevisionId();
    verify(artifactDownloadReport).getArtifact();
    verify(configurationResolveReport).getArtifactsReports(DownloadStatus.FAILED, true);
    verify(ivyNode).getId();
    verify(configurationResolveReport).getUnresolvedDependencies();
  }

  @Test
  public void testHandleErrorsWithErrorsNoErrorMessage() throws DependenciesResolverException {
    assertException();

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

    resolver.handleErrors(report);

    verify(configurationResolveReport).hasError();
    verify(artifact).getModuleRevisionId();
    verify(artifactDownloadReport).getArtifact();
    verify(configurationResolveReport).getArtifactsReports(DownloadStatus.FAILED, true);
    verify(ivyNode).getId();
    verify(configurationResolveReport).getUnresolvedDependencies();
  }

  private void assertException() {
    expectedEx.expect(DependenciesResolverException.class);
    expectedEx.expectMessage("Dependency resolved with errors.");
    expectedEx.expect(new BaseMatcher<DependenciesResolverException>() {
      @Override
      public boolean matches(Object item) {
        DependenciesResolverException exceptions = (DependenciesResolverException) item;
        return exceptions.getDependencyProblemMessages().size() == 3;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("The DependenciesResolverException should bave 3 dependency problem messages");
      }
    });
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
