package com.cloudogu.scmmanager.scm;

import com.google.common.annotations.VisibleForTesting;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;

public final class Icons {

    private static final String URL_PATTERN = "plugin/scm-manager/images/%s/%s.png";

    private Icons() {
    }

    private static Icon icon(String name, Size size) {
        return new Icon(
            name + " " + size.className,
            String.format(URL_PATTERN, size.id, name),
            size.style
        );
    }

    public static void register(String name) {
        register(IconSet.icons, name);
    }

    @VisibleForTesting
    static void register(IconSet iconSet, String name) {
        for (Size size : Size.values()) {
            iconSet.addIcon(icon(name, size));
        }
    }

    public enum Size {
        SMALL("16x16", "icon-sm", Icon.ICON_SMALL_STYLE),
        MEDIUM("24x24", "icon-md", Icon.ICON_MEDIUM_STYLE),
        LARGE("32x32", "icon-lg", Icon.ICON_LARGE_STYLE),
        XLARGE("48x48", "icon-xlg", Icon.ICON_XLARGE_STYLE);

        private final String id;
        private final String className;
        private final String style;

        Size(String id, String className, String style) {
            this.id = id;
            this.className = className;
            this.style = style;
        }

        public String getId() {
            return id;
        }

        public String getClassName() {
            return className;
        }

        public String getStyle() {
            return style;
        }
    }

}
