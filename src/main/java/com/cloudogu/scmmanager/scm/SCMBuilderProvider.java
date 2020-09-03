package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.Repository;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

import java.util.Collection;

abstract class SCMBuilderProvider implements ExtensionPoint {

  private final String type;

  protected SCMBuilderProvider(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public abstract boolean isSupported(@NonNull SCMHeadCategory category);

  public abstract Collection<SCMSourceTraitDescriptor> getTraitDescriptors(SCMSourceDescriptor sourceDescriptor);

  public static SCMBuilder<?, ?> from(Context context) {
    CloneInformation cloneInformation = context.getHead().getCloneInformation();
    return byType(cloneInformation.getType()).create(context);
  }

  protected abstract SCMBuilder<?,?> create(Context context);

  static SCMBuilderProvider byType(String type) {
    for (SCMBuilderProvider provider : all()) {
      if (provider.getType().equalsIgnoreCase(type)) {
        return provider;
      }
    }
    throw new IllegalStateException("could not find provider for type " + type);
  }

  static ExtensionList<SCMBuilderProvider> all() {
    return Jenkins.get().getExtensionList(SCMBuilderProvider.class);
  }

  static boolean isSupported(Repository repository) {
    for (SCMBuilderProvider provider : all()) {
      if (provider.getType().equalsIgnoreCase(repository.getType())) {
        return true;
      }
    }
    return false;
  }

  static class Context {

    private final LinkBuilder linkBuilder;
    private final ScmManagerHead head;
    private final SCMRevision revision;
    private final String credentialsId;

    public Context(LinkBuilder linkBuilder, ScmManagerHead head, SCMRevision revision, String credentialsId) {
      this.linkBuilder = linkBuilder;
      this.head = head;
      this.revision = revision;
      this.credentialsId = credentialsId;
    }

    public ScmManagerHead getHead() {
      return head;
    }

    public SCMRevision getRevision() {
      return revision;
    }

    public String getCredentialsId() {
      return credentialsId;
    }

    public LinkBuilder getLinkBuilder() {
      return linkBuilder;
    }
  }

}
