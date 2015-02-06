module api.liveedit.image {

    import ComponentView = api.liveedit.ComponentView;
    import ContentView = api.liveedit.ContentView;
    import RegionView = api.liveedit.RegionView;
    import ImageComponent = api.content.page.region.ImageComponent;

    export class ImageComponentViewBuilder extends ComponentViewBuilder<ImageComponent> {

        constructor() {
            super();
            this.setType(ImageItemType.get());
        }
    }

    export class ImageComponentView extends ComponentView<ImageComponent> {

        private image: api.dom.Element;
        private imageComponent: ImageComponent;

        constructor(builder: ImageComponentViewBuilder) {
            this.liveEditModel = builder.parentRegionView.liveEditModel;
            this.imageComponent = builder.component;

            super(builder.setPlaceholder(
                new ImagePlaceholder(this)).
                setTooltipViewer(new ImageComponentViewer()));

            this.initializeImage();

            this.addClass('image-view');
        }

        private initializeImage() {

            for (var i = 0; i < this.getChildren().length; i++) {
                var figure = this.getChildren()[i];
                if (figure.getHTMLElement().tagName.toUpperCase() == 'FIGURE') {
                    for (var j = 0; j < figure.getChildren().length; j++) {
                        var image = figure.getChildren()[j];
                        if (image.getHTMLElement().tagName.toUpperCase() == 'IMG') {
                            this.image = image;

                            // no way to use ImgEl.onLoaded because all html tags are parsed as Element
                            this.image.getEl().addEventListener("load", (event) => {
                                // refresh shader and highlighter after image loaded
                                // if it's still selected
                                if (this.isSelected()) {
                                    this.highlight();
                                    this.shade();
                                }
                            });
                        }

                        return;
                    }
                }
            }
        }

        isEmpty(): boolean {
            return !this.imageComponent || this.imageComponent.isEmpty();
        }

        duplicate(duplicate: ImageComponent): ImageComponentView {
            var duplicatedView = new ImageComponentView(new ImageComponentViewBuilder().
                setParentRegionView(this.getParentItemView()).
                setParentElement(this.getParentElement()).
                setComponent(duplicate));
            duplicatedView.insertAfterEl(this);
            return duplicatedView;
        }

    }
}