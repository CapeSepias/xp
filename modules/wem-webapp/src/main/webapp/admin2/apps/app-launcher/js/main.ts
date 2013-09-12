///<reference path='../../../api/js/ExtJs.d.ts' />
///<reference path='../../../api/js/Mousetrap.d.ts' />
///<reference path='../../../api/js/api.d.ts' />

///<reference path='app/model/Application.ts' />
///<reference path='app/model/Applications.ts' />
///<reference path='app/model/UserStore.ts' />
///<reference path='app/model/Authenticator.ts' />
///<reference path='app/view/AppTile.ts' />
///<reference path='app/view/Branding.ts' />
///<reference path='app/view/CenterPanel.ts' />
///<reference path='app/view/HomeMainContainer.ts' />
///<reference path='app/view/LinksContainer.ts' />
///<reference path='app/view/InstallationInfo.ts' />
///<reference path='app/view/AppInfo.ts' />
///<reference path='app/view/LoginForm.ts' />
///<reference path='app/view/AppSelector.ts' />
///<reference path='app/view/AppLauncher.ts' />
///<reference path='app/view/VersionInfo.ts' />

///<reference path='app/launcher/LostConnectionDetector.ts' />
///<reference path='app/launcher/LostConnectionDetectorListener.ts' />

declare var Ext;
declare var Admin;
declare var CONFIG;

var USERSTORES:app_model.UserStore[] = [
    new app_model.UserStore('ABC', '1'),
    new app_model.UserStore('LDAP', '2'),
    new app_model.UserStore('Local', '3'),
    new app_model.UserStore('Some very long value', '4')
];

function isUserLoggedIn():boolean {
    // TODO create utility class for cookie handling, remove dependency to ExtJs
    var dummyCookie = Ext.util.Cookies.get('dummy_userIsLoggedIn');
    return dummyCookie && dummyCookie === 'true';
}

Ext.application({
    name: 'app-launcher',

    controllers: [
    ],

    launch: function () {
        var userLoggedIn = isUserLoggedIn();
        var mainContainer = new app_view.HomeMainContainer(api_util.getAbsoluteUri('admin/rest/ui/background.jpg'));
        var appLauncher = new app_view.AppLauncher(mainContainer);

        var homeBrandingPanel = mainContainer.getBrandingPanel();
        api_remote_util.RemoteSystemService.system_getSystemInfo({}, (result:api_remote_util.SystemGetSystemInfoResult) => {
            homeBrandingPanel.setInstallation(result.installationName);
            homeBrandingPanel.setVersion(result.version);
        });

        var linksPanel = new app_view.LinksContainer().
            addLink('Community', 'http://www.enonic.com/community').
            addLink('Documentation', 'http://www.enonic.com/docs').
            addLink('About', 'https://enonic.com/en/home/enonic-cms');

        var appInfoPanel = new app_view.AppInfo();

        var appSelector = new app_view.AppSelector(app_model.Applications.getAllApps());
        appSelector.onAppHighlighted((app:app_model.Application) => {
            appInfoPanel.showAppInfo(app);
        });
        appSelector.onAppUnhighlighted((app:app_model.Application) => {
            appInfoPanel.hideAppInfo();
        });
        appSelector.onAppSelected((app:app_model.Application) => {
            appLauncher.loadApplication(app);
        });

        var loginPanel = new app_view.LoginForm(new app_model.AuthenticatorImpl());
        loginPanel.setLicensedTo('Licensed to Enonic');
        loginPanel.setUserStores(USERSTORES, USERSTORES[1]);
        loginPanel.onUserAuthenticated((userName:string, userStore:app_model.UserStore) => {
            console.log('User logged in', userName, userStore);
            Ext.util.Cookies.set('dummy_userIsLoggedIn', 'true');
            loginPanel.hide();
            appSelector.show();
            appSelector.afterRender();
        });

        var centerPanel = mainContainer.getCenterPanel();
        centerPanel.appendRightColumn(loginPanel);
        centerPanel.appendRightColumn(appInfoPanel);
        centerPanel.appendRightColumn(linksPanel);
        centerPanel.appendLeftColumn(appSelector);

        if (userLoggedIn) {
            loginPanel.hide();
            appSelector.afterRender();
        } else {
            appSelector.hide();
        }

        api_dom.Body.get().appendChild(mainContainer);
    }
});

