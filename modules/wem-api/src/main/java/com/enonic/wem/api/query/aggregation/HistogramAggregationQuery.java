package com.enonic.wem.api.query.aggregation;

public class HistogramAggregationQuery
    extends AbstractHistogramAggregationQuery<Long>
{
    private final Long extendedBoundMin;

    private final Long extendedBoundMax;

    private final Order order;

    private HistogramAggregationQuery( final Builder builder )
    {
        super( builder, builder.interval );
        this.extendedBoundMax = builder.extendedBoundMax;
        this.extendedBoundMin = builder.extendedBoundMin;
        this.order = builder.order;
    }

    public Long getExtendedBoundMin()
    {
        return extendedBoundMin;
    }

    public Long getExtendedBoundMax()
    {
        return extendedBoundMax;
    }

    public boolean setExtendedBounds()
    {
        return extendedBoundMax != null && extendedBoundMin != null;
    }

    public Order getOrder()
    {
        return order;
    }

    public static Builder create( final String name )
    {
        return new Builder( name );
    }

    public static class Builder
        extends AbstractHistogramAggregationQuery.Builder<Builder, Long>
    {
        private Long extendedBoundMin;

        private Long extendedBoundMax;

        private Order order;

        public Builder( final String name )
        {
            super( name );
        }

        public Builder extendedBoundMin( final long extendedBoundMin )
        {
            this.extendedBoundMin = extendedBoundMin;
            return this;
        }

        public Builder extendedBoundMax( final long extendedBoundMax )
        {
            this.extendedBoundMax = extendedBoundMax;
            return this;
        }

        public Builder order( final Order order )
        {
            this.order = order;
            return this;
        }


        public HistogramAggregationQuery build()
        {
            return new HistogramAggregationQuery( this );
        }
    }

    public enum Order
    {
        KEY_ASC,
        KEY_DESC,
        COUNT_ASC,
        COUNT_DESC
    }


}
