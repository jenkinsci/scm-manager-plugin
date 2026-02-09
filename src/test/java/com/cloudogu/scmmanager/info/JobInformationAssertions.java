package com.cloudogu.scmmanager.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class JobInformationAssertions {

    private JobInformationAssertions() {}

    static void info(JobInformation info, String type, String rev, String url, String credentials) {
        assertEquals(type, info.getType());
        assertEquals(rev, info.getRevision());
        assertEquals(url, info.getUrl());
        assertEquals(credentials, info.getCredentialsId());
    }
}
