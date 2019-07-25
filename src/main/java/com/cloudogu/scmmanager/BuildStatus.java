package com.cloudogu.scmmanager;

public final class BuildStatus {

  private final String name;
  private final String displayName;
  private final String url;

  // field name is required for marshaling to json
  @SuppressWarnings("squid:S00115")
  private static final String type = "jenkins";
  private final StatusType status;

  private BuildStatus(String name, String displayName, String url, StatusType status) {
    this.name = name;
    this.displayName = displayName;
    this.url = url;
    this.status = status;
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
