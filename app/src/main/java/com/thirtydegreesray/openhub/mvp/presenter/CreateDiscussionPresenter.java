package com.thirtydegreesray.openhub.mvp.presenter;

import androidx.annotation.NonNull;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.mvp.contract.ICreateDiscussionContract;
import com.thirtydegreesray.openhub.mvp.model.Discussion;
import com.thirtydegreesray.openhub.mvp.model.DiscussionCategory;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Presenter for creating discussions
 */

public class CreateDiscussionPresenter extends BasePresenter<ICreateDiscussionContract.View>
        implements ICreateDiscussionContract.Presenter {

    @AutoAccess String userId;
    @AutoAccess String repoName;

    @Inject
    public CreateDiscussionPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getBody() {
        return "";
    }

    @Override
    public void createDiscussion(@NonNull String title, @NonNull String body) {
        Discussion discussion = new Discussion();
        discussion.setTitle(title);
        discussion.setBody(body);

        HttpObserver<Discussion> httpObserver = new HttpObserver<Discussion>() {
            @Override
            public void onError(Throwable error) {
                mView.showErrorToast(getErrorTip(error));
            }

            @Override
            public void onSuccess(HttpResponse<Discussion> response) {
                mView.showNewDiscussion(response.body());
            }
        };

        generalRxHttpExecute(new IObservableCreator<Discussion>() {
            @Override
            public Observable<Response<Discussion>> createObservable(boolean forceNetWork) {
                return getRepoService().createDiscussion(forceNetWork, userId, repoName, discussion);
            }
        }, httpObserver, false, mView.getProgressDialog(getLoadTip()));
    }

}
