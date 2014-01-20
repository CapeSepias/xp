module app.contextwindow {
    export class DetailPanel extends api.ui.Panel {
        private header:api.dom.H3El;
        private subtitle:api.dom.DivEl;
        private iconEl:api.dom.DivEl;
        private infoEl:api.dom.DivEl;

        constructor(contextWindow:ContextWindow) {
            super("detail-panel");

            this.initElements();
            this.setEmpty();

            SelectComponentEvent.on((event) => {
                this.setName(event.getComponent().name);
                this.setType(event.getComponent().componentType.typeName);
                this.setIcon(event.getComponent().componentType.iconCls);
            });

            ComponentDeselectEvent.on((event) => {
                this.setEmpty();
            });

        }

        private initElements() {
            this.header = new api.dom.H3El();
            this.subtitle = new api.dom.DivEl();
            this.iconEl = new api.dom.DivEl();
            this.infoEl = new api.dom.DivEl();

            this.iconEl.addClass("icon");

            this.appendChild(this.iconEl);
            this.appendChild(this.header);
            this.appendChild(this.subtitle);
            this.appendChild(this.infoEl);
        }

        private setEmpty() {
            this.header.getEl().setInnerHtml("Empty");
            this.subtitle.getEl().setInnerHtml("No component selected");
            this.iconEl.removeAllClasses("icon");
        }

        setIcon(iconCls:string) {
            this.iconEl.addClass(iconCls);
        }

        setName(name:string) {
            this.subtitle.getEl().setInnerHtml(name);
        }

        setType(type:string) {
            this.header.getEl().setInnerHtml(type);
        }
    }
}