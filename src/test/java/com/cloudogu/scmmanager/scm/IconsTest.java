package com.cloudogu.scmmanager.scm;

import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class IconsTest {

    @Test
    public void shouldRegisterIcon() {
        IconSet iconSet = new IconSet();
        String name = "icon-scm-manager-source";
        Icons.register(iconSet, name);
        IconAssertions iconAssertions = new IconAssertions(iconSet, name);
        iconAssertions.assertAllSizes();
    }

    private final static class IconAssertions {

        private final IconSet iconSet;
        private final String name;

        private IconAssertions(IconSet iconSet, String name) {
            this.iconSet = iconSet;
            this.name = name;
        }

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
