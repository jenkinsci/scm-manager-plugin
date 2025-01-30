package com.cloudogu.scmmanager.scm;

import hudson.model.User;
import hudson.scm.ChangeLogSet;

import java.util.Collection;
import java.util.List;

/**
 * <ul>
 *   <li>Git</li>
 *   <li>Subversion (SVN)</li>
 *   <li>Mercurial (Hg)</li>
 * </ul>
 */
public class ScmManagerChangeSet extends ChangeLogSet.Entry {
  @Override
  public String getMsg() {
    return "";
  }

  @Override
  public User getAuthor() {
    return null;
  }

  @Override
  public Collection<String> getAffectedPaths() {
    return List.of();
  }
}
