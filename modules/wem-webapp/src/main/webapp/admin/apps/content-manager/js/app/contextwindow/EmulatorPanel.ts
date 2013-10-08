module app_contextwindow {
    export class EmulatorPanel extends api_ui.Panel {
        private grid:EmulatorGrid;
        private contextWindow:ContextWindow;

        constructor(contextWindow:ContextWindow) {
            super("EmulatorPanel");
            this.addClass("emulator-panel");

            this.contextWindow = contextWindow;

            var text = new api_dom.PEl();
            text.getEl().setInnerHtml("Emulate different client's physical sizes");
            this.appendChild(text);

            this.grid = new EmulatorGrid();
            this.appendChild(this.grid);

            this.getData();

            // Using jQuery since grid.setOnClick fires event twice, bug in slickgrid
            jQuery(this.getHTMLElement()).on("click", ".component", (event:JQueryEventObject) => {
                var width = jQuery(event.currentTarget).children('div').data("width");
                var height = jQuery(event.currentTarget).children('div').data("height");
                var type = jQuery(event.currentTarget).children('div').data("device_type");
                this.contextWindow.getLiveEditEl().getEl().setWidth(width);
                this.contextWindow.getLiveEditEl().getEl().setHeight(height);
            });

            jQuery(this.getHTMLElement()).on("click", ".rotate", (event:JQueryEventObject) => {
                event.stopPropagation();
                var width = jQuery(event.currentTarget).parent('div').data("height");
                var height = jQuery(event.currentTarget).parent('div').data("width");
                console.log(width, height);
                this.contextWindow.getLiveEditEl().getEl().setWidth(width);
                this.contextWindow.getLiveEditEl().getEl().setHeight(height);
            });
        }

        private getData():void {
            jQuery.ajax({
                url: "/admin/apps/content-manager/js/data/context-window/devices.json",
                success: (data:any, textStatus:string, jqXHR:JQueryXHR) => {
                    this.grid.updateData(EmulatorGrid.toSlickData(data));
                }
            });
        }
    }
}