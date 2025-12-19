

package com.thirtydegreesray.openhub.mvp.presenter;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.thirtydegreesray.openhub.AppConfig;
import com.thirtydegreesray.openhub.AppData;
import com.thirtydegreesray.openhub.dao.AuthUser;
import com.thirtydegreesray.openhub.dao.AuthUserDao;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.core.HttpSubscriber;
import com.thirtydegreesray.openhub.http.model.AuthRequestModel;
import com.thirtydegreesray.openhub.mvp.contract.ILoginContract;
import com.thirtydegreesray.openhub.mvp.model.BasicToken;
import com.thirtydegreesray.openhub.mvp.model.OauthToken;
import com.thirtydegreesray.openhub.mvp.model.User;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;
import com.thirtydegreesray.openhub.http.core.AppRetrofit;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import okhttp3.Credentials;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on 2017/7/12.
 *
 * @author ThirtyDegreesRay
 */

public class LoginPresenter extends BasePresenter<ILoginContract.View>
        implements ILoginContract.Presenter {

    @Inject
    public LoginPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void basicLogin(String userName, String password) {
        basicLogin(userName, password, null);
    }

    @Override
    public void basicLogin(String userName, String password, String otp) {
        AppRetrofit.INSTANCE.setOtp(otp);
        AuthRequestModel authRequestModel = AuthRequestModel.generate();
        String token = Credentials.basic(userName, password);
        Observable<Response<BasicToken>> observable =
                getLoginService(token).authorizations(authRequestModel);
        
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSubscriber<>(new HttpObserver<BasicToken>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.onGetTokenError(getErrorTip(error));
                    }

                    @Override
                    public void onSuccess(HttpResponse<BasicToken> response) {
                        if (response.isSuccessful()) {
                            BasicToken token = response.body();
                            if (token != null) {
                                mView.onGetTokenSuccess(token);
                            } else {
                                mView.onGetTokenError(response.getOriResponse().message());
                            }
                        } else {
                            if (response.getOriResponse().code() == 401) {
                                String otpHeader = response.getOriResponse().headers().get("X-GitHub-OTP");
                                if (otpHeader != null && otpHeader.startsWith("required")) {
                                    mView.showOtpLoginDialog();
                                } else {
                                    mView.onGetTokenError(response.getOriResponse().message());
                                }
                            } else {
                                mView.onGetTokenError(response.getOriResponse().message());
                            }
                        }
                    }
                }));
    }

    @Override
    public void loginWithPat(String pat) {
        BasicToken basicToken = new BasicToken();
        basicToken.setToken(pat);
        // Ensure scopes is not null to prevent NullPointerException when calling listToString
        basicToken.setScopes(new java.util.ArrayList<String>());
        mView.onGetTokenSuccess(basicToken);
    }

    @Override
    public void handleOauth(Intent intent) {
        // OAuth flow is no longer supported, this method will do nothing.
    }

    @Override
    public void getUserInfo(final BasicToken basicToken) {
        HttpSubscriber<User> subscriber = new HttpSubscriber<>(
                new HttpObserver<User>() {
                    @Override
                    public void onError(Throwable error) {
                        mView.dismissProgressDialog();
                        mView.onGetTokenError(getErrorTip(error));
                    }

                    @Override
                    public void onSuccess(HttpResponse<User> response) {
//                        mView.dismissProgressDialog();
                        if (response.body() != null) {
                            saveAuthUser(basicToken, response.body());
                            mView.onLoginComplete();
                        } else {
                            mView.dismissProgressDialog();
                            mView.onGetTokenError(getErrorTip(new Throwable("Get user info error")));
                        }
                    }
                }
        );
        Observable<Response<User>> observable = getUserService(basicToken.getToken()).
                getPersonInfo(true);
        generalRxHttpExecute(observable, subscriber);
        mView.showProgressDialog(getLoadTip());

    }

    private void saveAuthUser(BasicToken basicToken, User userInfo) {
        String updateSql = "UPDATE " + daoSession.getAuthUserDao().getTablename()
                + " SET " + AuthUserDao.Properties.Selected.columnName + " = 0";
        daoSession.getAuthUserDao().getDatabase().execSQL(updateSql);

        String deleteExistsSql = "DELETE FROM " + daoSession.getAuthUserDao().getTablename()
                + " WHERE " + AuthUserDao.Properties.LoginId.columnName
                + " = '" + userInfo.getLogin() + "'";
        daoSession.getAuthUserDao().getDatabase().execSQL(deleteExistsSql);

        AuthUser authUser = new AuthUser();
        String scope = StringUtils.listToString(basicToken.getScopes(), ",");
        Date date = new Date();
        authUser.setAccessToken(basicToken.getToken());
        authUser.setScope(scope);
        authUser.setAuthTime(date);
        authUser.setExpireIn(360 * 24 * 60 * 60);
        authUser.setSelected(true);
        authUser.setLoginId(userInfo.getLogin());
        authUser.setName(userInfo.getName());
        authUser.setAvatar(userInfo.getAvatarUrl());
        daoSession.getAuthUserDao().insert(authUser);

        AppData.INSTANCE.setAuthUser(authUser);
        AppData.INSTANCE.setLoggedUser(userInfo);
    }


}
