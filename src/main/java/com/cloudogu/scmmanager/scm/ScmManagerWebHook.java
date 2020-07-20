package com.cloudogu.scmmanager.scm;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import jenkins.scm.api.SCMHeadEvent;
import net.sf.json.JSONArray;
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
  public HttpResponse doNotify(StaplerRequest req) throws ServletException {
    JSONObject form = req.getSubmittedForm();
    if (!verifyParameters(form, "namespace", "name", "type", "server")) {
      return HttpResponses.errorWithoutStack(400, "requires values for 'namespace', 'name', 'type', 'server'");
    }
    fireIfPresent(form, "deletedBranches", names -> new ScmManagerBranchDeletedEvent(form, names));
    fireIfPresent(form, "createdOrModifiedBranches", names -> new ScmManagerBranchUpdatedEvent(form, names));
    fireIfPresent(form, "deletedTags", names -> new ScmManagerTagDeletedEvent(form, names));
    fireIfPresent(form, "createOrModifiedTags", names -> new ScmManagerTagUpdatedEvent(form, names));
    return HttpResponses.ok();
  }

  void fireIfPresent(JSONObject form, String arrayName, Function<Collection<String>, ScmManagerHeadEvent> eventProvider) {
    if (form.containsKey(arrayName)) {
      JSONArray array = form.getJSONArray(arrayName);
      if (!array.isEmpty()) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < array.size(); ++i) {
          names.add(array.getString(i));
        }
        SCMHeadEvent.fireNow(eventProvider.apply(names));
      }
    }
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
