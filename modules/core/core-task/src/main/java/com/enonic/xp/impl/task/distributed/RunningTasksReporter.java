package com.enonic.xp.impl.task.distributed;

import java.util.List;

import com.enonic.xp.impl.task.LocalTaskManager;
import com.enonic.xp.task.TaskInfo;

public final class RunningTasksReporter
    implements SerializableFunction<LocalTaskManager, List<TaskInfo>>
{
    private static final long serialVersionUID = 0;

    @Override
    public List<TaskInfo> apply( final LocalTaskManager taskManager )
    {
        return List.copyOf( taskManager.getRunningTasks() );
    }
}
