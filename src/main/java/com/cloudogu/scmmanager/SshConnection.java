package com.cloudogu.scmmanager;

import com.trilead.ssh2.Connection;

public class SshConnection {

  private final Connection connection;
  private final NamespaceAndName repository;

  SshConnection(Connection connection, NamespaceAndName repository) {
    this.connection = connection;
    this.repository = repository;
  }

  public Connection getConnection() {
    return connection;
  }

  public NamespaceAndName getRepository() {
    return repository;
  }
}
