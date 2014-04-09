module api.util.loader {

    import LoaderEvents = api.util.loader.event.LoaderEvents;
    import LoaderEvent = api.util.loader.event.LoaderEvent;
    import LoadedDataEvent = api.util.loader.event.LoadedDataEvent;
    import LoadingDataEvent = api.util.loader.event.LoadingDataEvent;

    export class BaseLoader<JSON, OBJECT> {

        private request: api.rest.ResourceRequest<JSON>;

        private isLoading: boolean;

        private results: OBJECT[];

        private searchString: string;

        private loadedDataListeners: {(event: LoadedDataEvent):void}[] = [];

        private loadingDataListeners: {(event: LoadingDataEvent):void}[] = [];

        constructor(request: api.rest.ResourceRequest<JSON>, autoLoad: boolean = true) {
            this.isLoading = false;
            this.setRequest(request);
            if (autoLoad) {
                this.load();
            }
        }

        doRequest(): Q.Promise<OBJECT[]> {
            var deferred = Q.defer<OBJECT[]>();

            this.request.sendAndParse().done((results: OBJECT[]) => {
                deferred.resolve(results);
            });

            return deferred.promise;
        }

        load(): void {
            this.isLoading = true;
            this.notifyLoadingData();
            this.doRequest().done((results: OBJECT[]) => {
                this.results = results;
                this.isLoading = false;
                this.notifyLoadedData(results);
            });
        }

        loading(isLoading?: boolean): boolean {
            if (typeof isLoading == 'boolean') {
                this.isLoading = isLoading;
            }
            return this.isLoading;
        }

        setRequest(request: api.rest.ResourceRequest<JSON>) {
            this.request = request;
        }

        getRequest(): api.rest.ResourceRequest<JSON> {
            return this.request;
        }

        search(searchString: string) {

            this.searchString = searchString;
            if (this.results) {
                var filtered = this.results.filter(this.filterFn, this);
                this.notifyLoadedData(filtered);
            }
        }

        getSearchString(): string {
            return this.searchString;
        }

        filterFn(result: OBJECT): boolean {
            throw Error("must be implemented");
        }

        notifyLoadedData(results: OBJECT[]) {
            this.loadedDataListeners.forEach((listener: (event: LoadedDataEvent)=>void)=> {
                listener.call(this, new LoadedDataEvent<OBJECT>(results));
            });
        }

        notifyLoadingData() {
            this.loadingDataListeners.forEach((listener: (event: LoadingDataEvent)=>void)=> {
                listener.call(this, new LoadingDataEvent());
            });
        }

        onLoadedData(listener: (event: LoadedDataEvent<OBJECT>) => void) {
            this.loadedDataListeners.push(listener);
        }

        onLoadingData(listener: (event: LoadingDataEvent) => void) {
            this.loadingDataListeners.push(listener);
        }

        unLoadedData(listener: (event: LoadedDataEvent<OBJECT>) => void) {
            this.loadedDataListeners = this.loadedDataListeners.filter((currentListener: (event: LoadedDataEvent)=>void)=> {
                return currentListener != listener;
            });
        }

        unLoadingData(listener: (event: LoadingDataEvent) => void) {
            this.loadingDataListeners = this.loadingDataListeners.filter((currentListener: (event: LoadingDataEvent)=>void)=> {
                return currentListener != listener;
            });
        }

    }
}