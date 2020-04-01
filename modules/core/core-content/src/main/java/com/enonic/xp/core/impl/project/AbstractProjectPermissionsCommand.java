package com.enonic.xp.core.impl.project;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import com.enonic.xp.project.ProjectConstants;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.PrincipalKeys;
import com.enonic.xp.security.PrincipalRelationship;
import com.enonic.xp.security.PrincipalRelationships;
import com.enonic.xp.security.SecurityService;

abstract class AbstractProjectPermissionsCommand
    extends AbstractProjectCommand
{
    final SecurityService securityService;

    AbstractProjectPermissionsCommand( final Builder builder )
    {
        super( builder );
        this.securityService = builder.securityService;
    }

    protected Set<PrincipalRelationship> doGetAddedMembers( final PrincipalRelationships oldRoleMembers, final PrincipalKeys newRoleMembers,
                                                            final PrincipalKey roleKey )
    {
        return newRoleMembers.
            stream().
            filter( newRoleMember -> oldRoleMembers.
                stream().
                noneMatch( oldRoleMember -> oldRoleMember.getTo().equals( newRoleMember ) ) ).
            map( newMember -> PrincipalRelationship.from( roleKey ).to( newMember ) ).
            collect( Collectors.toSet() );
    }

    protected Set<PrincipalRelationship> doGetRemovedMembers( final PrincipalRelationships oldRoleMembers,
                                                              final PrincipalKeys newRoleMembers )
    {
        return oldRoleMembers.
            stream().
            filter( oldRoleMember -> newRoleMembers.
                stream().
                noneMatch( newRoleMember -> oldRoleMember.getTo().equals( newRoleMember ) ) ).
            collect( Collectors.toSet() );
    }

    public static class Builder<B extends Builder>
        extends AbstractProjectCommand.Builder<B>
    {
        SecurityService securityService;

        public B securityService( final SecurityService securityService )
        {
            this.securityService = securityService;
            return (B) this;
        }

        void validate()
        {
            Preconditions.checkNotNull( securityService, "securityService cannot be null" );
            Preconditions.checkArgument( !ProjectConstants.DEFAULT_PROJECT_NAME.equals( projectName ), "Default project has no roles" );
        }
    }

}
