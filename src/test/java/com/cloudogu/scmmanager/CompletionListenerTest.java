package com.cloudogu.scmmanager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.model.Result;
import hudson.model.Run;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompletionListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Run<?, ?> run;

    @Test
    void testNotificationServiceIsCalled() {
        CompletionListener listener = new CompletionListener();
        listener.setNotificationService(notificationService);

        Result result = Result.SUCCESS;
        when(run.getResult()).thenReturn(result);

        listener.onCompleted(run, null);

        verify(notificationService).notify(run, result);
    }
}
