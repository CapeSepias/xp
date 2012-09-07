package com.enonic.wem.api.account.editor;

import com.enonic.wem.api.account.Account;
import com.enonic.wem.api.account.GroupAccount;
import com.enonic.wem.api.account.RoleAccount;
import com.enonic.wem.api.account.UserAccount;

public abstract class AccountEditorAdapter
    implements AccountEditor
{
    @Override
    public final boolean edit( final Account account )
        throws Exception
    {
        if ( account instanceof UserAccount )
        {
            return this.editUser( (UserAccount) account );
        }
        else if ( account instanceof GroupAccount )
        {
            return editGroup( (GroupAccount) account );
        }
        else if ( account instanceof RoleAccount )
        {
            return editRole( (RoleAccount) account );
        }
        else
        {
            return false;
        }
    }

    protected abstract boolean editUser( final UserAccount account )
        throws Exception;

    protected abstract boolean editGroup( final GroupAccount account )
        throws Exception;

    protected abstract boolean editRole( final RoleAccount account )
        throws Exception;
}
