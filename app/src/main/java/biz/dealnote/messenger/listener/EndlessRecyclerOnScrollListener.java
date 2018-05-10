package biz.dealnote.messenger.listener;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import biz.dealnote.messenger.util.Objects;

public abstract class EndlessRecyclerOnScrollListener extends androidx.recyclerview.widget.RecyclerView.OnScrollListener {

    public EndlessRecyclerOnScrollListener() {

    }

    private static final int MIN_DELAY = 100;
    private static final int VISIBILITY_THRESHOLD = 0; //elements to the end

    private Long mLastInterceptTime;

    private boolean isAllowScrollIntercept(long minDelay){
        return mLastInterceptTime == null || System.currentTimeMillis() - mLastInterceptTime > minDelay;
    }

    @Override
    public void onScrolled(androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        androidx.recyclerview.widget.RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        if(Objects.isNull(manager)){
            // wtf?
            return;
        }

        if (!isAllowScrollIntercept(MIN_DELAY)) {
            return;
        }

        boolean isLastElementVisible = false;
        if (manager instanceof StaggeredGridLayoutManager) {
            isLastElementVisible = isAtLastElementOfStaggedGridLayoutManager((androidx.recyclerview.widget.StaggeredGridLayoutManager) manager);
        }

        if (manager instanceof androidx.recyclerview.widget.LinearLayoutManager) {
            isLastElementVisible = isAtLastElementOfLinearLayoutManager((androidx.recyclerview.widget.LinearLayoutManager) manager);
        }

        if (manager instanceof GridLayoutManager) {
            isLastElementVisible = isAtLastElementOfGridLayoutManager((androidx.recyclerview.widget.GridLayoutManager) manager);
        }

        if (isLastElementVisible) {
            mLastInterceptTime = System.currentTimeMillis();
            onScrollToLastElement();
            return;
        }

        boolean isFirstElementVisible = false;

        if (manager instanceof androidx.recyclerview.widget.LinearLayoutManager) {
            isFirstElementVisible = ((LinearLayoutManager) manager).findFirstVisibleItemPosition() == 0;
        }

        if(isFirstElementVisible){
            mLastInterceptTime = System.currentTimeMillis();
            onScrollToFirstElement();
        }
    }

    private int visibleItemCount;
    private int totalItemCount;
    private int pastVisibleItems;

    private boolean isAtLastElementOfLinearLayoutManager(androidx.recyclerview.widget.LinearLayoutManager linearLayoutManager) {
        visibleItemCount = linearLayoutManager.getChildCount();
        totalItemCount = linearLayoutManager.getItemCount();
        pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
        return (visibleItemCount + pastVisibleItems) >= totalItemCount - VISIBILITY_THRESHOLD;
    }

    private boolean isAtLastElementOfGridLayoutManager(GridLayoutManager gridLayoutManager) {
        visibleItemCount = gridLayoutManager.getChildCount();
        totalItemCount = gridLayoutManager.getItemCount();
        pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();
        return (visibleItemCount + pastVisibleItems) >= totalItemCount - VISIBILITY_THRESHOLD;
    }

    private boolean isAtLastElementOfStaggedGridLayoutManager(StaggeredGridLayoutManager staggeredGridLayoutManager) {
        visibleItemCount = staggeredGridLayoutManager.getChildCount();
        totalItemCount = staggeredGridLayoutManager.getItemCount();
        int[] firstVisibleItems = new int[staggeredGridLayoutManager.getSpanCount()];
        firstVisibleItems = staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);

        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
            pastVisibleItems = firstVisibleItems[0];
        }

        return (visibleItemCount + pastVisibleItems) >= totalItemCount - VISIBILITY_THRESHOLD;
    }

    public abstract void onScrollToLastElement();

    public void onScrollToFirstElement() {

    }
}