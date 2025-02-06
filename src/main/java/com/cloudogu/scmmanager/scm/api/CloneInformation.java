package com.cloudogu.scmmanager.scm.api;

import java.io.Serializable;
import java.util.Objects;

public class CloneInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String type;
    private final String url;

    public CloneInformation(String type, String url) {
        this.type = type;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloneInformation that = (CloneInformation) o;
        return type.equals(that.type) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, url);
    }
}
