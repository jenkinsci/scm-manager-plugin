package com.cloudogu.scmmanager;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

public interface Notifier {

  void notify(String revision, BuildStatus buildStatus) throws IOException, JSchException;

}
