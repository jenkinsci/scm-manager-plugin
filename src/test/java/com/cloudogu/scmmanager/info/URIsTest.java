package com.cloudogu.scmmanager.info;

import org.junit.Test;

import static com.cloudogu.scmmanager.info.URIs.normalize;
import static org.assertj.core.api.Assertions.assertThat;

public class URIsTest {

    @Test
    public void shouldNotModifyURI() {
        String[] uris = new String[]{
            "http://hitchhiker.com:8080", "http://hitchhiker.com:8080/path",
            "https://hitchhiker.com:8443", "https://hitchhiker.com:8443/some/path",
            "http://hitchhiker.com:80", "http://hitchhiker.com:80/path",
            "http://hitchhiker.com:443", "http://hitchhiker.com:443/path",
            "ssh://hitchhiker.com:2222", "ssh://hitchhiker.com:2222/path",
            "ssh://hitchhiker.com:22/path", "ssh://hitchhiker.com:22/path"
        };

        for (String uri : uris) {
            assertThat(normalize(uri)).isEqualTo(uri);
        }
    }

    @Test
    public void shouldAddPort() {
        assertThat(normalize("http://hitchhiker.com")).isEqualTo("http://hitchhiker.com:80");
        assertThat(normalize("https://hitchhiker.com")).isEqualTo("https://hitchhiker.com:443");
        assertThat(normalize("ssh://hitchhiker.com")).isEqualTo("ssh://hitchhiker.com:22");
        assertThat(normalize("http://hitchhiker.com/path")).isEqualTo("http://hitchhiker.com:80/path");
        assertThat(normalize("https://hitchhiker.com/path")).isEqualTo("https://hitchhiker.com:443/path");
        assertThat(normalize("ssh://hitchhiker.com/path")).isEqualTo("ssh://hitchhiker.com:22/path");
    }

    @Test
    public void shouldRemoveCredentials() {
        assertThat(normalize("http://trillian@hitchhiker.com:8080")).isEqualTo("http://hitchhiker.com:8080");
        assertThat(normalize("http://trillian:secret@hitchhiker.com:8080")).isEqualTo("http://hitchhiker.com:8080");
        assertThat(normalize("ssh://trillian@hitchhiker.com/path")).isEqualTo("ssh://hitchhiker.com:22/path");
    }

    @Test
    public void shouldRemoveQueryParameters() {
        assertThat(normalize("http://hitchhiker.com:8080/path?a=b")).isEqualTo("http://hitchhiker.com:8080/path");
    }

    @Test
    public void shouldRemoveHashParameters() {
        assertThat(normalize("http://hitchhiker.com:8080/path#abc")).isEqualTo("http://hitchhiker.com:8080/path");
    }

}
