package com.cloudogu.scmmanager.scm;

import static org.assertj.core.api.Assertions.assertThat;

import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.junit.jupiter.api.Test;

class IconsTest {

    @Test
    void shouldRegisterIcon() {
        IconSet iconSet = new IconSet();
        String name = "icon-scm-manager-source";
        Icons.register(iconSet, name);
        IconAssertions iconAssertions = new IconAssertions(iconSet, name);
        iconAssertions.assertAllSizes();
    }

    private record IconAssertions(IconSet iconSet, String name) {

        void assertAllSizes() {
            for (Icons.Size size : Icons.Size.values()) {
                assertSize(size);
            }
        }

        void assertSize(Icons.Size size) {
            Icon icon = iconSet.getIconByClassSpec(name + " " + size.getClassName());
            assertThat(icon).isNotNull();
            assertThat(icon.getUrl()).contains(size.getId());
        }
    }
}
