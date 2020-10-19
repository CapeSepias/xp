package com.enonic.xp.impl.task;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.task.distributed.TaskExecutor;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.task.RunnableTask;
import com.enonic.xp.task.TaskId;
import com.enonic.xp.task.TaskInfo;
import com.enonic.xp.task.TaskService;

@Component
public final class TaskServiceImpl
    implements TaskService
{
    private final LocalTaskManager taskManager;

    private final TaskExecutor taskExecutor;

    private volatile TaskInfoManager taskInfoManager;

    @Activate
    public TaskServiceImpl( @Reference final LocalTaskManager taskManager, @Reference TaskExecutor taskExecutor )
    {
        this.taskManager = taskManager;
        this.taskInfoManager = taskManager;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public TaskId submitTask( final RunnableTask runnable, final String description )
    {
        return taskManager.submitTask( runnable, description, "" );
    }

    @Override
    public TaskId submitTask( final DescriptorKey key, final PropertyTree config )
    {
        if ( taskInfoManager instanceof ClusteredTaskManager )
        {
            return ( (ClusteredTaskManager) taskInfoManager ).submitClustered( key.toString(), config );
        }
        else
        {
            return taskExecutor.submitLocal( key, config );
        }
    }

    @Override
    public TaskInfo getTaskInfo( final TaskId taskId )
    {
        return taskInfoManager.getTaskInfo( taskId );
    }

    @Override
    public List<TaskInfo> getAllTasks()
    {
        return taskInfoManager.getAllTasks();
    }

    @Override
    public List<TaskInfo> getRunningTasks()
    {
        return taskInfoManager.getRunningTasks();
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setClusteredTaskManager( final ClusteredTaskManager clusteredTaskManager )
    {
        this.taskInfoManager = clusteredTaskManager;
    }

    public void unsetClusteredTaskManager( final ClusteredTaskManager clusteredTaskManager )
    {
        this.taskInfoManager = taskManager;
    }
}
