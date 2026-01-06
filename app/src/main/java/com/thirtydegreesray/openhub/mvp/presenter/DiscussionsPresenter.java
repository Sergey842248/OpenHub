package com.thirtydegreesray.openhub.mvp.presenter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.error.HttpPageNoFoundError;
import com.thirtydegreesray.openhub.mvp.contract.IDiscussionsContract;
import com.thirtydegreesray.openhub.mvp.model.Discussion;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePagerPresenter;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Presenter for discussions functionality
 */

public class DiscussionsPresenter extends BasePagerPresenter<IDiscussionsContract.View>
        implements IDiscussionsContract.Presenter {

    @AutoAccess String owner;
    @AutoAccess String repoName;

    private ArrayList<Discussion> discussions;

    @Inject
    public DiscussionsPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void onViewInitialized() {
        super.onViewInitialized();
    }

    @Override
    protected void loadData() {
        loadDiscussions(1, false);
    }

    @Override
    public void loadDiscussions(final int page, final boolean isReload) {
        boolean readCacheFirst = page == 1 && !isReload;
        loadRepoDiscussions(page, isReload, readCacheFirst);
    }

    private void loadRepoDiscussions(final int page, final boolean isReload, final boolean readCacheFirst){
        mView.showLoading();
        HttpObserver<ArrayList<Discussion>> httpObserver =
                new HttpObserver<ArrayList<Discussion>>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.hideLoading();
                        handleError(error);
                    }

                    @Override
                    public void onSuccess(HttpResponse<ArrayList<Discussion>> response) {
                        mView.hideLoading();
                        handleSuccess(response.body(), isReload, readCacheFirst);
                    }
                };

        generalRxHttpExecute(new IObservableCreator<ArrayList<Discussion>>() {
            @Override
            public Observable<Response<ArrayList<Discussion>>> createObservable(boolean forceNetWork) {
                return getRepoService().getRepoDiscussions(forceNetWork, owner, repoName, page);
            }
        }, httpObserver, readCacheFirst);
    }

    private void handleError(Throwable error){
        if(!StringUtils.isBlankList(discussions)){
            mView.showErrorToast(getErrorTip(error));
        } else if(error instanceof HttpPageNoFoundError){
            mView.showDiscussions(new ArrayList<Discussion>());
        }else{
            mView.showLoadError(getErrorTip(error));
        }
    }

    private void handleSuccess(ArrayList<Discussion> resultDiscussions, boolean isReload, boolean readCacheFirst){
        if (isReload || discussions == null || readCacheFirst) {
            discussions = resultDiscussions;
        } else {
            discussions.addAll(resultDiscussions);
        }
        if (resultDiscussions.size() == 0 && discussions.size() != 0) {
            mView.setCanLoadMore(false);
        } else {
            mView.showDiscussions(discussions);
        }
    }

}
