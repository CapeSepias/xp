module api.liveedit {

    import PropertyTree = api.data.PropertyTree;
    import Content = api.content.Content;
    import Page = api.content.page.Page;
    import PageModel = api.content.page.PageModel;
    import PageMode = api.content.page.PageMode;
    import PageModeChangedEvent = api.content.page.PageModeChangedEvent;
    import Site = api.content.site.Site;
    import Regions = api.content.page.region.Regions;
    import Component = api.content.page.region.Component;
    import Region = api.content.page.region.Region;
    import RegionPath = api.content.page.region.RegionPath;
    import ComponentPath = api.content.page.region.ComponentPath;

    export class PageViewBuilder {

        liveEditModel: LiveEditModel;

        itemViewProducer: ItemViewIdProducer;

        element: api.dom.Body;

        setLiveEditModel(value: LiveEditModel): PageViewBuilder {
            this.liveEditModel = value;
            return this;
        }

        setItemViewProducer(value: ItemViewIdProducer): PageViewBuilder {
            this.itemViewProducer = value;
            return this;
        }

        setElement(value: api.dom.Body): PageViewBuilder {
            this.element = value;
            return this;
        }

        build(): PageView {
            return new PageView(this);
        }
    }

    export class PageView extends ItemView {

        private pageModel: PageModel;

        private regionViews: RegionView[];

        private viewsById: {[s:number] : ItemView;};

        private itemViewAddedListeners: {(event: ItemViewAddedEvent) : void}[];

        private itemViewRemovedListeners: {(event: ItemViewRemovedEvent) : void}[];

        private unlockedScreenActions: api.ui.Action[];

        constructor(builder: PageViewBuilder) {

            this.liveEditModel = builder.liveEditModel;
            this.pageModel = builder.liveEditModel.getPageModel();
            this.pageModel.onPropertyChanged(() => this.refreshEmptyState());
            this.regionViews = [];
            this.viewsById = {};
            this.itemViewAddedListeners = [];
            this.itemViewRemovedListeners = [];


            var resetAction = new api.ui.Action('Reset');
            if (this.pageModel.getMode() == PageMode.AUTOMATIC || this.pageModel.getMode() == PageMode.NO_CONTROLLER) {
                resetAction.setEnabled(false);
            }
            resetAction.onExecuted(() => {
                this.pageModel.reset(this);
                this.setLocked(true);
            });
            this.pageModel.onPageModeChanged((event: PageModeChangedEvent) => {
                var resetEnabled = !(event.getNewMode() != PageMode.AUTOMATIC && event.getNewMode() != PageMode.NO_CONTROLLER);
                resetAction.setEnabled(resetEnabled);
            });
            this.unlockedScreenActions = [resetAction];

            super(new ItemViewBuilder().
                setLiveEditModel(builder.liveEditModel).
                setItemViewIdProducer(builder.itemViewProducer).
                setPlaceholder(new PagePlaceholder(this)).
                setTooltipViewer(new api.content.ContentSummaryViewer()).
                setType(PageItemType.get()).
                setElement(builder.element).
                setParentElement(builder.element.getParentElement()).
                setContextMenuActions(this.unlockedScreenActions).
                setContextMenuTitle(new PageViewContextMenuTitle(builder.liveEditModel.getContent())));

            this.addClass('page-view');

            this.setTooltipObject(builder.liveEditModel.getContent());
            this.parseItemViews();

            this.appendChild(this.createTextModeToolbar());

            this.refreshEmptyState();

            this.toItemViewArray().forEach((itemView: ItemView) => {
                this.registerItemView(itemView);
            });

            this.regionViews.forEach((regionView: RegionView) => {
                regionView.onItemViewAdded((event: ItemViewAddedEvent) => {
                    this.registerItemView(event.getView());

                    // adding anything should exit the text edit mode
                    this.exitTextEditModeIfNeeded();
                });
                regionView.onItemViewRemoved((event: ItemViewRemovedEvent) => this.unregisterItemView(event.getView()));
            });

            // lock page by default for every content that has not been modified except for page template
            if (!this.liveEditModel.getContent().isPageTemplate() && !this.isPageModified(this.pageModel)) {
                this.setLocked(true);
            }

            this.listenToMouseEvents();
        }

        private isPageModified(pageModel: PageModel): boolean {
            // default template regions differing from page regions means it has been modified
            return pageModel.getDefaultPageTemplate().isPage() &&
                   !pageModel.getDefaultPageTemplate().getRegions().equals(pageModel.getRegions());
        }

        isManagingContextMenu(): boolean {
            return true;
        }

        isManagingCursor(): boolean {
            return true;
        }

        isManagingTooltip(): boolean {
            return true;
        }

        isManagingShader(): boolean {
            return true;
        }

        isManagingHighlighter(): boolean {
            return true;
        }

        private listenToMouseEvents() {
            this.onMouseOverView(() => {
                var hasSelectedView = this.hasSelectedView();

                if (!this.isTextEditMode() && !hasSelectedView && !this.isLocked()) {
                    this.showTooltip();
                    this.highlight();
                    this.showCursor();
                }
            });
            this.onMouseLeaveView(() => {
                var hasSelectedView = this.hasSelectedView();

                if (!this.isTextEditMode() && !hasSelectedView && !this.isLocked()) {
                    this.hideTooltip();
                    this.unhighlight();
                    this.resetCursor();
                }
            });

            Shader.get().onClicked((event: MouseEvent) => {
                if (!this.isLocked() && this.isSelected()) {
                    this.deselect();
                }
            });
            Shader.get().onUnlockClicked((event: MouseEvent) => {
                if (this.isLocked()) {
                    this.setLocked(false);
                }
            })
        }

        select(clickPosition?: Position, menuPosition?: ItemViewContextMenuPosition) {
            super.select(clickPosition, menuPosition);

            if (!this.isLocked()) {
                this.showContextMenu(clickPosition, menuPosition);
            }
            if (!this.isEmpty()) {
                this.shade();
            }

            this.hideTooltip();
            this.showCursor();

            new PageSelectedEvent(this).fire();
        }

        deselect(silent?: boolean) {
            super.deselect(silent);

            if (!this.isEmpty()) {
                this.unshade();
            }

            this.resetCursor();
            this.hideContextMenu();
        }

        handleClick(event: MouseEvent) {
            event.stopPropagation();
            event.preventDefault();

            if (this.isTextEditMode()) {
                this.setTextEditMode(false);
            } else {
                super.handleClick(event);
            }
        }

        isLocked() {
            return this.hasClass('locked');
        }

        setLocked(locked: boolean) {
            this.toggleClass('locked', locked);

            if (locked) {
                this.shade();

                new PageLockedEvent(this).fire();
            } else {
                this.unshade();

                if (!this.pageModel.isPageTemplate() || this.pageModel.getMode() == PageMode.AUTOMATIC) {
                    this.pageModel.initializePageFromDefault(this);
                }
            }
        }

        private createTextModeToolbar() {
            var toolbar = new api.dom.DivEl('text-edit-toolbar');
            var wrapper = new api.dom.DivEl('wrapper');
            wrapper.setHtml('Text Edit Mode');
            var closeButton = new api.ui.button.CloseButton('no-bg');
            closeButton.onClicked((event: MouseEvent) => {
                event.stopPropagation();
                event.preventDefault();

                this.setTextEditMode(false);
            });
            wrapper.appendChild(closeButton);
            toolbar.appendChild(wrapper);
            return toolbar;
        }


        isTextEditMode(): boolean {
            return this.hasClass('text-edit-mode');
        }

        setTextEditMode(flag: boolean) {
            this.toggleClass('text-edit-mode', flag);

            var textItemViews = this.getItemViewsByType(api.liveedit.text.TextItemType.get());

            var textView: api.liveedit.text.TextComponentView;
            textItemViews.forEach((view: ItemView) => {
                textView = <api.liveedit.text.TextComponentView> view;
                if (textView.isEditMode() != flag) {
                    textView.setEditMode(flag);
                }
            });

            if (flag) {
                new PageTextModeStartedEvent(this).fire();
            }
        }

        isEmpty(): boolean {
            return !this.pageModel || this.pageModel.getMode() == PageMode.NO_CONTROLLER;
        }

        getName(): string {
            return this.liveEditModel.getContent() ? this.liveEditModel.getContent().getDisplayName() : "[No name]";
        }

        getParentItemView(): ItemView {
            return null;
        }

        addRegion(regionView: RegionView) {
            this.regionViews.push(regionView);
        }

        getRegions(): RegionView[] {
            return this.regionViews;
        }

        toItemViewArray(): ItemView[] {

            var array: ItemView[] = [];
            array.push(this);
            this.regionViews.forEach((regionView: RegionView) => {
                var itemViews = regionView.toItemViewArray();
                array = array.concat(itemViews);
            });
            return array;
        }

        hasSelectedView(): boolean {
            return !!this.getSelectedView();
        }

        getSelectedView(): ItemView {
            for (var id in this.viewsById) {
                if (this.viewsById.hasOwnProperty(id) && this.viewsById[id].isSelected()) {
                    return this.viewsById[id];
                }
            }
            return null;
        }

        getItemViewById(id: ItemViewId): ItemView {
            api.util.assertNotNull(id, "value cannot be null");
            return this.viewsById[id.toNumber()];
        }


        getItemViewsByType(type: ItemType): ItemView[] {
            var views: ItemView[] = [];
            for (var key in this.viewsById) {
                if (this.viewsById.hasOwnProperty(key)) {
                    var view = this.viewsById[key];
                    if (type.equals(view.getType())) {
                        views.push(view);
                    }
                }
            }
            return views;
        }

        getItemViewByElement(element: HTMLElement): ItemView {
            api.util.assertNotNull(element, "element cannot be null");

            var itemId = ItemView.parseItemId(element);
            if (!itemId) {
                return null;
            }

            var itemView = this.getItemViewById(itemId);
            api.util.assertNotNull(itemView, "ItemView not found: " + itemId.toString());

            return itemView;
        }

        getRegionViewByElement(element: HTMLElement): RegionView {
            api.util.assertNotNull(element, "element cannot be null");

            var itemId = ItemView.parseItemId(element);
            if (!itemId) {
                return null;
            }

            var itemView = this.getItemViewById(itemId);
            api.util.assertNotNull(itemView, "ItemView not found: " + itemId.toString());

            if (api.ObjectHelper.iFrameSafeInstanceOf(itemView, RegionView)) {
                return <RegionView>itemView;
            }
            return null;
        }

        getComponentViewByElement(element: HTMLElement): ComponentView<Component> {
            api.util.assertNotNull(element, "element cannot be null");

            var itemId = ItemView.parseItemId(element);
            if (!itemId) {
                return null;
            }

            var itemView = this.getItemViewById(itemId);
            api.util.assertNotNull(itemView, "ItemView not found: " + itemId.toString());
            if (api.ObjectHelper.iFrameSafeInstanceOf(itemView, ComponentView)) {
                return <ComponentView<Component>>itemView;
            }
            return null;
        }

        getRegionViewByPath(path: RegionPath): RegionView {

            for (var i = 0; i < this.regionViews.length; i++) {
                var regionView = this.regionViews[i];

                if (path.hasParentComponentPath()) {
                    var componentView = this.getComponentViewByPath(path.getParentComponentPath());
                    if (api.ObjectHelper.iFrameSafeInstanceOf(componentView, api.liveedit.layout.LayoutComponentView)) {
                        var layoutView = <api.liveedit.layout.LayoutComponentView>componentView;
                        layoutView.getRegionViewByName(path.getRegionName());
                    }
                }
                else {
                    if (path.getRegionName() == regionView.getRegionName()) {
                        return regionView;
                    }
                }
            }

            return null;
        }

        getComponentViewByPath(path: ComponentPath): ComponentView<Component> {

            var firstLevelOfPath = path.getFirstLevel();

            for (var i = 0; i < this.regionViews.length; i++) {
                var regionView = this.regionViews[i];
                if (firstLevelOfPath.getRegionName() == regionView.getRegionName()) {
                    if (path.numberOfLevels() == 1) {
                        return regionView.getComponentViewByIndex(firstLevelOfPath.getComponentIndex());
                    }
                    else {
                        var layoutView: api.liveedit.layout.LayoutComponentView = <api.liveedit.layout.LayoutComponentView>regionView.getComponentViewByIndex(firstLevelOfPath.getComponentIndex());
                        return layoutView.getComponentViewByPath(path.removeFirstLevel());
                    }
                }
            }

            return null;
        }

        private exitTextEditModeIfNeeded() {
            if (this.isTextEditMode()) {
                this.setTextEditMode(false);
            }
        }

        private registerItemView(view: ItemView) {

            // logging...
            var extra = "";
            if (api.ObjectHelper.iFrameSafeInstanceOf(view, ComponentView)) {
                var componentView = <ComponentView<Component>>view;
                if (componentView.hasComponentPath()) {
                    extra = componentView.getComponentPath().toString();
                }
            }
            else if (api.ObjectHelper.iFrameSafeInstanceOf(view, RegionView)) {
                var regionView = <RegionView>view;
                extra = regionView.getRegionPath().toString();
            }

            console.debug("PageView.registerItemView: " + view.getItemId().toNumber() + " : " + view.getType().getShortName() + " : " +
                          extra);

            this.viewsById[view.getItemId().toNumber()] = view;

            this.notifyItemViewAdded(new ItemViewAddedEvent(view));
        }

        private unregisterItemView(view: ItemView) {
            console.debug("PageView.unregisterItemView: " + view.getItemId().toNumber());
            delete this.viewsById[view.getItemId().toNumber()];

            this.notifyItemViewRemoved(new ItemViewRemovedEvent(view));
        }

        private parseItemViews() {
            this.doParseItemViews();
        }

        private doParseItemViews(parentElement?: api.dom.Element) {

            var pageRegions = this.liveEditModel.getPageModel().getRegions();
            if (!pageRegions) {
                return;
            }
            var regions: Region[] = pageRegions.getRegions();
            var children = parentElement ? parentElement.getChildren() : this.getChildren();
            var regionIndex = 0;
            children.forEach((childElement: api.dom.Element) => {
                var itemType = ItemType.fromElement(childElement);
                if (itemType) {
                    if (RegionItemType.get().equals(itemType)) {
                        var region = regions[regionIndex++];
                        if (region) {
                            var regionView = new RegionView(new RegionViewBuilder().
                                setLiveEditModel(this.liveEditModel).
                                setParentView(this).
                                setRegion(region).
                                setElement(childElement));
                            this.addRegion(regionView);
                            regionView.parseComponentViews();
                        }
                    }
                    else {
                        this.doParseItemViews(childElement);
                    }
                }
                else {
                    this.doParseItemViews(childElement);
                }
            });
        }

        onItemViewAdded(listener: (event: ItemViewAddedEvent) => void) {
            this.itemViewAddedListeners.push(listener);
        }

        unItemViewAdded(listener: (event: ItemViewAddedEvent) => void) {
            this.itemViewAddedListeners = this.itemViewAddedListeners.filter((current) => (current != listener));
        }

        private notifyItemViewAdded(event: ItemViewAddedEvent) {
            this.itemViewAddedListeners.forEach((listener) => listener(event));
        }

        onItemViewRemoved(listener: (event: ItemViewRemovedEvent) => void) {
            this.itemViewRemovedListeners.push(listener);
        }

        unItemViewRemoved(listener: (event: ItemViewRemovedEvent) => void) {
            this.itemViewRemovedListeners = this.itemViewRemovedListeners.filter((current) => (current != listener));
        }

        private notifyItemViewRemoved(event: ItemViewRemovedEvent) {
            this.itemViewRemovedListeners.forEach((listener) => listener(event));
        }
    }
}