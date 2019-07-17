package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.config.ScmInformation;
import com.google.common.base.Strings;
import hudson.Extension;
import hudson.model.Run;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class ScmV2NotifierProvider implements NotifierProvider {

  private static final Pattern PATTERN = Pattern.compile("^http(?:s)?://[^/]+(/[A-Za-z0-9.\\-_]+)?/repo/([A-Za-z0-9.\\-_]+)/([A-Za-z0-9.\\-_]+)$");

  private AuthenticationFactory authenticationFactory;

  @Inject
  public void setAuthenticationFactory(AuthenticationFactory authenticationFactory) {
    this.authenticationFactory = authenticationFactory;
  }

  @Override
  public Optional<ScmV2Notifier> get(Run<?, ?>  run, ScmInformation information) throws MalformedURLException {
    ScmV2Notifier notifier = null;

    String url = information.getUrl();
    Matcher matcher = PATTERN.matcher(url);
    if (matcher.matches()) {
      notifier = createNotifier(run, information, url, matcher);
    }
    return Optional.ofNullable(notifier);
  }

  private ScmV2Notifier createNotifier(Run<?, ?> run, ScmInformation information, String url, Matcher matcher) throws MalformedURLException {
    URL instance = createInstanceURL(url, matcher);
    NamespaceAndName namespaceAndName = createNamespaceAndName(matcher);
    Authentication authentication = authenticationFactory.create(run, information.getCredentialsId());

    return new ScmV2Notifier(instance, namespaceAndName, authentication);
  }

  private NamespaceAndName createNamespaceAndName(Matcher matcher) {
    return new NamespaceAndName(matcher.group(2), matcher.group(3));
  }

  private URL createInstanceURL(String stringUrl, Matcher matcher) throws MalformedURLException {
    URL url = new URL(stringUrl);
    return new URL(url.getProtocol(), url.getHost(), url.getPort(), Strings.nullToEmpty(matcher.group(1)));
  }
}
