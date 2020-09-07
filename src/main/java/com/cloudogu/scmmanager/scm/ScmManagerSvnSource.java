package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerApi;
import de.otto.edison.hal.Link;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import jenkins.scm.impl.subversion.SubversionSCMSource;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Optional;

public class ScmManagerSvnSource extends SubversionSCMSource {

  private final String serverUrl;
  private final String repository;

  @DataBoundConstructor
  public ScmManagerSvnSource(String id, String serverUrl, String repository, String credentialsId) {
    super(id, repository);
    this.setCredentialsId(credentialsId);
    this.serverUrl = serverUrl;
    this.repository = repository;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public String getRepository() {
    return repository;
  }

  @Extension(optional = true)
  @Symbol("scm-manager-svn")
  public static class DescriptorImpl extends ScmManagerSourceDescriptor {

    public static final String DEFAULT_INCLUDES = "trunk,branches/*,tags/*,sandbox/*";
    public static final String DEFAULT_EXCLUDES = "";

    public DescriptorImpl() {
      super(ScmManagerApi::create, r -> "svn".equals(r.getType()));
    }


    @Override
    protected ListBoxModel.Option createRepositoryOption(Repository repository) {
      String name = repository.getNamespace() + "/" + repository.getName();
      Optional<Link> protocol = repository.getLinks().getLinkBy("protocol", l -> "http".equals(l.getName()));
      return protocol.map(link -> new ListBoxModel.Option(name, link.getHref())).orElse(null);
    }

    @NonNull
    @Override
    public String getDisplayName() {
      return "SCM-Manager (svn)";
    }

    @NonNull
    @Override
    protected SCMHeadCategory[] createCategories() {
      return new SCMHeadCategory[]{
        UncategorizedSCMHeadCategory.DEFAULT
      };
    }
  }

}
