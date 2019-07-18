package com.cloudogu.scmmanager;

public final class BuildStatus {

  private final String name;
  private final String url;
  private final String type = "jenkins";
  private final StatusType status;

  private BuildStatus(String name, String url, StatusType status) {
    this.name = name;
    this.url = url;
    this.status = status;
  }

  public String getName() {
    return name;
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

  static BuildStatus pending(String name, String url) {
    return new BuildStatus(name, url, StatusType.PENDING);
  }

  static BuildStatus aborted(String name, String url) {
    return new BuildStatus(name, url, StatusType.ABORTED);
  }

  static BuildStatus unstable(String name, String url) {
    return new BuildStatus(name, url, StatusType.UNSTABLE);
  }

  static BuildStatus success(String name, String url) {
    return new BuildStatus(name, url, StatusType.SUCCESS);
  }

  static BuildStatus failure(String name, String url) {
    return new BuildStatus(name, url, StatusType.FAILURE);
  }

}
