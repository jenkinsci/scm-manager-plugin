package com.cloudogu.scmmanager.scm.api;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Namespace extends HalRepresentation implements Serializable {
    private String namespace;

    public Namespace() {}

    public Namespace(String namespace) {
        this.namespace = namespace;
    }

    @VisibleForTesting
    public Namespace(Links links, String namespace) {
        super(links);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Namespace)) return false;

        Namespace namespace1 = (Namespace) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(namespace, namespace1.namespace)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(namespace)
                .toHashCode();
    }
}
