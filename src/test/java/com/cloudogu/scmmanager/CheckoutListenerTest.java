package com.cloudogu.scmmanager;

import com.cloudogu.scmmanager.info.JobInformation;
import com.cloudogu.scmmanager.info.ScmInformationService;
import hudson.model.Run;
import hudson.scm.SCM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ScmInformationService informationService;

    @Mock
    private SCM scm;

    @Mock
    private Run<?, ?> run;

    private CheckoutListener checkoutListener;

    @Before
    public void setUp() {
        checkoutListener = new CheckoutListener();
        checkoutListener.setNotificationService(notificationService);
        checkoutListener.setInformationService(informationService);
    }

    @Test
    public void testNotificationServiceIsCalled() {
        checkoutListener.onCheckout(run, scm, null, null, null, null);
        verify(notificationService).notify(run, null);
    }

    @Test
    public void shouldAddNotificationAction() {
        List<JobInformation> information = new ArrayList<>();
        when(informationService.resolve(run, scm)).thenReturn(information);

        checkoutListener.onCheckout(run, scm, null, null, null, null);

        ArgumentCaptor<NotificationAction> actionCaptor = ArgumentCaptor.forClass(NotificationAction.class);
        verify(run).addAction(actionCaptor.capture());

        NotificationAction action = actionCaptor.getValue();
        assertThat(action.getJobInformation()).isSameAs(information);
    }

}
