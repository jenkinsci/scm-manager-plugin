package com.cloudogu.scmmanager.scm;

import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerWebHook_SourceEventTest {

  private static final String SCM_URL = "http://localhost/scm";

  @Mock
  StaplerRequest request;
  @Mock
  StaplerResponse response;

  JSONObject form = new JSONObject();

  @Spy
  ScmManagerWebHook hook;

  @Before
  public void prepareForm() throws ServletException {
    form.put("server", SCM_URL);
    form.put("eventTarget", "NAVIGATOR");
    when(request.getSubmittedForm()).thenReturn(form);
  }

  @Test
  public void shouldAcceptRequestWithMinimalValues() throws ServletException, IOException {
    HttpResponse httpResponse = hook.doNotify(request);

    httpResponse.generateResponse(request, response, null);
    verify(response).setStatus(200);
  }

  @Test
  public void shouldRejectRequestWithMissingServer() throws ServletException, IOException {
    form.remove("server");

    HttpResponse httpResponse = hook.doNotify(request);

    httpResponse.generateResponse(request, response, null);
    verify(response).sendError(eq(400), anyString());
  }

  @Test
  public void shouldTriggerForGlobalEvent() throws ServletException, IOException {
    HttpResponse httpResponse = hook.doNotify(request);

    httpResponse.generateResponse(request, response, null);
    verify(response).setStatus(200);
    verify(hook).fireNow(sourceEventThat(argument -> {
      assertThat(argument.getPayload().isGlobal()).isTrue();
      assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "hog"))).isTrue();
      assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "guide"))).isTrue();
      assertThat(argument.isMatch(mockNavigator("http://vogon/scm", "space"))).isFalse();
      assertThat(argument.isMatch(mockSource("http://localhost/scm", "X"))).isTrue();
      assertThat(argument.isMatch(mockSource("http://localhost/scm", "earth"))).isTrue();
      assertThat(argument.isMatch(mockSource("http://vogon/scm", "X"))).isFalse();
      return true;
    }));
  }

  @Test
  public void shouldDetectEventWithSsh() throws ServletException, IOException {
    JSONArray identifications = new JSONArray();
    JSONObject identification = new JSONObject();
    identification.put("name", "ssh");
    identification.put("value", "hitchhiker.com:2222");
    identifications.add(identification);
    form.put("identifications", identifications);

    HttpResponse httpResponse = hook.doNotify(request);

    httpResponse.generateResponse(request, response, null);
    verify(response).setStatus(200);
    verify(hook).fireNow(sourceEventThat(argument -> {
      assertThat(argument.getPayload().isGlobal()).isTrue();
      assertThat(argument.isMatch(mockNavigator("ssh://hitchhiker.com:2222/", "hog"))).isTrue();
      assertThat(argument.isMatch(mockNavigator("http://hitchhiker.com/scm", "hog"))).isFalse();
      return true;
    }));
  }

  @Test
  public void shouldTriggerForRepositorySpecificEvent() throws ServletException, IOException {
    form.put("namespace", "hog");
    form.put("name", "drive");

    HttpResponse httpResponse = hook.doNotify(request);

    httpResponse.generateResponse(request, response, null);
    verify(response).setStatus(200);
    verify(hook).fireNow(sourceEventThat(argument -> {
      assertThat(argument.getPayload().isGlobal()).isFalse();
      assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "hog"))).isTrue();
      assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "guide"))).isFalse();
      assertThat(argument.isMatch(mockNavigator("http://vogon/scm", "space"))).isFalse();
      assertThat(argument.isMatch(mockSource("http://localhost/scm", "drive"))).isTrue();
      assertThat(argument.isMatch(mockSource("http://localhost/scm", "earth"))).isFalse();
      assertThat(argument.isMatch(mockSource("http://vogon/scm", "X"))).isFalse();
      return true;
    }));
  }

  private ScmManagerSourceEvent sourceEventThat(ArgumentMatcher<ScmManagerSourceEvent> assertion) {
    return argThat(assertion);
  }

  private SCMNavigator mockNavigator(String serverUrl, String namespace) {
    ScmManagerNavigator mock = mock(ScmManagerNavigator.class);
    when(mock.getServerUrl()).thenReturn(serverUrl);
    when(mock.getNamespace()).thenReturn(namespace);
    return mock;
  }

  private SCMSource mockSource(String serverUrl, String name) {
    ScmManagerSource mock = mock(ScmManagerSource.class);
    when(mock.getServerUrl()).thenReturn(serverUrl);
    when(mock.getName()).thenReturn(name);
    return mock;
  }
}
