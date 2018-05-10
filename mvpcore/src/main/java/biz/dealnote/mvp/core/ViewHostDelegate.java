package biz.dealnote.mvp.core;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.lang.ref.WeakReference;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public class ViewHostDelegate<P extends IPresenter<V>, V extends IMvpView> {

    private static final String SAVE_PRESENTER_STATE = "save_presenter_state";
    private static final int LOADER_ID = 101;

    private LoaderManager mLoaderManager;
    private Bundle mLastKnownPresenterState;

    private String mTag;

    private boolean mDelivered;
    private boolean mViewReady;
    private boolean mDestroyed;

    private P mPresenter;
    private WeakReference<V> mViewReference;

    private PresenterLifecycleCallback<P, V> mLifecycleCallback;

    public interface PresenterLifecycleCallback<P extends IPresenter<V>, V extends IMvpView> {
        void onPresenterDestroyed();

        void savePresenterState(@NonNull P presenter, @NonNull Bundle outState);

        void onPresenterReceived(@NonNull P presenter);

        IPresenterFactory<P> getPresenterFactory(@Nullable Bundle saveInstanceState);
    }

    public ViewHostDelegate(@NonNull PresenterLifecycleCallback<P, V> callback, String tag) {
        this.mLifecycleCallback = callback;
        this.mTag = tag;
    }

    public void onCreate(@NonNull final Context context, @NonNull V viewHost, @NonNull LoaderManager loaderManager, @Nullable Bundle savedInstanceState) {
        this.mLoaderManager = loaderManager;
        this.mViewReference = new WeakReference<>(viewHost);

        if (savedInstanceState != null) {
            this.mLastKnownPresenterState = savedInstanceState.getBundle(SAVE_PRESENTER_STATE);
        }

        final Context app = context.getApplicationContext();
        PresenterLoader<P> loader = (PresenterLoader<P>) loaderManager.initLoader(LOADER_ID, mLastKnownPresenterState, new androidx.loader.app.LoaderManager.LoaderCallbacks<P>() {
            @NonNull
            @Override
            public androidx.loader.content.Loader<P> onCreateLoader(int id, Bundle args) {
                return new PresenterLoader<>(app, mLifecycleCallback.getPresenterFactory(args), mTag);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<P> loader, P data) {
                onPresenterReceived(data);
            }

            @Override
            public void onLoaderReset(@NonNull Loader<P> loader) {
                mPresenter = null;
                onPresenterDestroyed();
            }
        });

        loader.setAdditionaLoadCompleteListener(mOnLoadCompleteListener);
        loader.startLoading();
    }

    @SuppressWarnings("unchecked")
    public void onDestroy() {
        androidx.loader.content.Loader loader = mLoaderManager.getLoader(LOADER_ID);
        if (loader instanceof PresenterLoader) {
            ((PresenterLoader) loader).setAdditionaLoadCompleteListener(null);
        }

        mPresenter.onViewHostDetached();
        mViewReference = new WeakReference<>(null);
        mLoaderManager = null;
        mDestroyed = true;
    }

    public void onViewCreated() {
        if(mViewReady) {
            return;
        }

        mViewReady = true;

        if (isPresenterPrepared()) {
            mPresenter.onGuiCreated(mViewReference.get());
        }
    }

    public void onDestroyView() {
        mViewReady = false;
        mPresenter.onGuiDestroyed();
    }

    public P getPresenter(){
        return mPresenter;
    }

    public boolean isViewReady() {
        return mViewReady;
    }

    public void onResume() {
        mPresenter.onGuiResumed();
    }

    public void onPause() {
        mPresenter.onGuiPaused();
    }

    public boolean isPresenterPrepared() {
        return mPresenter != null;
    }

    private androidx.loader.content.Loader.OnLoadCompleteListener<P> mOnLoadCompleteListener = new Loader.OnLoadCompleteListener<P>() {
        @Override
        public void onLoadComplete(@NonNull androidx.loader.content.Loader<P> loader, P data) {
            onPresenterReceived(data);
        }
    };

    private void onPresenterReceived(P presenter) {
        if (!mDelivered) {
            mPresenter = presenter;
            mPresenter.onViewHostAttached(mViewReference.get());
            mDelivered = true;

            onPresenterPrepared(presenter);
        }
    }

    private void onPresenterPrepared(P presenter) {
        mLifecycleCallback.onPresenterReceived(presenter);
    }

    private void onPresenterDestroyed() {
        mLifecycleCallback.onPresenterDestroyed();
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mPresenter != null) {
            mLastKnownPresenterState = new Bundle();
            mLifecycleCallback.savePresenterState(mPresenter, mLastKnownPresenterState);
        }

        outState.putBundle(SAVE_PRESENTER_STATE, mLastKnownPresenterState);
    }

    public boolean isDestroyed() {
        return mDestroyed;
    }
}