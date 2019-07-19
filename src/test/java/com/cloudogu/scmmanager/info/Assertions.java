package com.cloudogu.scmmanager.info;

import static org.junit.Assert.assertEquals;

final class Assertions {

  private Assertions() {
  }

  static void info(ScmInformation info, String type, String rev, String url, String credentials) {
    assertEquals(type, info.getType());
    assertEquals(rev, info.getRevision());
    assertEquals(url, info.getUrl());
    assertEquals(credentials, info.getCredentialsId());
  }
}
