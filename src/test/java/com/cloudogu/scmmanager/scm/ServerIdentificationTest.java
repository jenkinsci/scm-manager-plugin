package com.cloudogu.scmmanager.scm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ServerIdentificationTest {

    @Test
    public void shouldNotThrowNullPointerExceptionIfServerUrlIsNull() {
        ServerIdentification serverIdentification = new ServerIdentification(null, null);

        boolean match = serverIdentification.matches(null);

        assertThat(match).isFalse();
    }
}
