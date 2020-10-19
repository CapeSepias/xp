package com.enonic.xp.impl.task.distributed;

import org.junit.jupiter.api.Test;

import com.enonic.xp.impl.task.LocalTaskManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RunningTasksReporterTest
{
    @Test
    void apply()
    {
        final LocalTaskManager taskManager = mock( LocalTaskManager.class );
        new RunningTasksReporter().apply( taskManager );
        verify( taskManager ).getRunningTasks();
    }
}
