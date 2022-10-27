package com.cloudogu.scmmanager.scm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ServerIdentificationTest {

  @Test
  public void shouldNotThrowNpeIfServerUrlIsNull() {
    ServerIdentification serverIdentification = new ServerIdentification(null, null);

    assertThat(serverIdentification.matches(null)).isFalse();
  }
}
