package com.thirtydegreesray.openhub.mvp.presenter;

import android.content.Intent;
import android.net.Uri;

import com.thirtydegreesray.openhub.AppConfig;
import com.thirtydegreesray.openhub.AppData;
import com.thirtydegreesray.openhub.dao.AuthUser;
import com.thirtydegreesray.openhub.dao.AuthUserDao;
import com.thirtydegreesray.openhub.dao.DaoSession;
import com.thirtydegreesray.openhub.http.core.HttpObserver;
import com.thirtydegreesray.openhub.http.core.HttpResponse;
import com.thirtydegreesray.openhub.http.core.HttpSubscriber;
import com.thirtydegreesray.openhub.mvp.contract.ILoginContract;
import com.thirtydegreesray.openhub.mvp.model.BasicToken;
import com.thirtydegreesray.openhub.mvp.model.OauthToken;
import com.thirtydegreesray.openhub.mvp.model.User;
import com.thirtydegreesray.openhub.mvp.presenter.base.BasePresenter;
import com.thirtydegreesray.openhub.util.OAuthPkceUtil;
import com.thirtydegreesray.openhub.util.PrefUtils;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

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

    private static final String PREF_OAUTH_STATE = "oauth_state";
    private static final String PREF_OAUTH_CODE_VERIFIER = "oauth_code_verifier";

    @Inject
    public LoginPresenter(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void startOauthLogin() {
        if (StringUtils.isBlank(AppConfig.OPENHUB_CLIENT_ID)) {
            mView.onGetTokenError("GitHub OAuth client id is not configured");
            return;
        }

        String state = UUID.randomUUID().toString();
        String codeVerifier = OAuthPkceUtil.generateCodeVerifier();
        String codeChallenge = OAuthPkceUtil.generateS256CodeChallenge(codeVerifier);

        PrefUtils.set(PREF_OAUTH_STATE, state);
        PrefUtils.set(PREF_OAUTH_CODE_VERIFIER, codeVerifier);

        Uri oauthUri = Uri.parse(AppConfig.OAUTH2_URL).buildUpon()
                .appendQueryParameter("client_id", AppConfig.OPENHUB_CLIENT_ID)
                .appendQueryParameter("redirect_uri", AppConfig.REDIRECT_URL)
                .appendQueryParameter("scope", AppConfig.OAUTH2_SCOPE)
                .appendQueryParameter("state", state)
                .appendQueryParameter("code_challenge", codeChallenge)
                .appendQueryParameter("code_challenge_method", "S256")
                .build();

        mView.openOauthPage(oauthUri.toString());
    }

    @Override
    public void loginWithPat(String pat) {
        BasicToken basicToken = new BasicToken();
        basicToken.setToken(pat);
        basicToken.setScopes(new ArrayList<>());
        mView.onGetTokenSuccess(basicToken);
    }

    @Override
    public void handleOauth(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }

        Uri uri = intent.getData();
        if (uri == null || !"openhub".equals(uri.getScheme()) || !"login".equals(uri.getHost())) {
            return;
        }

        String error = uri.getQueryParameter("error");
        if (!StringUtils.isBlank(error)) {
            clearOauthTemp();
            mView.onGetTokenError(error);
            return;
        }

        String code = uri.getQueryParameter("code");
        String state = uri.getQueryParameter("state");

        if (StringUtils.isBlank(code) || StringUtils.isBlank(state)) {
            clearOauthTemp();
            mView.onGetTokenError("Invalid OAuth response");
            return;
        }

        String expectedState = PrefUtils.getDefaultSp().getString(PREF_OAUTH_STATE, null);
        if (StringUtils.isBlank(expectedState) || !expectedState.equals(state)) {
            clearOauthTemp();
            mView.onGetTokenError("OAuth state mismatch");
            return;
        }

        String codeVerifier = PrefUtils.getDefaultSp().getString(PREF_OAUTH_CODE_VERIFIER, null);

        String clientSecret = StringUtils.isBlank(AppConfig.OPENHUB_CLIENT_SECRET) ? null : AppConfig.OPENHUB_CLIENT_SECRET;
        Observable<Response<OauthToken>> observable = getLoginService().getAccessToken(
                AppConfig.OPENHUB_CLIENT_ID,
                clientSecret,
                code,
                AppConfig.REDIRECT_URL,
                state,
                codeVerifier
        );

        mView.showProgressDialog(getLoadTip());
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSubscriber<>(new HttpObserver<OauthToken>() {
                    @Override
                    public void onError(Throwable error) {
                        clearOauthTemp();
                        mView.dismissProgressDialog();
                        mView.onGetTokenError(getErrorTip(error));
                    }

                    @Override
                    public void onSuccess(HttpResponse<OauthToken> response) {
                        clearOauthTemp();
                        mView.dismissProgressDialog();
                        if (!response.isSuccessful() || response.body() == null) {
                            mView.onGetTokenError(response.getOriResponse().message());
                            return;
                        }

                        OauthToken oauthToken = response.body();
                        if (oauthToken == null || StringUtils.isBlank(oauthToken.getAccessToken())) {
                            String errorTip = oauthToken != null && !StringUtils.isBlank(oauthToken.getErrorDescription())
                                    ? oauthToken.getErrorDescription()
                                    : "Get access token error";
                            mView.onGetTokenError(errorTip);
                            return;
                        }

                        mView.onGetTokenSuccess(BasicToken.generateFromOauthToken(oauthToken));
                    }
                }));
    }

    private void clearOauthTemp() {
        PrefUtils.clearKey(PREF_OAUTH_STATE);
        PrefUtils.clearKey(PREF_OAUTH_CODE_VERIFIER);
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
                        if (response.body() != null) {
                            mView.dismissProgressDialog();
                            saveAuthUser(basicToken, response.body());
                            mView.onLoginComplete();
                        } else {
                            mView.dismissProgressDialog();
                            mView.onGetTokenError(getErrorTip(new Throwable("Get user info error")));
                        }
                    }
                }
        );
        Observable<Response<User>> observable = getUserService(basicToken.getToken()).getPersonInfo(true);
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
