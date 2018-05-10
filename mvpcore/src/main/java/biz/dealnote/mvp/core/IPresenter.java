package biz.dealnote.mvp.core;

import androidx.annotation.NonNull;

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
public interface IPresenter<V extends IMvpView> {

    void onViewHostAttached(@NonNull V viewHost);
    void onViewHostDetached();
    boolean isViewHostAttached();

    void onGuiCreated(@NonNull V viewHost);
    void onGuiDestroyed();
    void onGuiResumed();
    void onGuiPaused();

    boolean isGuiReady();
    boolean isGuiResumed();

    void onDestroyed();
}