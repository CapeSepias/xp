module api.form {

    import PropertySet = api.data.PropertySet;
    import PropertyArray = api.data.PropertyArray;
    import PropertyPath = api.data.PropertyPath;
    import PropertyPathElement = api.data.PropertyPathElement;
    import Property = api.data.Property;
    import Value = api.data.Value;
    import ValueType = api.data.ValueType;
    import ValueTypes = api.data.ValueTypes;
    import PropertyTree = api.data.PropertyTree;

    export interface FormItemSetOccurrenceViewConfig {

        context: FormContext;

        formItemSetOccurrence: FormItemSetOccurrence;

        formItemSet: FormItemSet;

        parent: FormItemSetOccurrenceView;

        dataSet: PropertySet
    }

    export class FormItemSetOccurrenceView extends FormItemOccurrenceView {

        private context: FormContext;

        private formItemSetOccurrence: FormItemSetOccurrence;

        private formItemSet: FormItemSet;

        private removeButton: api.dom.AEl;

        private label: FormItemSetLabel;

        private constructedWithData: boolean;

        private parent: FormItemSetOccurrenceView;

        private propertySet: PropertySet;

        private formItemLayer: FormItemLayer;

        private formItemViews: FormItemView[] = [];

        private helpText: HelpTextContainer;

        private formItemSetOccurrencesContainer: api.dom.DivEl;

        private validityChangedListeners: {(event: RecordingValidityChangedEvent): void}[] = [];

        private previousValidationRecording: ValidationRecording;

        constructor(config: FormItemSetOccurrenceViewConfig) {
            super("form-item-set-occurrence-view", config.formItemSetOccurrence);
            this.context = config.context;
            this.formItemSetOccurrence = config.formItemSetOccurrence;
            this.formItemSet = config.formItemSet;
            this.parent = config.parent;
            this.constructedWithData = config.dataSet != null;
            this.propertySet = config.dataSet;

            this.formItemLayer = new FormItemLayer(config.context);
        }

        getDataPath(): PropertyPath {

            return this.propertySet.getProperty().getPath();
        }

        public layout(validate: boolean = true): wemQ.Promise<void> {

            var deferred = wemQ.defer<void>();

            this.removeButton = new api.dom.AEl("remove-button");
            this.appendChild(this.removeButton);
            this.removeButton.onClicked((event: MouseEvent) => {
                this.notifyRemoveButtonClicked();
                event.stopPropagation();
                event.preventDefault();
                return false;
            });

            this.label = new FormItemSetLabel(this.formItemSet);
            this.appendChild(this.label);


            if (this.formItemSet.getHelpText()) {
                this.helpText = new HelpTextContainer(this.formItemSet.getHelpText());

                this.label.appendChild(this.helpText.getToggler());
                this.appendChild(this.helpText.getHelpText());

                this.toggleHelpText(this.formItemSet.isHelpTextOn());
            }


            this.formItemSetOccurrencesContainer = new api.dom.DivEl("form-item-set-occurrences-container");
            this.appendChild(this.formItemSetOccurrencesContainer);

            var layoutPromise: wemQ.Promise<FormItemView[]> = this.formItemLayer.setFormItems(
                this.formItemSet.getFormItems()).setParentElement(this.formItemSetOccurrencesContainer).setParent(this).layout(
                this.propertySet, validate);

            layoutPromise.then((formItemViews: FormItemView[]) => {

                this.formItemViews = formItemViews;
                if (validate) {
                    this.validate(true);
                }

                this.formItemViews.forEach((formItemView: FormItemView) => {
                    formItemView.onValidityChanged((event: RecordingValidityChangedEvent) => {

                        if (!this.previousValidationRecording) {
                            return; // previousValidationRecording is initialized on validate() call which may not be triggered in some cases
                        }

                        var previousValidState = this.previousValidationRecording.isValid();
                        if (event.isValid()) {
                            this.previousValidationRecording.removeByPath(event.getOrigin(), false, event.isIncludeChildren());
                        } else {
                            this.previousValidationRecording.flatten(event.getRecording());
                        }

                        if (previousValidState != this.previousValidationRecording.isValid()) {
                            this.notifyValidityChanged(new RecordingValidityChangedEvent(this.previousValidationRecording,
                                this.resolveValidationRecordingPath()).setIncludeChildren(true));
                        }
                    });
                });

                this.refresh();
                deferred.resolve(null);
            }).catch((reason: any) => {
                api.DefaultErrorHandler.handle(reason);
            }).done();

            return deferred.promise;
        }

        public update(propertyArray: PropertyArray, unchangedOnly?: boolean): wemQ.Promise<void> {
            var set = propertyArray.getSet(this.formItemSetOccurrence.getIndex());
            if (!set) {
                set = propertyArray.addSet();
            }
            this.propertySet = set;
            return this.formItemLayer.update(this.propertySet, unchangedOnly);
        }

        getFormItemViews(): FormItemView[] {
            return this.formItemViews;
        }

        giveFocus() {
            var focusGiven = false;
            this.getFormItemViews().forEach((formItemView: FormItemView) => {
                if (!focusGiven && formItemView.giveFocus()) {
                    focusGiven = true;
                }
            });
            return focusGiven;
        }

        refresh() {

            if (!this.formItemSetOccurrence.oneAndOnly()) {
                this.label.addClass("drag-control");
            } else {
                this.label.removeClass("drag-control");
            }

            this.removeButton.setVisible(this.formItemSetOccurrence.isRemoveButtonRequired());
        }

        onEditContentRequest(listener: (content: api.content.ContentSummary) => void) {
            this.formItemViews.forEach((formItemView: FormItemView) => {
                formItemView.onEditContentRequest(listener);
            });
        }

        unEditContentRequest(listener: (content: api.content.ContentSummary) => void) {
            this.formItemViews.forEach((formItemView: FormItemView) => {
                formItemView.unEditContentRequest(listener);
            });
        }

        showContainer(show: boolean) {
            if (show) {
                this.formItemSetOccurrencesContainer.show();
            } else {
                this.formItemSetOccurrencesContainer.hide();
            }
        }

        private resolveValidationRecordingPath(): ValidationRecordingPath {
            return new ValidationRecordingPath(this.getDataPath(), null);
        }

        getLastValidationRecording(): ValidationRecording {
            return this.previousValidationRecording;
        }

        public displayValidationErrors(value: boolean) {
            this.formItemViews.forEach((view: FormItemView) => {
                view.displayValidationErrors(value);
            });
        }

        public setHighlightOnValidityChange(highlight: boolean) {
            this.formItemViews.forEach((view: FormItemView) => {
                view.setHighlightOnValidityChange(highlight);
            });
        }

        hasValidUserInput(): boolean {

            var result = true;
            this.formItemViews.forEach((formItemView: FormItemView) => {
                if (!formItemView.hasValidUserInput()) {
                    result = false;
                }
            });
            return result;
        }

        toggleHelpText(show?: boolean) {
            if (!!this.helpText) {
                this.helpText.toggleHelpText(show);
            }
        }
        
        validate(silent: boolean = true): ValidationRecording {

            var allRecordings = new ValidationRecording();
            this.formItemViews.forEach((formItemView: FormItemView) => {
                var currRecording = formItemView.validate(silent);
                allRecordings.flatten(currRecording);

            });

            if (!silent) {
                if (allRecordings.validityChanged(this.previousValidationRecording)) {
                    this.notifyValidityChanged(new RecordingValidityChangedEvent(allRecordings, this.resolveValidationRecordingPath()));
                }
            }
            this.previousValidationRecording = allRecordings;
            return allRecordings;
        }

        onValidityChanged(listener: (event: RecordingValidityChangedEvent)=>void) {
            this.validityChangedListeners.push(listener);
        }

        unValidityChanged(listener: (event: RecordingValidityChangedEvent)=>void) {
            this.validityChangedListeners.filter((currentListener: (event: RecordingValidityChangedEvent)=>void) => {
                return listener == currentListener;
            });
        }

        private notifyValidityChanged(event: RecordingValidityChangedEvent) {
            this.validityChangedListeners.forEach((listener: (event: RecordingValidityChangedEvent)=>void) => {
                listener(event);
            });
        }

        onFocus(listener: (event: FocusEvent) => void) {
            this.formItemViews.forEach((formItemView) => {
                formItemView.onFocus(listener);
            });
        }

        unFocus(listener: (event: FocusEvent) => void) {
            this.formItemViews.forEach((formItemView) => {
                formItemView.unFocus(listener);
            });
        }

        onBlur(listener: (event: FocusEvent) => void) {
            this.formItemViews.forEach((formItemView) => {
                formItemView.onBlur(listener);
            });
        }

        unBlur(listener: (event: FocusEvent) => void) {
            this.formItemViews.forEach((formItemView) => {
                formItemView.unBlur(listener);
            });
        }
    }

}