package com.cloudogu.scmmanager;

import java.io.IOException;

public interface Notifier {

    void notify(String revision, BuildStatus buildStatus) throws IOException;

}
