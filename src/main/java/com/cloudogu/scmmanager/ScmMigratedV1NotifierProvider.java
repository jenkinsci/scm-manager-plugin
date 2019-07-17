package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.config.ScmInformation;
import hudson.Extension;
import hudson.model.Run;

import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class ScmMigratedV1NotifierProvider implements NotifierProvider {

  private static final Pattern PATTERN = Pattern.compile("^http(?:s)?://[^/]+(/[A-Za-z0-9.\\-_]+)?/(git|hg|svn)/(.*)$");

  private AuthenticationFactory authenticationFactory;

  @Inject
  public void setAuthenticationFactory(AuthenticationFactory authenticationFactory) {
    this.authenticationFactory = authenticationFactory;
  }

  @Override
  public Optional<ScmMigratedV1Notifier> get(Run<?, ?> run, ScmInformation information) {
    ScmMigratedV1Notifier notifier = null;

    Matcher matcher = PATTERN.matcher(information.getUrl());
    if (matcher.matches()) {
      notifier = new ScmMigratedV1Notifier(authenticationFactory, run, information);
    }

    return Optional.ofNullable(notifier);
  }
}
