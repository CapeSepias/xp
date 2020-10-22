package com.enonic.xp.impl.task.distributed;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.task.OsgiSupport;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.task.TaskId;

public class TaskSubmitterCallable
    implements Callable<TaskId>, Serializable
{
    private static final long serialVersionUID = 0;

    private final String key;

    private final PropertyTree config;

    public TaskSubmitterCallable( final String key, final PropertyTree config )
    {
        this.key = key;
        this.config = config;
    }

    @Override
    public TaskId call()
    {
        return OsgiSupport.withService( TaskExecutor.class,
                                        taskExecutor -> taskExecutor.submitLocal( DescriptorKey.from( key ), config ) ).orElseThrow(
            () -> new IllegalStateException( "TaskExecutor is missing" ) );
    }
}
