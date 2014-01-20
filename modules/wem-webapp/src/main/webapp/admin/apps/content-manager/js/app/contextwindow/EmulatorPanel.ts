module app.contextwindow {

    export class EmulatorPanel extends api.ui.Panel {
        private dataView: api.ui.grid.DataView<any>;
        private grid: EmulatorGrid;
        private contextWindow: ContextWindow;

        constructor(contextWindow: ContextWindow) {
            super("emulator-panel");

            this.contextWindow = contextWindow;

            var text = new api.dom.PEl();
            text.getEl().setInnerHtml("Emulate different client's physical sizes");
            this.appendChild(text);

            this.dataView = new api.ui.grid.DataView<any>();
            this.grid = new EmulatorGrid(this.dataView);
            this.appendChild(this.grid);

            this.getData();

            // Using jQuery since grid.setOnClick fires event twice, bug in slickgrid
            jQuery(this.getHTMLElement()).on("click", ".grid-row", (event: JQueryEventObject) => {
                var width = jQuery(event.currentTarget).children('div').data("width");
                var height = jQuery(event.currentTarget).children('div').data("height");
                var type = jQuery(event.currentTarget).children('div').data("device.type");
                this.contextWindow.getLiveEditEl().getEl().setWidth(width);
                this.contextWindow.getLiveEditEl().getEl().setHeight(height);
            });

            jQuery(this.getHTMLElement()).on("click", ".rotate", (event: JQueryEventObject) => {
                event.stopPropagation();
                var width = this.contextWindow.getLiveEditEl().getEl().getWidth();
                var height = this.contextWindow.getLiveEditEl().getEl().getHeight();

                this.contextWindow.getLiveEditEl().getEl().setHeight(width + "px");
                this.contextWindow.getLiveEditEl().getEl().setWidth(height + "px");
            });
        }

        private getData(): void {
            jQuery.ajax({
                url: api.util.getAdminUri("apps/content-manager/js/data/context-window/devices.json"),
                success: (data: any, textStatus: string, jqXHR: JQueryXHR) => {
                    this.dataView.setItems(EmulatorGrid.toSlickData(data));
                }
            });
        }
    }
}