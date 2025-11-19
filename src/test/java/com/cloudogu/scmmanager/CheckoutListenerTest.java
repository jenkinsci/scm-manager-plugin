package com.cloudogu.scmmanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudogu.scmmanager.info.JobInformation;
import com.cloudogu.scmmanager.info.ScmInformationService;
import hudson.model.Run;
import hudson.scm.SCM;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ScmInformationService informationService;

    @Mock
    private SCM scm;

    @Mock
    private Run<?, ?> run;

    private CheckoutListener checkoutListener;

    @BeforeEach
    void beforeEach() {
        checkoutListener = new CheckoutListener();
        checkoutListener.setNotificationService(notificationService);
        checkoutListener.setInformationService(informationService);
    }

    @Test
    void testNotificationServiceIsCalled() {
        checkoutListener.onCheckout(run, scm, null, null, null, null);
        verify(notificationService).notify(run, null);
    }

    @Test
    void shouldAddNotificationAction() {
        List<JobInformation> information = new ArrayList<>();
        when(informationService.resolve(run, scm)).thenReturn(information);

        checkoutListener.onCheckout(run, scm, null, null, null, null);

        ArgumentCaptor<NotificationAction> actionCaptor = ArgumentCaptor.forClass(NotificationAction.class);
        verify(run).addAction(actionCaptor.capture());

        NotificationAction action = actionCaptor.getValue();
        assertThat(action.getJobInformation()).isSameAs(information);
    }
}
