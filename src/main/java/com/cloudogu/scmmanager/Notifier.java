package com.cloudogu.scmmanager;

public interface Notifier {

  void notify(String revision, BuildStatus buildStatus);

}
