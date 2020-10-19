package com.enonic.xp.impl.task.distributed;

import org.junit.jupiter.api.Test;

import com.enonic.xp.impl.task.LocalTaskManager;
import com.enonic.xp.task.TaskId;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SingleTaskReporterTest
{
    @Test
    void apply()
    {
        final TaskId someTaskId = TaskId.from( "someTask" );
        final LocalTaskManager taskManager = mock( LocalTaskManager.class );
        new SingleTaskReporter( someTaskId ).apply( taskManager );
        verify( taskManager ).getTaskInfo( eq( someTaskId ) );
    }
}
