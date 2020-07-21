package com.cloudogu.scmmanager.scm;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import jenkins.scm.api.SCMHeadEvent;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Extension
public class ScmManagerWebHook implements UnprotectedRootAction {
  public static final String URL_NAME = "scm-manager-hook";
  public static final String ENDPOINT = "notify";

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @Override
  public String getUrlName() {
    return URL_NAME;
  }

  @RequirePOST
  public HttpResponse doNotify(StaplerRequest request) throws ServletException {
    JSONObject form = request.getSubmittedForm();
    if (!verifyParameters(form, "namespace", "name", "type", "server")) {
      return HttpResponses.errorWithoutStack(400, "requires values for 'namespace', 'name', 'type', 'server'");
    }
    fireIfPresent(form, "deletedBranches", branches -> new ScmManagerBranchDeletedEvent(form, branches));
    fireIfPresent(form, "createdOrModifiedBranches", branches -> new ScmManagerBranchUpdatedEvent(form, branches));
    fireIfPresent(form, "deletedTags", tags -> new ScmManagerTagDeletedEvent(form, tags));
    fireIfPresent(form, "createOrModifiedTags", tags -> new ScmManagerTagUpdatedEvent(form, tags));
    fireIfPresent(form, "deletedPullRequests", pullRequests -> new ScmManagerPullRequestDeletedEvent(form, pullRequests));
    fireIfPresent(form, "createOrModifiedPullRequests", pullRequests -> new ScmManagerPullRequestUpdatedEvent(form, pullRequests));
    return HttpResponses.ok();
  }

  void fireIfPresent(JSONObject form, String arrayName, Function<Collection<JSONObject>, ScmManagerHeadEvent> eventProvider) {
    if (form.containsKey(arrayName)) {
      JSONArray array = form.optJSONArray(arrayName); // we have to use optJSONArray, because we can have null values
      if (array != null && !array.isEmpty()) {
        List<JSONObject> objects = new ArrayList<>();
        for (int i = 0; i < array.size(); ++i) {
          objects.add(array.getJSONObject(i));
        }
        fireNow(eventProvider.apply(objects));
      }
    }
  }

  @VisibleForTesting
  void fireNow(ScmManagerHeadEvent event) {
    SCMHeadEvent.fireNow(event);
  }

  private boolean verifyParameters(JSONObject form, String... keys) {
    return Arrays.stream(keys).allMatch(form::containsKey);
  }

  @Extension
  public static class CrumbExclusionImpl extends CrumbExclusion {
    public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
      String pathInfo = req.getPathInfo();
      if (pathInfo != null && pathInfo.equals("/" + URL_NAME + "/" + ENDPOINT)) {
        chain.doFilter(req, resp);
        return true;
      } else {
        return false;
      }
    }
  }
}
