Ext.define('Admin.view.contentManager.contextwindow.emulator.Emulator', {
    extend: 'Ext.container.Container',
    alias: 'widget.contextWindowEmulator',
    uses: 'Admin.view.contentManager.contextwindow.Helper',

    contextWindow: undefined,
    title: 'Emulator',

    layout: {
        type: 'fit'
    },

    DEVICES_URL: '../admin/apps/content-manager/js/data/context-window/devices.json',

    topTextCmp: undefined,
    listView: undefined,

    deviceOrientation: 'vertical', // vertical|horizontal

    initComponent: function () {
        this.topTextCmp = this.createTopTextCmp();
        this.listView = this.createListView();
        this.items = [
            this.topTextCmp,
            this.listView
        ];

        this.callParent(arguments);
    },

    /**
     * @returns {Ext.Component}
     */
    createTopTextCmp: function () {
        return new Ext.Component({
            cls: 'admin-emulator-top-bar',
            html: '<div>Emulate different client\'s physical sizes</div>'
        });
    },

    /**
     * @returns {Ext.view.View}
     */
    createListView: function () {
        var me = this;
        var monitorFullModelData = {
            "name": "Monitor full (default)",
            "device_type": "monitor_full",
            "width": "100%",
            "height": "100%",
            "rotatable": false
        };

        // fixme: formalize model, store 'n stuff

        Ext.define('Admin.ContextWindow.DeviceModel', {
            extend: 'Ext.data.Model',
            fields: [
                { name: 'name', type: 'string' },
                { name: 'device_type', type: 'string' },
                { name: 'width', type: 'auto' },
                { name: 'height', type: 'auto' },
                { name: 'rotatable', type: 'boolean' }
            ]
        });

        Ext.create('Ext.data.Store', {
            id: 'contextWindowDeviceStore',
            model: 'Admin.ContextWindow.DeviceModel',
            proxy: {
                type: 'ajax',
                url: me.DEVICES_URL,
                reader: {
                    type: 'json',
                    root: 'devices'
                }
            },
            listeners: {
                load: function (store, records) {
                    var monitorFullModel = new Admin.ContextWindow.DeviceModel(monitorFullModelData);
                    store.insert(0, monitorFullModel);
                    me.listView.getSelectionModel().select(0);
                }
            },
            autoLoad: true
        });

        var template = new Ext.XTemplate(
            '<tpl for=".">',
            '   <div class="admin-cw-item">',
            '      <div class="admin-cw-item-row">',
            '           <div class="admin-cw-item-icon {[this.getIconCls(values.device_type)]}"></div>',
            '           <div class="admin-cw-item-info">',
            '               <h3>{name}</h3>',
            '               <sub>{width} x {height}</sub>',
            '           </div>',
            '           <tpl if="rotatable">',
            '               <div class="admin-device-item-rotate-button icon-rotate-right" title="Rotate"></div>',
            '           </tpl>',
            '       </div>',
            '   </div>',
            '</tpl>',
            {
                getIconCls: function (deviceType) {
                    return Admin.view.contentManager.contextwindow.Helper.resolveDeviceTypeIconCls(deviceType);
                }
            }
        );

        return new Ext.view.View({
            flex: 1,
            store: Ext.getStore('contextWindowDeviceStore'),
            tpl: template,
            cls: 'admin-cw-items admin-device-items',
            itemSelector: 'div.admin-cw-item',
            emptyText: 'No devices available',
            selectedItemCls: 'admin-device-item-selected',
            listeners: {
                itemclick: {
                    fn: me.onItemClick,
                    scope: me
                }
            }
        });
    },

    resizeLiveEditFrame: function (deviceModel) {
        var me = this,
            iFrameEl = Ext.get(me.contextWindow.getLiveEditIFrameDom().id),
            iFrameContainer = iFrameEl.parent(),
            deviceType = deviceModel.data.device_type,
            deviceIsRotatable = deviceModel.data.rotatable,
            width = deviceModel.data.width,
            height = deviceModel.data.height,
            useFullWidth = deviceType === 'monitor_full',
            newWidth = useFullWidth ? iFrameContainer.getWidth() : width,
            newHeight = useFullWidth ? iFrameContainer.getHeight() : height;

        if (this.deviceOrientation === 'horizontal' && deviceIsRotatable) {
            newWidth = height;
            newHeight = width;
        }
        iFrameEl.animate({
            duration: 200,
            easing: 'linear',
            to: {
                width: newWidth,
                height: newHeight
            },
            listeners: {
                afteranimate: function () {
                    if (useFullWidth) {
                        iFrameEl.setStyle('width', width);
                    }
                    if (useFullWidth) {
                        iFrameEl.setStyle('height', height);
                    }
                }
            }
        });
    },

    onItemClick: function (view, record, item, index, event) {
        var me = this;
        var targetIsRotateButton = Ext.fly(event.target).hasCls('admin-device-item-rotate-button');
        if (targetIsRotateButton) {
            me.deviceOrientation = me.deviceOrientation === 'vertical' ? 'horizontal' : 'vertical';
            me.rotateRotateButton(Ext.fly(event.target));
        } else {
            me.deviceOrientation = 'vertical';
            var rotateButtonDom = Ext.get(item).down('.admin-device-item-rotate-button');
            if (rotateButtonDom) {
                rotateButtonDom.removeCls('admin-device-item-rotate-button-horizontal');
            }
        }

        me.resizeLiveEditFrame(record);
    },

    rotateRotateButton: function (buttonEl) {
        if (this.deviceOrientation === 'horizontal') {
            buttonEl.addCls('admin-device-item-rotate-button-horizontal');
        } else {
            buttonEl.removeCls('admin-device-item-rotate-button-horizontal');
        }
    }

});