module api_ui_combobox {

    export class ComboBoxSelectedOptionView<T> extends api_dom.DivEl{

        private option:OptionData<T>;

        private selectedOptionToBeRemovedListeners:{(toBeRemoved:ComboBoxSelectedOptionView<T>): void;}[] = [];

        constructor(option:OptionData<T>) {
            super("ComboBoxSelectedOptionView", "selected-option");
            this.option = option;
            this.layout();
        }

        getOption():OptionData<T> {
            return this.option;
        }

        layout() {
            var removeButtonEl = new api_dom.AEl(null, "remove");
            var optionValueEl = new api_dom.DivEl(null, 'option-value');
            optionValueEl.getEl().setInnerHtml(this.option.displayValue.toString());

            removeButtonEl.getEl().addEventListener('click', (event:Event) => {
                this.notifySelectedOptionToBeRemoved();

                event.stopPropagation();
                event.preventDefault();
                return false;
            });

            this.appendChild(removeButtonEl);
            this.appendChild(optionValueEl);
        }

        notifySelectedOptionToBeRemoved() {
            this.selectedOptionToBeRemovedListeners.forEach( (listener) => {
                listener(this);
            });
        }

        addSelectedOptionToBeRemovedListener(listener:{(toBeRemoved:ComboBoxSelectedOptionView<T>): void;}) {
            this.selectedOptionToBeRemovedListeners.push(listener);
        }

        removeSelectedOptionToBeRemovedListener(listener:{(toBeRemoved:ComboBoxSelectedOptionView<T>): void;}) {
            this.selectedOptionToBeRemovedListeners = this.selectedOptionToBeRemovedListeners.filter(function (curr) {
                return curr != listener;
            });
        }
    }
}