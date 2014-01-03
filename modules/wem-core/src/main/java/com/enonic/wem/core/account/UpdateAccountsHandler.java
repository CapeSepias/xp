package com.enonic.wem.core.account;

import javax.inject.Inject;
import javax.jcr.Session;

import org.joda.time.DateTime;

import com.enonic.wem.api.account.Account;
import com.enonic.wem.api.account.AccountKey;
import com.enonic.wem.api.account.GroupAccount;
import com.enonic.wem.api.account.RoleAccount;
import com.enonic.wem.api.account.UserAccount;
import com.enonic.wem.api.account.editor.AccountEditor;
import com.enonic.wem.api.command.UpdateResult;
import com.enonic.wem.api.command.account.UpdateAccounts;
import com.enonic.wem.core.account.dao.AccountDao;
import com.enonic.wem.core.command.CommandHandler;


public final class UpdateAccountsHandler
    extends CommandHandler<UpdateAccounts>
{
    private AccountDao accountDao;

    @Override
    public void handle()
        throws Exception
    {
        final AccountKey accountKey = command.getKey();
        final AccountEditor editor = command.getEditor();
        final Session session = context.getJcrSession();

        UpdateResult result = UpdateResult.notUpdated();
        try
        {
            final Account account = retrieveAccount( session, accountKey );
            if ( account != null )
            {
                final boolean flag = editor.edit( account );
                if ( flag )
                {
                    updateAccount( session, account );
                    result = UpdateResult.updated();
                }
            }
        }
        catch ( Exception e )
        {
            result = UpdateResult.failure( e.getMessage() );
        }

        session.save();

        command.setResult( result );
    }

    private Account retrieveAccount( final Session session, final AccountKey account )
        throws Exception
    {
        switch ( account.getType() )
        {
            case USER:
                return accountDao.findUser( account.asUser(), true, true, session );
            case GROUP:
                return accountDao.findGroup( account.asGroup(), true, session );
            default:
                return accountDao.findRole( account.asRole(), true, session );
        }
    }

    private void updateAccount( final Session session, final Account account )
        throws Exception
    {
        account.setModifiedTime( DateTime.now() );
        switch ( account.getKey().getType() )
        {
            case USER:
                accountDao.updateUser( (UserAccount) account, session );
                break;
            case GROUP:
                accountDao.updateGroup( (GroupAccount) account, session );
                break;
            case ROLE:
                accountDao.updateRole( (RoleAccount) account, session );
                break;
        }


    }

    @Inject
    public void setAccountDao( final AccountDao accountDao )
    {
        this.accountDao = accountDao;
    }
}
