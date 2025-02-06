package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerPullRequestHead;
import com.cloudogu.scmmanager.scm.api.ScmManagerTag;
import jenkins.scm.api.SCMHead;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmManagerWebHook_HeadEventTest {

    @Mock
    StaplerRequest request;
    @Mock
    StaplerResponse response;

    JSONObject form = new JSONObject();

    @Spy
    ScmManagerWebHook hook;
    @Captor
    ArgumentCaptor<ScmManagerHeadEvent> scmManagerHeadEventCaptor;

    @Before
    public void prepareForm() throws ServletException {
        form.put("namespace", "space");
        form.put("name", "X");
        form.put("type", "git");
        form.put("server", "http://localhost/scm");
        when(request.getSubmittedForm()).thenReturn(form);
        doNothing().when(hook).fireNow(scmManagerHeadEventCaptor.capture());
    }

    @Test
    public void shouldAcceptRequestWithMinimalValues() throws ServletException, IOException {
        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
    }

    @Test
    public void shouldRejectRequestWithMissingNamespace() throws ServletException, IOException {
        form.remove("namespace");

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).sendError(eq(400), anyString());
    }

    @Test
    public void shouldRejectRequestWithMissingName() throws ServletException, IOException {
        form.remove("name");

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).sendError(eq(400), anyString());
    }

    @Test
    public void shouldRejectRequestWithMissingType() throws ServletException, IOException {
        form.remove("type");

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).sendError(eq(400), anyString());
    }

    @Test
    public void shouldRejectRequestWithMissingServer() throws ServletException, IOException {
        form.remove("server");

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).sendError(eq(400), anyString());
    }

    @Test
    public void shouldTriggerForDeletedBranches() throws ServletException, IOException {
        JSONObject branch = new JSONObject();
        branch.put("name", "feature");
        form.put("deletedBranches", array(branch));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
        verify(hook).fireNow(headEventThat(argument -> {
            Collection<SCMHead> heads = argument.heads(new CloneInformation("git", ""));
            assertThat(heads).hasSize(1);
            assertThat(heads).first().isExactlyInstanceOf(ScmManagerHead.class);
            assertThat(heads).extracting("name").containsExactly("feature");
            return true;
        }));
    }

    @Test
    public void shouldTriggerForCreatedOrModifiedBranches() throws ServletException, IOException {
        JSONObject branch = new JSONObject();
        branch.put("name", "develop");
        form.put("createdOrModifiedBranches", array(branch));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
        verify(hook).fireNow(headEventThat(argument -> {
            Collection<SCMHead> heads = argument.heads(new CloneInformation("git", ""));
            assertThat(heads).hasSize(1);
            assertThat(heads).first().isExactlyInstanceOf(ScmManagerHead.class);
            assertThat(heads).extracting("name").containsExactly("develop");
            return true;
        }));
    }

    @Test
    public void shouldTriggerSourceEventForCreatedOrModifiedBranches() throws ServletException, IOException {
        JSONObject branch = new JSONObject();
        branch.put("name", "develop");
        form.put("createdOrModifiedBranches", array(branch));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
        verify(hook, times(2)).fireNow(sourceEventThat(argument -> {
            assertThat(argument.getPayload().isGlobal()).isFalse();
            return true;
        }));
    }

    @Test
    public void shouldTriggerForDeletedTags() throws ServletException, IOException {
        JSONObject tag = new JSONObject();
        tag.put("name", "0.0.1");
        form.put("deletedTags", array(tag));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
        verify(hook).fireNow(headEventThat(argument -> {
            Collection<SCMHead> heads = argument.heads(new CloneInformation("git", ""));
            assertThat(heads).hasSize(1);
            assertThat(heads).first().isExactlyInstanceOf(ScmManagerTag.class);
            assertThat(heads).extracting("name").containsExactly("0.0.1");
            return true;
        }));
    }

    @Test
    public void shouldTriggerForCreatedOrModifiedTags() throws ServletException, IOException {
        JSONObject tag = new JSONObject();
        tag.put("name", "1.0.0");
        form.put("createOrModifiedTags", array(tag));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);
        verify(hook).fireNow(headEventThat(argument -> {
            Collection<SCMHead> heads = argument.heads(new CloneInformation("git", ""));
            assertThat(heads).hasSize(1);
            assertThat(heads).first().isExactlyInstanceOf(ScmManagerTag.class);
            assertThat(heads).extracting("name").containsExactly("1.0.0");
            return true;
        }));
    }

    @Test
    public void shouldTriggerForDeletedPullRequests() throws ServletException, IOException {
        JSONObject pullRequest = new JSONObject();
        pullRequest.put("id", "42");
        pullRequest.put("source", "feature");
        pullRequest.put("target", "develop");
        form.put("deletedPullRequests", array(pullRequest));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);

        List<SCMHead> heads = scmManagerHeadEventCaptor.getAllValues().stream().flatMap(event -> event.heads(new CloneInformation("git", "")).stream()).collect(Collectors.toList());
        assertThat(heads).hasSize(2);

        assertThat(heads).first().isExactlyInstanceOf(ScmManagerPullRequestHead.class);
        assertThat(heads).first().extracting("id").isEqualTo("42");
        assertThat(heads).first().extracting("source.name").isEqualTo("feature");
        assertThat(heads).first().extracting("target.name").isEqualTo("develop");

        assertThat(heads).element(1).extracting("name").isEqualTo("feature");
    }

    @Test
    public void shouldTriggerForCreatedPullRequests() throws ServletException, IOException {
        JSONObject pullRequest = new JSONObject();
        pullRequest.put("id", "42");
        pullRequest.put("source", "feature");
        pullRequest.put("target", "develop");
        form.put("createOrModifiedPullRequests", array(pullRequest));

        HttpResponse httpResponse = hook.doNotify(request);

        httpResponse.generateResponse(request, response, null);
        verify(response).setStatus(200);

        List<SCMHead> heads = scmManagerHeadEventCaptor.getAllValues().stream().flatMap(event -> event.heads(new CloneInformation("git", "")).stream()).collect(Collectors.toList());
        assertThat(heads).hasSize(2);

        assertThat(heads).first().isExactlyInstanceOf(ScmManagerPullRequestHead.class);
        assertThat(heads).first().extracting("id").isEqualTo("42");
        assertThat(heads).first().extracting("source.name").isEqualTo("feature");
        assertThat(heads).first().extracting("target.name").isEqualTo("develop");

        assertThat(heads).element(1).extracting("name").isEqualTo("feature");
    }

    private ScmManagerHeadEvent headEventThat(ArgumentMatcher<ScmManagerHeadEvent> assertion) {
        return argThat(assertion);
    }

    private ScmManagerSourceEvent sourceEventThat(ArgumentMatcher<ScmManagerSourceEvent> assertion) {
        return argThat(assertion);
    }

    private JSONArray array(JSONObject branch) {
        JSONArray array = new JSONArray();
        array.add(branch);
        return array;
    }
}
