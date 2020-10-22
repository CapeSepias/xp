package com.enonic.xp.impl.task.distributed;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import com.enonic.xp.impl.task.LocalTaskManager;
import com.enonic.xp.impl.task.OsgiSupport;
import com.enonic.xp.task.TaskInfo;

public final class TasksReporterCallable
    implements Callable<List<TaskInfo>>, Serializable
{
    private static final long serialVersionUID = 0;

    private final SerializableFunction<LocalTaskManager, List<TaskInfo>> function;

    public TasksReporterCallable( final SerializableFunction<LocalTaskManager, List<TaskInfo>> function )
    {
        this.function = function;
    }

    @Override
    public List<TaskInfo> call()
    {
        return OsgiSupport.withService( LocalTaskManager.class, function ).orElse( List.of() );
    }
}
