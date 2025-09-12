package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScmManagerWebHook_SourceEventTest {

    private static final String SCM_URL = "http://localhost/scm";

    @Mock
    private StaplerRequest request;

    @Mock
    private StaplerResponse response;

    private final JSONObject form = new JSONObject();

    @Spy
    private ScmManagerWebHook hook;

    @Captor
    private ArgumentCaptor<ScmManagerSourceEvent> sourceEventCaptor;

    @BeforeEach
    void beforeEach() throws ServletException {
        form.put("server", SCM_URL);
        form.put("eventTarget", "NAVIGATOR");
        when(request.getSubmittedForm()).thenReturn(form);
        doNothing().when(hook).fireNow(sourceEventCaptor.capture());
    }

    @Test
    void shouldAcceptRequestWithMinimalValues() throws ServletException, IOException {
        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
    }

    @Test
    void shouldRejectRequestWithMissingServer() throws ServletException, IOException {
        form.remove("server");

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).sendError(eq(400), anyString());
    }

    @Test
    void shouldTriggerForGlobalEvent() throws ServletException, IOException {
        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);

        verify(hook).fireNow(sourceEventThat(argument -> {
            assertThat(argument.getPayload().isGlobal()).isTrue();
            assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "hog")))
                    .isTrue();
            assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "guide")))
                    .isTrue();
            assertThat(argument.isMatch(mockNavigator("http://vogon/scm", "space")))
                    .isFalse();
            assertThat(argument.isMatch(mockSource("http://localhost/scm", "X")))
                    .isTrue();
            assertThat(argument.isMatch(mockSource("http://localhost/scm", "earth")))
                    .isTrue();
            assertThat(argument.isMatch(mockSource("http://vogon/scm", "X"))).isFalse();
            return true;
        }));
    }

    @Test
    void shouldDetectEventWithSsh() throws ServletException, IOException {
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
            assertThat(argument.isMatch(mockNavigator("ssh://hitchhiker.com:2222/", "hog")))
                    .isTrue();
            assertThat(argument.isMatch(mockNavigator("http://hitchhiker.com/scm", "hog")))
                    .isFalse();
            return true;
        }));
    }

    @Test
    void shouldTriggerForRepositorySpecificEvent() throws ServletException, IOException {
        form.put("namespace", "hog");
        form.put("name", "drive");

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);

        assertThat(sourceEventCaptor.getAllValues()).hasSize(2);
        assertThat(sourceEventCaptor.getAllValues().get(0)).matches(argument -> {
            assertThat(argument.getPayload().isGlobal()).isFalse();
            assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "hog")))
                    .isTrue();
            assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "guide")))
                    .isFalse();
            assertThat(argument.isMatch(mockNavigator("http://vogon/scm", "space")))
                    .isFalse();
            assertThat(argument.isMatch(mockSource("http://localhost/scm", "drive")))
                    .isTrue();
            assertThat(argument.isMatch(mockSource("http://localhost/scm", "earth")))
                    .isFalse();
            assertThat(argument.isMatch(mockSource("http://vogon/scm", "X"))).isFalse();
            return true;
        });
        assertThat(sourceEventCaptor.getAllValues().get(1)).matches(argument -> {
            assertThat(argument.getPayload().isGlobal()).isFalse();
            assertThat(argument.isMatch(mockNavigator("http://localhost/scm", "--all--")))
                    .isTrue();
            assertThat(argument.isMatch(mockSource("http://localhost/scm", "drive")))
                    .isTrue();
            assertThat(argument.isMatch(mockSource("http://localhost/scm", "earth")))
                    .isFalse();
            assertThat(argument.isMatch(mockSource("http://vogon/scm", "X"))).isFalse();
            return true;
        });
    }

    private ScmManagerSourceEvent sourceEventThat(ArgumentMatcher<ScmManagerSourceEvent> assertion) {
        return argThat(assertion);
    }

    private SCMNavigator mockNavigator(String serverUrl, String namespace) {
        ScmManagerNavigator mock = mock(ScmManagerNavigator.class);
        when(mock.getServerUrl()).thenReturn(serverUrl);
        when(mock.isForNamespace(namespace)).thenReturn(true);
        return mock;
    }

    private SCMSource mockSource(String serverUrl, String name) {
        ScmManagerSource mock = mock(ScmManagerSource.class);
        when(mock.getServerUrl()).thenReturn(serverUrl);
        when(mock.getName()).thenReturn(name);
        return mock;
    }
}
