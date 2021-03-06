package com.enonic.xp.repo.impl.elasticsearch.snapshot;

import org.elasticsearch.client.Client;

class AbstractSnapshotExecutor
{
    final String snapshotRepositoryName;

    final String snapshotName;

    final Client client;

    AbstractSnapshotExecutor( final Builder builder )
    {
        snapshotRepositoryName = builder.snapshotRepositoryName;
        client = builder.client;
        snapshotName = builder.snapshotName;
    }

    public static class Builder<B extends Builder>
    {
        private String snapshotRepositoryName;

        private Client client;

        private String snapshotName;

        @SuppressWarnings("unchecked")
        public B snapshotRepositoryName( final String val )
        {
            snapshotRepositoryName = val;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B client( final Client val )
        {
            client = val;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B snapshotName( final String snapshotName )
        {
            this.snapshotName = snapshotName;
            return (B) this;
        }
    }
}
