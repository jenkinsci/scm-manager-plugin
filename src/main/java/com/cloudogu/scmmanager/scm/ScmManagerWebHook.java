package com.cloudogu.scmmanager.scm;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import jenkins.scm.api.SCMHeadEvent;
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
import java.util.Arrays;

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
    SCMHeadEvent.fireNow(
      new ScmManagerHeadEvent(
        form.getString("namespace"),
        form.getString("name"),
        form.getString("type"),
        form.getString("server")));
    return HttpResponses.ok();
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
