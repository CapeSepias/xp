package com.enonic.xp.impl.task;

import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.task.TaskId;

public interface ClusteredTaskManager
    extends TaskInfoManager
{
    TaskId submitClustered( String key, final PropertyTree config );
}
