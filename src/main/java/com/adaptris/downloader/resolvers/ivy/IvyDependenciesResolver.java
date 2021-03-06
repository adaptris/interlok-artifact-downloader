package com.adaptris.downloader.resolvers.ivy;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.Configuration.Visibility;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.plugins.resolver.RepositoryResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.CredentialsStore;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerDispatcher;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.downloader.resolvers.DependenciesResolver;
import com.adaptris.downloader.resolvers.DependenciesResolverException;
import com.adaptris.downloader.resolvers.DependenciesResolverProperties;
import com.adaptris.downloader.utils.UidGenerator;

public class IvyDependenciesResolver implements DependenciesResolver {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String ASTERISK = "*";
  private static final String[] FILE_EXTENSIONS = new String[] {"jar", "war", "dll", "lic"};


  /*
  <conf name="master" visibility="public" description="contains only the artifact published by this module itself, with no transitive dependencies"/>
  <conf name="compile" visibility="public" description="this is the default scope, used if none is specified. Compile dependencies are available in all classpaths."/>
  <conf name="default" visibility="public" description="runtime dependencies and master artifact can be used with this conf" extends="runtime,master"/>
  <conf name="provided" visibility="public" description="this is much like compile, but indicates you expect the JDK or a container to provide it. It is only available on the compilation classpath, and is not transitive."/>
  <conf name="runtime" visibility="public" description="this scope indicates that the dependency is not required for compilation, but is for execution. It is in the runtime and test classpaths, but not the compile classpath." extends="compile"/>
  <conf name="test" visibility="private" description="this scope indicates that the dependency is not required for normal use of the application, and is only available for the test compilation and execution phases." extends="runtime"/>
  <conf name="system" visibility="public" description="this scope is similar to provided except that you have to provide the JAR which contains it explicitly. The artifact is always available and is not looked up in a repository."/>
  <conf name="sources" visibility="public" description="this configuration contains the source artifact of this module, if any."/>
  <conf name="javadoc" visibility="public" description="this configuration contains the javadoc artifact of this module, if any."/>
  <conf name="optional" visibility="public" description="contains all optional dependencies">
   */

  private static final String CONF_MASTER_NAME = "master";

  private static final Configuration CONF_MASTER = new Configuration(CONF_MASTER_NAME, Visibility.PUBLIC, "Master dependencies",
      new String[] {}, false, null);

  private static final String CONF_COMPILE_NAME = "compile";

  private static final Configuration CONF_COMPILE = new Configuration(CONF_COMPILE_NAME, Visibility.PUBLIC, "Compile dependencies",
      new String[] {}, false, null);

  private static final String CONF_RUNTIME_NAME = "runtime";

  private static final Configuration CONF_RUNTIME = new Configuration(CONF_RUNTIME_NAME, Visibility.PUBLIC, "Compile dependencies",
      new String[] { CONF_COMPILE_NAME }, false, null);

  private static final String CONF_DEFAULT_NAME = "default";

  private static final Configuration CONF_DEFAULT = new Configuration(CONF_DEFAULT_NAME, Visibility.PUBLIC, "Default dependencies",
      new String[] { CONF_RUNTIME_NAME, CONF_MASTER_NAME }, true, null);

  private static final String CONF_PROVIDED_NAME = "provided";

  private static final Configuration CONF_PROVIDED = new Configuration(CONF_PROVIDED_NAME, Visibility.PUBLIC, "Provided dependencies",
      new String[] {}, false, null);

  private static final String CONF_TEST_NAME = "test";

  private static final Configuration CONF_TEST = new Configuration(CONF_TEST_NAME, Visibility.PRIVATE, "Test dependencies",
      new String[] { CONF_RUNTIME_NAME }, false, null);

  private static final String CONF_OPTIONAL_NAME = "optional";

  private static final Configuration CONF_OPTIONAL = new Configuration(CONF_OPTIONAL_NAME, Visibility.PUBLIC, "Optional dependencies",
      new String[] {}, true, null);

  private final DependenciesResolverProperties properties;

  public IvyDependenciesResolver(DependenciesResolverProperties properties) {
    this.properties = properties;
  }

  @Override
  public List<File> resolveArtifacts(String groupId, String artifactId, String version, String repoUrl, String cacheDir, boolean addOptional, String... excludes)
      throws DependenciesResolverException {

    log.info("Trying to resolve artifact {}:{}:{}", groupId, artifactId, version);

    try {
      List<File> dependencyFiles = new ArrayList<>();

      initMessageLogger();

      Ivy ivy = buildIvy();

      IvySettings ivySettings = buildIvySettings(ivy, cacheDir);

      addRepos(repoUrl, ivySettings);

      log.debug(ivySettings.getDefaultResolver().toString());

      String[] confs = new String[] { CONF_DEFAULT_NAME, CONF_COMPILE_NAME };
      if (addOptional) {
        confs = ArrayUtils.add(confs, CONF_OPTIONAL_NAME);
      }

      // Ivy config

      // First create an ivy module (this always(!) has a "default" configuration already)
      DefaultModuleDescriptor moduleDescriptor =
          DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(groupId, artifactId + "-caller", "working"));

      // Add the module configurations
      moduleDescriptor.addConfiguration(CONF_MASTER);
      moduleDescriptor.addConfiguration(CONF_COMPILE);
      moduleDescriptor.addConfiguration(CONF_RUNTIME);
      moduleDescriptor.addConfiguration(CONF_DEFAULT);
      moduleDescriptor.addConfiguration(CONF_PROVIDED);
      moduleDescriptor.addConfiguration(CONF_TEST);
      moduleDescriptor.addConfiguration(CONF_OPTIONAL);

      moduleDescriptor.setDefaultConf(CONF_COMPILE_NAME);
      moduleDescriptor.setDefaultConfMapping(ModuleDescriptor.DEFAULT_CONFIGURATION);

      // Second add dependencies for what we are really looking for
      DefaultDependencyDescriptor dependencyDescriptor =
          new DefaultDependencyDescriptor(moduleDescriptor, ModuleRevisionId.newInstance(groupId, artifactId, version), false, false, true);
      for (int i = 0; i < confs.length; i++) {
        dependencyDescriptor.addDependencyConfiguration(confs[i], confs[i]);
      }
      addExcludes(dependencyDescriptor, excludes);
      moduleDescriptor.addDependency(dependencyDescriptor);


      ResolveOptions resolveOptions = new ResolveOptions().setConfs(confs);

      // Init resolve report
      ResolveReport report = ivy.resolve(moduleDescriptor, resolveOptions);
      handleErrors(report);

      ModuleDescriptor rmd = report.getModuleDescriptor();

      // if (confs.length == 1 && ASTERISK.equals(confs[0])) {
      //   confs = rmd.getConfigurationsNames();
      // }

      File retrievedDir = getRetrievedDir(ivySettings.getDefaultCache());
      String retrievePattern = ivySettings.substitute(retrievedDir.getAbsolutePath() + File.separator + "[artifact](-[classifier]).[ext]");
      RetrieveOptions retrieveOptions = new RetrieveOptions().setConfs(confs).setDestArtifactPattern(retrievePattern);
      ivy.retrieve(rmd.getModuleRevisionId(), retrieveOptions);

      dependencyFiles.addAll(FileUtils.listFiles(retrievedDir, FILE_EXTENSIONS, false));

      return dependencyFiles;
    } catch (IOException | ParseException expts) {
      throw new DependenciesResolverException("Failed to resolve artifacts for " + groupId + ":" + artifactId + ":" + version, expts);
    }
  }

  protected final void initMessageLogger() {
    int ivyLogLevel = IvyLogLevel.valueOrError(properties.getResolverLogLevel()).getLevel();
    Message.setDefaultLogger(new DefaultMessageLogger(ivyLogLevel));
  }

  private Ivy buildIvy() throws ParseException, IOException {
    // Create an Ivy instance
    Ivy ivy = Ivy.newInstance();
    ivy.configureDefault();
    ivy.pushContext();
    return ivy;
  }

  private IvySettings buildIvySettings(Ivy ivy, String cacheDir) {
    // Creates ivy settings
    IvySettings ivySettings = ivy.getSettings();
    ivySettings.addAllVariables(System.getProperties());
    ivySettings.setVariable("ivy.default.configuration.m2compatible", "true");

    // Set to the default cache location
    String localRepositoryString = cacheDir + "-ivy";
    ivySettings.setDefaultCache(new File(localRepositoryString));

    return ivySettings;
  }

  private void configureCredentials(String realm, String host, String username,
      String passwd) {
    if (!CredentialsStore.INSTANCE.hasCredentials(host)) {
      CredentialsStore.INSTANCE.addCredentials(realm, host, username, passwd);

      URLHandlerDispatcher dispatcher = new URLHandlerDispatcher();
      URLHandler httpHandler = URLHandlerRegistry.getHttp();
      dispatcher.setDownloader("http", httpHandler);
      dispatcher.setDownloader("https", httpHandler);
      URLHandlerRegistry.setDefault(dispatcher);
    }
  }

  protected final void addRepos(String repoUrl, IvySettings ivySettings) {
    ChainResolver chainResolver = new ChainResolver();
    chainResolver.setName("chain");
    chainResolver.setCheckmodified(true);

    // Add maven central repo
    addM2Resolver(chainResolver, MAVEN_CENTRAL_BASE_URL, "central");

    if (StringUtils.isNotBlank(repoUrl)) {
      // Add custom repo
      addM2Resolver(chainResolver, repoUrl, "custom");
    }

    // Add interlok repos
    for (String repo : properties.getRepos()) {
      addM2Resolver(chainResolver, properties.getRepoBaseUrl() + repo, repo);
    }

    if (properties.getCredentials() != null) {
      configureCredentials(properties.getCredentials().getRealm(), properties.getCredentials().getHost(),
          properties.getCredentials().getUsername(), properties.getCredentials().getPassword());
    }

    ivySettings.addResolver(chainResolver);
    ivySettings.setDefaultResolver(chainResolver.getName());
  }

  private void addM2Resolver(ChainResolver chainResolver, String url, String name) {
    // URL resolver for configuration of maven repo
    RepositoryResolver interlokDepResolver = buildM2Resolver(url, name);
    // Adding maven repo resolver
    chainResolver.add(interlokDepResolver);
  }

  private RepositoryResolver buildM2Resolver(String url, String name) {
    IBiblioResolver resolver = new IBiblioResolver();
    resolver.setM2compatible(true);
    // resolver.setUseMavenMetadata(true);
    // resolver.setUsepoms(true);
    resolver.setRoot(url);
    resolver.setName(name);
    resolver.setCheckmodified(true);
    // resolver.setForce(true);
    // resolver.setAlwaysCheckExactRevision(true);
    return resolver;
  }

  protected final void addExcludes(DefaultDependencyDescriptor dd, String... excludes) {
    for (String exclude : excludes) {
      String[] split = exclude.split(":");
      String org = StringUtils.trimToEmpty(split[0]);
      String name = ASTERISK;
      if (split.length > 1) {
        name = StringUtils.defaultString(split[1], ASTERISK);
      }
      ArtifactId excludeArtifactId = new ArtifactId(new ModuleId(org, name), ASTERISK, ASTERISK, ASTERISK);
      dd.addExcludeRule(CONF_DEFAULT_NAME, new DefaultExcludeRule(excludeArtifactId, new IvyMavenLikePatternMatcher(), null));
      dd.addExcludeRule(CONF_COMPILE_NAME, new DefaultExcludeRule(excludeArtifactId, new IvyMavenLikePatternMatcher(), null));
      dd.addExcludeRule(CONF_OPTIONAL_NAME, new DefaultExcludeRule(excludeArtifactId, new IvyMavenLikePatternMatcher(), null));
    }
  }

  private File getRetrievedDir(File localRepository) throws IOException {
    File retrievedDir = new File(new File(localRepository, "retrieved"), UidGenerator.getUUID());
    FileUtils.deleteQuietly(retrievedDir);
    retrievedDir.mkdirs();
    FileUtils.forceDeleteOnExit(retrievedDir);
    return retrievedDir;
  }

  protected final void handleErrors(ResolveReport report) throws DependenciesResolverException {
    if (report.hasError()) {
      List<String> dependencyErrors = new ArrayList<>(report.getProblemMessages());

      for (IvyNode ivyNode : report.getUnresolvedDependencies()) {
        String errMsg = ivyNode.getProblemMessage();
        String artifact = artifactToString(ivyNode.getId());
        if (errMsg.length() > 0) {
          dependencyErrors.add("unresolved dependency: " + artifact + ": " + errMsg);
        } else {
          dependencyErrors.add("unresolved dependency: " + artifact);
        }
      }

      for (ArtifactDownloadReport artifactDownloadReport : report.getFailedArtifactsReports()) {
        String artifact = artifactToString(artifactDownloadReport.getArtifact().getModuleRevisionId());
        dependencyErrors.add("download failed: " + artifact);
      }

      throw new DependenciesResolverException(dependencyErrors);
    }
  }

  private String artifactToString(ModuleRevisionId mri) {
    return mri.getOrganisation() + ":" + mri.getName() + ":" + mri.getRevision();
  }

}
