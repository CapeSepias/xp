package com.enonic.xp.impl.task.distributed;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.impl.task.LocalTaskManager;
import com.enonic.xp.impl.task.script.NamedTaskScriptFactory;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.task.RunnableTask;
import com.enonic.xp.task.TaskDescriptor;
import com.enonic.xp.task.TaskDescriptorService;
import com.enonic.xp.task.TaskId;
import com.enonic.xp.task.TaskNotFoundException;

import static com.enonic.xp.impl.task.script.NamedTaskScript.SCRIPT_METHOD_NAME;

@Component
public class TaskExecutorImpl
    implements TaskExecutor
{
    private final TaskDescriptorService taskDescriptorService;

    private final NamedTaskScriptFactory namedTaskScriptFactory;

    private final LocalTaskManager taskManager;

    @Activate
    public TaskExecutorImpl( @Reference final LocalTaskManager taskManager, @Reference final TaskDescriptorService taskDescriptorService,
                             @Reference final NamedTaskScriptFactory namedTaskScriptFactory )
    {
        this.taskManager = taskManager;
        this.taskDescriptorService = taskDescriptorService;
        this.namedTaskScriptFactory = namedTaskScriptFactory;
    }

    @Override
    public TaskId submitLocal( final DescriptorKey key, final PropertyTree config )
    {
        final TaskDescriptor descriptor = taskDescriptorService.getTasks( key.getApplicationKey() ).
            filter( taskDesc -> taskDesc.getKey().equals( key ) ).first();
        if ( descriptor == null )
        {
            throw new TaskNotFoundException( key );
        }
        else
        {
            final RunnableTask runnableTask = namedTaskScriptFactory.create( descriptor, config );
            if ( runnableTask == null )
            {
                throw new TaskNotFoundException( key, "Missing exported function '" + SCRIPT_METHOD_NAME + "' in task script" );
            }
            return taskManager.submitTask( runnableTask, descriptor.getDescription(), key.toString() );
        }
    }
}
