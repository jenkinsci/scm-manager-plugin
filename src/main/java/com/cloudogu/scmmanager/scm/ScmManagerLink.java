package com.cloudogu.scmmanager.scm;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.JellyContext;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.stapler.Stapler;

import java.util.Objects;

public class ScmManagerLink implements Action, IconSpec {

    @NonNull
    private final String iconClassName;
    @NonNull
    private final String url;

    public ScmManagerLink(@NonNull String iconClassName, @NonNull String url) {
        this.iconClassName = iconClassName;
        this.url = url;
    }

    @Override
    public String getIconFileName() {
        String className = getIconClassName();
        if (className != null) {
            Icon icon = IconSet.icons.getIconByClassSpec(className + " icon-md");
            if (icon != null) {
                JellyContext ctx = new JellyContext();
                ctx.setVariable("resURL", Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH);
                return icon.getQualifiedUrl(ctx);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "SCM-Manager";
    }

    @NonNull
    @Override
    public String getUrlName() {
        return url;
    }

    @Override
    public String getIconClassName() {
        return iconClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScmManagerLink that = (ScmManagerLink) o;
        return iconClassName.equals(that.iconClassName) &&
            url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconClassName, url);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("iconClassName", iconClassName)
            .add("url", url)
            .toString();
    }
}
