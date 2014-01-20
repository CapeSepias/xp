module api.ui.tab {

    export interface TabBarItemOptions {
        removable?:boolean;
    }

    export class TabBarItem extends api.dom.LiEl implements api.ui.PanelNavigationItem, api.event.Observable {

        private label: string;

        private index: number;

        private active:boolean = false;

        private removable:boolean = true;

        private listeners: TabBarItemListener[] = [];

        constructor(label:string, options:TabBarItemOptions = {}) {
            super("tab-bar-item");

            this.setLabel(label);
            this.removable = options.removable;

            this.getEl().addEventListener("click", (event) => {
                this.notifySelectedListeners();
            });
        }

        setIndex(value:number) {
            this.index = value;
        }

        getIndex():number {
            return this.index;
        }

        setLabel(value:string) {
            this.label = value;
            this.getEl().setInnerHtml(value);
            this.getEl().setAttribute('title', value);
        }

        getLabel():string {
            return this.label;
        }

        setActive(value:boolean) {
            this.active = value;
            this.active ? this.addClass("active") : this.removeClass("active");
        }

        isActive():boolean {
            return this.active;
        }

        setRemovable(value:boolean) {
            this.removable = value;
        }

        isRemovable():boolean {
            return this.removable;
        }

        addListener(listener:TabBarItemListener) {
            this.listeners.push(listener);
        }

        removeListener(listener:TabBarItemListener) {
            this.listeners = this.listeners.filter((elem) => {
                return elem != listener;
            });
        }

        private notifySelectedListeners() {
            this.listeners.forEach((listener:TabBarItemListener) => {
                if (listener.onSelected) {
                    listener.onSelected(this);
                }
            });
        }

    }

}