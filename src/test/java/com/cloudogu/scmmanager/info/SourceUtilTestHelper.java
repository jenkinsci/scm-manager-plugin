package com.cloudogu.scmmanager.info;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cloudogu.scmmanager.scm.ScmManagerSource;
import hudson.model.Run;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class SourceUtilTestHelper {

    private SourceUtilTestHelper() {}

    static void mockSource(Run<TestJob, TestRun> run, String... urls) {
        TestJob job = mock(TestJob.class);
        TestSCMSourceOwner sourceOwner = mock(TestSCMSourceOwner.class);

        doReturn(job).when(run).getParent();
        doReturn(sourceOwner).when(job).getParent();

        List<ScmManagerSource> sources =
                Arrays.stream(urls).map(SourceUtilTestHelper::createSource).collect(Collectors.toList());
        doReturn(sources).when(sourceOwner).getSCMSources();
    }

    private static ScmManagerSource createSource(String url) {
        ScmManagerSource scmSource = mock(ScmManagerSource.class);
        doReturn(url).when(scmSource).getRemoteUrl();
        return scmSource;
    }
}
