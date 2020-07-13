package com.cloudogu.scmmanager.scm.api;

import java.util.Date;

public class Changeset {

  private String id;
  private Date date;

  Changeset() {
  }

  public String getId() {
    return id;
  }

  public Date getDate() {
    return date;
  }
}
