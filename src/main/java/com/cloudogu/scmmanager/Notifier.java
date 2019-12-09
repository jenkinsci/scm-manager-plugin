package com.cloudogu.scmmanager;

import com.jcraft.jsch.JSchException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface Notifier {

  void notify(String revision, BuildStatus buildStatus) throws IOException, JSchException, JAXBException;

}
