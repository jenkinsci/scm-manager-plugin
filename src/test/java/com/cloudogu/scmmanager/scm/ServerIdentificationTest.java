package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerIdentificationTest {

    @Test
    void shouldNotThrowNullPointerExceptionIfServerUrlIsNull() {
        ServerIdentification serverIdentification = new ServerIdentification(null, null);

        boolean match = serverIdentification.matches(null);

        assertThat(match).isFalse();
    }
}
