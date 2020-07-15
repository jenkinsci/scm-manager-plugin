package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.CloneInformation;
import com.cloudogu.scmmanager.scm.api.ScmManagerHead;
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
    SCMHeadEvent.fireNow(new ScmManagerHeadEvent());

    // check if the event payload at least provides some proof of origin
    // this may be a query parameter or a HTTP header
    // if the proof of origin is missing, drop the event on the floor and return

    // extract the payload from the request
    // parse the payload
        /* PSEUDOCODE
        for (event : payload) {
            switch (eventType) {
                case HEAD:
                    SCMHeadEvent.fireNow(new MySCMHeadEvent(eventType, payload, SCMEvent.originOf(req));
                    break;
                case SOURCE:
                    SCMHeadEvent.fireNow(new MySCMSourceEvent(eventType, payload, SCMEvent.originOf(req));
                    break;
                case NAVIGATOR:
                    SCMHeadEvent.fireNow(new MySCMNavigatorEvent(eventType, payload, SCMEvent.originOf(req));
                    break;
            }
        }
        */
    return HttpResponses.ok();
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
