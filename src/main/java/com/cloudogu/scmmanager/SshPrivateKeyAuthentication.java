package com.cloudogu.scmmanager;

public class SshPrivateKeyAuthentication implements SSHAuthentication {

  private String username;
  private String privateKey;

  public SshPrivateKeyAuthentication(String username, String privateKey) {
    this.username = username;
    this.privateKey = privateKey;
  }

  public String getUsername() {
    return username;
  }

  public String getPrivateKey() {
    return privateKey;
  }
}
