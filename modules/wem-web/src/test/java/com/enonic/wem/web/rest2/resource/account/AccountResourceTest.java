package com.enonic.wem.web.rest2.resource.account;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.wem.core.search.Facet;
import com.enonic.wem.core.search.FacetEntry;
import com.enonic.wem.core.search.Facets;
import com.enonic.wem.core.search.SearchSortOrder;
import com.enonic.wem.core.search.account.AccountIndexField;
import com.enonic.wem.core.search.account.AccountKey;
import com.enonic.wem.core.search.account.AccountSearchQuery;
import com.enonic.wem.core.search.account.AccountSearchResults;
import com.enonic.wem.core.search.account.AccountSearchService;
import com.enonic.wem.core.search.account.AccountType;
import com.enonic.wem.web.rest2.resource.AbstractResourceTest;

import com.enonic.cms.core.security.group.GroupEntity;
import com.enonic.cms.core.security.group.GroupKey;
import com.enonic.cms.core.security.group.GroupType;
import com.enonic.cms.core.security.user.UserEntity;
import com.enonic.cms.core.security.user.UserKey;
import com.enonic.cms.core.security.user.UserType;
import com.enonic.cms.core.security.userstore.UserStoreEntity;
import com.enonic.cms.store.dao.GroupDao;
import com.enonic.cms.store.dao.UserDao;

public class AccountResourceTest
    extends AbstractResourceTest
{
    private AccountResource resource;

    private AccountSearchService service;

    private UserDao userDao;

    private GroupDao groupDao;

    @Before
    public void setUp()
    {
        this.service = Mockito.mock( AccountSearchService.class );
        this.userDao = Mockito.mock( UserDao.class );
        this.groupDao = Mockito.mock( GroupDao.class );
        this.resource = new AccountResource();
        this.resource.setAccountSearchService( service );
        this.resource.setUserDao( userDao );
        this.resource.setGroupDao( groupDao );
    }

    @Test
    public void testSearchEmpty()
        throws Exception
    {
        String query = "",
            types = "users,groups,roles",
            userStores = "",
            organizations = "",
            sort = AccountIndexField.DISPLAY_NAME_FIELD.id(),
            sortDir = SearchSortOrder.ASC.toString().toUpperCase();
        int start = 0, limit = 30;

        AccountSearchQuery searchQuery = resource.buildSearchQuery( query, types, userStores, organizations, start, limit, sort, sortDir );

        Mockito.when( service.search( searchQuery ) ).thenReturn( createEmptyResults() );

        AccountsResult results = resource.search( start, limit, sort, sortDir, query, types, userStores, organizations );

        assertJsonResult( "search_empty.json", results );
    }

    @Test
    public void testSearchAll()
        throws Exception
    {
        String query = "",
            types = "users,groups,roles",
            userStores = "",
            organizations = "",
            sort = AccountIndexField.DISPLAY_NAME_FIELD.id(),
            sortDir = SearchSortOrder.ASC.toString().toUpperCase();
        int start = 0, limit = 30;

        AccountSearchQuery searchQuery = resource.buildSearchQuery( query, types, userStores, organizations, start, limit, sort, sortDir );

        Mockito.when( userDao.findByKey( new UserKey( "E6C593DE14515428B06F1A16E0D28E2341FC5AB4" ) ) ).thenReturn(
            createUser( "E6C593DE14515428B06F1A16E0D28E2341FC5AB4" ) );
        Mockito.when( groupDao.findByKey( new GroupKey( "E34B614B26C666AA9929F90EF3FA723B3DAAAAB2" ) ) ).thenReturn(
            createGroup( "E34B614B26C666AA9929F90EF3FA723B3DAAAAB2" ) );
        Mockito.when( groupDao.findByKey( new GroupKey( "18311D321165D73043C10A9101016CDF3765898E" ) ) ).thenReturn(
            createRole( "18311D321165D73043C10A9101016CDF3765898E" ) );
        Mockito.when( service.search( searchQuery ) ).thenReturn( createAllResults() );

        AccountsResult results = resource.search( start, limit, sort, sortDir, query, types, userStores, organizations );

        assertJsonResult( "search_hits.json", results );
    }

    private GroupEntity createRole( final String key )
    {
        GroupEntity role = new GroupEntity();
        role.setKey( new GroupKey( key ) );
        role.setType( GroupType.AUTHENTICATED_USERS );
        role.setUserStore( createUserstore( "enonic" ) );
        role.setName( "authenticated" );
        role.setDescription( "Authenticated Users" );
        return role;
    }

    private GroupEntity createGroup( final String key )
    {
        GroupEntity group = new GroupEntity();
        group.setKey( new GroupKey( key ) );
        group.setType( GroupType.USERSTORE_GROUP );
        group.setUserStore( createUserstore( "global" ) );
        group.setName( "group1" );
        group.setDescription( "Group One" );
        return group;
    }

    private UserEntity createUser( final String key )
    {
        UserEntity user = new UserEntity();
        user.setKey( new UserKey( key ) );
        user.setType( UserType.NORMAL );
        user.setEmail( "user@email.com" );
        user.setUserStore( createUserstore( "enonic" ) );
        user.setName( "dummy" );
        user.setDisplayName( "Dummy User" );
        user.setPhoto( new byte[0] );
        return user;
    }

    private UserStoreEntity createUserstore( String name )
    {
        UserStoreEntity userstore = new UserStoreEntity();
        userstore.setName( name );
        return userstore;
    }

    private AccountSearchResults createEmptyResults()
    {
        AccountSearchResults searchResults = new AccountSearchResults( 0, 0 );

        Facets facets = searchResults.getFacets();
        Facet type = new Facet( "type" );
        type.addEntry( new FacetEntry( "role", 0 ) );
        type.addEntry( new FacetEntry( "group", 0 ) );
        type.addEntry( new FacetEntry( "user", 0 ) );
        facets.addFacet( type );
        Facet userstore = new Facet( "userstore" );
        userstore.addEntry( new FacetEntry( "global", 0 ) );
        userstore.addEntry( new FacetEntry( "enonic", 0 ) );
        facets.addFacet( userstore );
        final Facet organization = new Facet( "organization" );
        organization.addEntry( new FacetEntry( "Enonic", 0 ) );
        facets.addFacet( organization );

        return searchResults;
    }

    private AccountSearchResults createAllResults()
    {
        AccountSearchResults searchResults = new AccountSearchResults( 0, 3 );

        Facets facets = searchResults.getFacets();
        Facet type = new Facet( "type" );
        type.addEntry( new FacetEntry( "role", 1 ) );
        type.addEntry( new FacetEntry( "group", 1 ) );
        type.addEntry( new FacetEntry( "user", 1 ) );
        facets.addFacet( type );
        Facet userstore = new Facet( "userstore" );
        userstore.addEntry( new FacetEntry( "global", 1 ) );
        userstore.addEntry( new FacetEntry( "enonic", 2 ) );
        facets.addFacet( userstore );
        final Facet organization = new Facet( "organization" );
        organization.addEntry( new FacetEntry( "Enonic", 2 ) );
        facets.addFacet( organization );

        searchResults.add( new AccountKey( "E6C593DE14515428B06F1A16E0D28E2341FC5AB4" ), AccountType.USER, 1 );
        searchResults.add( new AccountKey( "E34B614B26C666AA9929F90EF3FA723B3DAAAAB2" ), AccountType.GROUP, 1 );
        searchResults.add( new AccountKey( "18311D321165D73043C10A9101016CDF3765898E" ), AccountType.ROLE, 1 );

        return searchResults;
    }

}
