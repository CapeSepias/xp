package com.enonic.xp.impl.task.distributed;

import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.task.TaskId;

public interface TaskExecutor
{
    TaskId submitLocal( final DescriptorKey key, final PropertyTree config );
}
