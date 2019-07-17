package com.cloudogu.scmmanager;

import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutListenerTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private Run<?, ?> run;

  @Test
  public void testNotificationServiceIsCalled() {
    CheckoutListener checkoutListener = new CheckoutListener();
    checkoutListener.setNotificationService(notificationService);

    checkoutListener.onCheckout(run, null, null, null, null, null);

    verify(notificationService).notify(run, null);
  }

}
