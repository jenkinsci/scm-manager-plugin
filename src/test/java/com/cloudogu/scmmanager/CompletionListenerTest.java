package com.cloudogu.scmmanager;

import hudson.model.Result;
import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompletionListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Run<?, ?> run;

    @Test
    public void testNotificationServiceIsCalled() {
        CompletionListener listener = new CompletionListener();
        listener.setNotificationService(notificationService);

        Result result = Result.SUCCESS;
        when(run.getResult()).thenReturn(result);

        listener.onCompleted(run, null);

        verify(notificationService).notify(run, result);
    }

}
