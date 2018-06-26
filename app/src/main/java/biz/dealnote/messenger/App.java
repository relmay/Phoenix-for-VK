package biz.dealnote.messenger;

import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.service.KeepLongpollService;
import biz.dealnote.messenger.settings.Settings;

public class App extends MultiDexApplication {

    //noinspection ResourceType
    private static App sInstanse;

    @Override
    public void onCreate() {
        sInstanse = this;
        AppCompatDelegate.setDefaultNightMode(Settings.get().ui().getNightMode());

        super.onCreate();

        Settings.get()
                .main()
                .incrementRunCount();

        PicassoInstance.init(this, Injection.provideProxySettings());

        if(Settings.get().other().isKeepLongpoll()){
            KeepLongpollService.start(this);
        }
    }

    @NonNull
    public static App getInstance() {
        if (sInstanse == null) {
            throw new IllegalStateException("App instance is null!!! WTF???");
        }

        return sInstanse;
    }
}