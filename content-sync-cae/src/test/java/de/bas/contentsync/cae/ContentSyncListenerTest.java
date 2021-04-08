package de.bas.contentsync.cae;

import de.bas.contentsync.beans.ContentSync;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;

/**
 * @author mschwarz
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentSyncListenerTest {
    @Mock
    FutureTask<ContentSync> futureTask;

    @Mock
    ContentSync contentSync;

    @Mock
    ScheduledExecutorService executor;

    @InjectMocks
    ContentSyncListener contentSyncListener = new ContentSyncListener(null, null, null, null);

    @Test
    public void testFutureDate() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 30);
        contentSyncListener.startScheduled(contentSync, futureTask, instance);
        Mockito.verify(executor, times(1)).schedule(any(FutureTask.class), anyLong(), any());
    }

    @Test
    public void testPastDate() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, -30);
        contentSyncListener.startScheduled(contentSync, futureTask, instance);
        Mockito.verify(executor, times(0)).schedule(any(FutureTask.class), anyLong(), any());
    }
}
