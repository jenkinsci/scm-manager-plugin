package com.cloudogu.scmmanager.scm.api;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class Repository extends HalRepresentation implements Serializable {

    private String namespace;
    private String name;
    private String type;

    private CloneInformation cloneInformation;

    public Repository() {}

    public Repository(String namespace, String name, String type) {
        this.namespace = namespace;
        this.name = name;
        this.type = type;
    }

    @VisibleForTesting
    public Repository(String namespace, String name, String type, Links links) {
        super(links);
        this.namespace = namespace;
        this.name = name;
        this.type = type;
    }

    @VisibleForTesting
    public Repository(String namespace, String name, String type, Embedded embedded) {
        super(null, embedded);
        this.namespace = namespace;
        this.name = name;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getUrl(String protocol) {
        return getLinks()
                .getLinkBy("protocol", l -> protocol.equals(l.getName()))
                .map(Link::getHref);
    }

    public String mustGetUrl(String protocol) {
        return getUrl(protocol)
                .orElseThrow(() -> new IllegalStateException("could not find protocol link of type " + protocol));
    }

    public CloneInformation getCloneInformation(String protocol) {
        if (cloneInformation == null) {
            cloneInformation = new CloneInformation(type, mustGetUrl(protocol));
        }
        return cloneInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Repository that = (Repository) o;
        return Objects.equals(namespace, that.namespace)
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(cloneInformation, that.cloneInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), namespace, name, type, cloneInformation);
    }
}
