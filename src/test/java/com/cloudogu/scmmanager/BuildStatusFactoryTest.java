package com.cloudogu.scmmanager;

import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BuildStatusFactoryTest {

  private static final String URL = "https://oss.cloudogu.com/jenkins";

  @Mock
  private Run run;

  private BuildStatusFactory buildStatusFactory = new BuildStatusFactory();

  @Before
  @SuppressWarnings("unchecked")
  public void prepareRun() {
    ItemGroup group = mock(ItemGroup.class);
    when(group.getFullName()).thenReturn("jenkins");
    Job job = new SimpleJob(group, "scm-manager-plugin");
    when(run.getParent()).thenReturn(job);
    when(run.getUrl()).thenReturn("/job/jenkins/scm-manager-plugin/42");
  }

  @Test
  public void testPending() {
    BuildStatus buildStatus = buildStatusFactory.create(URL, run, null);
    assertStatus(buildStatus, BuildStatus.StatusType.PENDING);
  }

  @Test
  public void testSuccess() {
    BuildStatus buildStatus = buildStatusFactory.create(URL, run, Result.SUCCESS);
    assertStatus(buildStatus, BuildStatus.StatusType.SUCCESS);
  }

  @Test
  public void testFailure() {
    BuildStatus buildStatus = buildStatusFactory.create(URL, run, Result.FAILURE);
    assertStatus(buildStatus, BuildStatus.StatusType.FAILURE);
  }

  @Test
  public void testAborted() {
    BuildStatus buildStatus = buildStatusFactory.create(URL, run, Result.ABORTED);
    assertStatus(buildStatus, BuildStatus.StatusType.ABORTED);
  }

  @Test
  public void testUnknownStatus() {
    BuildStatus buildStatus = buildStatusFactory.create(URL, run, Result.NOT_BUILT);
    assertNull(buildStatus);
  }

  private void assertStatus(BuildStatus buildStatus, BuildStatus.StatusType type) {
    assertEquals("jenkins", buildStatus.getType());
    assertEquals("jenkins/scm-manager-plugin", buildStatus.getName());
    assertEquals(URL + "/job/jenkins/scm-manager-plugin/42", buildStatus.getUrl());
    assertEquals(type, buildStatus.getStatus());
  }

  private static class SimpleJob extends Job {

    SimpleJob(ItemGroup parent, String name) {
      super(parent, name);
    }

    @Override
    public boolean isBuildable() {
      return false;
    }

    @Override
    protected SortedMap _getRuns() {
      return null;
    }

    @Override
    protected void removeRun(Run run) {

    }
  }

}
