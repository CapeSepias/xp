package com.enonic.wem.repo.internal.entity.query.aggregation;

import java.util.Iterator;

import org.junit.Test;

import com.google.common.base.Strings;

import junit.framework.Assert;

import com.enonic.wem.api.aggregation.Bucket;
import com.enonic.wem.api.aggregation.BucketAggregation;
import com.enonic.wem.api.aggregation.Buckets;
import com.enonic.wem.api.aggregation.NumericRangeBucket;
import com.enonic.wem.api.data.PropertyTree;
import com.enonic.wem.api.node.CreateNodeParams;
import com.enonic.wem.api.node.FindNodesByQueryResult;
import com.enonic.wem.api.node.Node;
import com.enonic.wem.api.node.NodePath;
import com.enonic.wem.api.node.NodeQuery;
import com.enonic.wem.api.query.aggregation.NumericRange;
import com.enonic.wem.api.query.aggregation.NumericRangeAggregationQuery;
import com.enonic.wem.repo.internal.entity.AbstractNodeTest;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

public class NumericRangeAggregationTest
    extends AbstractNodeTest
{
    @Test
    public void ranges()
        throws Exception
    {
        createNode( 100d, "n1", NodePath.ROOT );
        createNode( 300d, "n2", NodePath.ROOT );
        createNode( 400d, "n3", NodePath.ROOT );
        createNode( 200d, "n4", NodePath.ROOT );
        createNode( 600d, "n5", NodePath.ROOT );
        createNode( 500d, "n6", NodePath.ROOT );

        final NodeQuery query = NodeQuery.create().
            addAggregationQuery( NumericRangeAggregationQuery.create( "myNumericRange" ).
                fieldName( "numeric" ).
                addRange( NumericRange.create().
                    to( 150d ).
                    build() ).
                addRange( NumericRange.create().
                    from( 150d ).
                    to( 250d ).
                    build() ).
                addRange( NumericRange.create().
                    from( 250d ).
                    to( 350d ).
                    build() ).
                addRange( NumericRange.create().
                    from( 350d ).
                    build() ).
                build() ).
            build();

        FindNodesByQueryResult result = doFindByQuery( query );

        assertEquals( 1, result.getAggregations().getSize() );

        final BucketAggregation aggregation = (BucketAggregation) result.getAggregations().get( "myNumericRange" );

        final Buckets buckets = aggregation.getBuckets();

        final Iterator<Bucket> iterator = buckets.iterator();
        verifyBucket( iterator.next(), 1, null );
        verifyBucket( iterator.next(), 1, null );
        verifyBucket( iterator.next(), 1, null );
        verifyBucket( iterator.next(), 3, null );
    }

    @Test
    public void keys()
        throws Exception
    {
        createNode( 100d, "n1", NodePath.ROOT );
        createNode( 200d, "n4", NodePath.ROOT );
        createNode( 300d, "n2", NodePath.ROOT );
        createNode( 400d, "n3", NodePath.ROOT );
        createNode( 500d, "n6", NodePath.ROOT );
        createNode( 600d, "n5", NodePath.ROOT );

        final NodeQuery query = NodeQuery.create().
            addAggregationQuery( NumericRangeAggregationQuery.create( "myNumericRange" ).
                fieldName( "numeric" ).
                addRange( NumericRange.create().
                    key( "small" ).
                    to( 350d ).
                    build() ).
                addRange( NumericRange.create().
                    key( "large" ).
                    from( 350d ).
                    build() ).
                build() ).
            build();

        FindNodesByQueryResult result = doFindByQuery( query );

        assertEquals( 1, result.getAggregations().getSize() );

        final BucketAggregation aggregation = (BucketAggregation) result.getAggregations().get( "myNumericRange" );

        final Buckets buckets = aggregation.getBuckets();

        final Iterator<Bucket> iterator = buckets.iterator();
        verifyBucket( iterator.next(), 3, "small" );
        verifyBucket( iterator.next(), 3, "large" );
    }

    private void verifyBucket( final Bucket bucket, final int count, final String key )
    {
        assertTrue( bucket instanceof NumericRangeBucket );
        final NumericRangeBucket buck = (NumericRangeBucket) bucket;

        assertEquals( count, buck.getDocCount() );

        if ( !Strings.isNullOrEmpty( key ) )
        {
            Assert.assertEquals( buck.getKey(), key );
        }
    }

    private Node createNode( final Double numericValue, final String name, final NodePath parent )
    {
        final PropertyTree data = new PropertyTree();
        data.addDouble( "numeric", numericValue );

        return createNode( CreateNodeParams.create().
            parent( parent ).
            name( name ).
            data( data ).
            build() );
    }


}
