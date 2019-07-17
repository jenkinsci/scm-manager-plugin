package com.cloudogu.scmmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.UserCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Run;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationFactoryTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  @Mock
  private Run<?, ?> run;

  private final AuthenticationFactory authenticationFactory = new AuthenticationFactory();

  @Test
  public void testCreateWithoutCredentialsId() {
    Authentication authentication = authenticationFactory.create(run, null);
    assertSame(AuthenticationFactory.NOOP_AUTHENTICATION, authentication);
  }

  @Test
  public void testCreateWithEmptyCredentialsId() {
    Authentication authentication = authenticationFactory.create(run, "");
    assertSame(AuthenticationFactory.NOOP_AUTHENTICATION, authentication);
  }

  @Test
  public void testCreateWithNonExistingCredentials() {
    Authentication authentication = authenticationFactory.create(run, "scm-two");
    assertSame(AuthenticationFactory.NOOP_AUTHENTICATION, authentication);
  }

  @Test
  public void testCreate() throws IOException {
    addCredential("scm-one", "trillian", "secret");

    Authentication authentication = authenticationFactory.create(run, "scm-one");
    assertThat(authentication, CoreMatchers.instanceOf(BasicAuthentication.class));

    BasicAuthentication basic = (BasicAuthentication) authentication;
    assertEquals("trillian", basic.getUsername());
    assertEquals("secret", basic.getPassword().getPlainText());
  }

  private void addCredential(String id, String username, String password) throws IOException {
    Credentials c = new UsernamePasswordCredentialsImpl(
      CredentialsScope.GLOBAL, id, "description", username, password
    );
    SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
    instance.getCredentials().add(c);
    instance.save();
  }

}
