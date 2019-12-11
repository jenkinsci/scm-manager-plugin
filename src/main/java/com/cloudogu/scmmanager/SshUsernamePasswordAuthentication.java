package com.cloudogu.scmmanager;

public class SshUsernamePasswordAuthentication implements SSHAuthentication {

  private String username;
  private String password;

  public SshUsernamePasswordAuthentication(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
