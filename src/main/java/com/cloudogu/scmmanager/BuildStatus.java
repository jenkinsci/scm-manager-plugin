package com.cloudogu.scmmanager;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cistatus")
@XmlAccessorType(XmlAccessType.FIELD)
public final class BuildStatus {

    private String name;
    private String displayName;
    private String url;

    // field name is required for marshaling to json
    @SuppressWarnings("squid:S00115")
    private String type = "jenkins";
    private StatusType status;

    private BuildStatus(String name, String displayName, String url, StatusType status) {
        this.name = name;
        this.displayName = displayName;
        this.url = url;
        this.status = status;
    }

    BuildStatus() {
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public enum StatusType {
        PENDING, ABORTED, UNSTABLE, FAILURE, SUCCESS
    }

    static BuildStatus pending(String name, String displayName, String url) {
        return new BuildStatus(name, displayName, url, StatusType.PENDING);
    }

    static BuildStatus aborted(String name, String displayName, String url) {
        return new BuildStatus(name, displayName, url, StatusType.ABORTED);
    }

    static BuildStatus unstable(String name, String displayName, String url) {
        return new BuildStatus(name, displayName, url, StatusType.UNSTABLE);
    }

    static BuildStatus success(String name, String displayName, String url) {
        return new BuildStatus(name, displayName, url, StatusType.SUCCESS);
    }

    static BuildStatus failure(String name, String displayName, String url) {
        return new BuildStatus(name, displayName, url, StatusType.FAILURE);
    }

}
