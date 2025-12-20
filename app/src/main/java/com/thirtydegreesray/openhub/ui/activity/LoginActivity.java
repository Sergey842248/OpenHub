package com.thirtydegreesray.openhub.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.inject.module.ActivityModule;
import com.thirtydegreesray.openhub.mvp.contract.ILoginContract;
import com.thirtydegreesray.openhub.mvp.model.BasicToken;
import com.thirtydegreesray.openhub.mvp.presenter.LoginPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.BaseActivity;
import com.thirtydegreesray.openhub.util.StringUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unstoppable.submitbuttonview.SubmitButton;

import butterknife.BindView;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

/**
 * Created on 2017/7/12.
 *
 * @author ThirtyDegreesRay
 */

public class LoginActivity extends BaseActivity<LoginPresenter>
        implements ILoginContract.View {

    private final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R2.id.pat_et) TextInputEditText patEt;
    @BindView(R2.id.pat_layout) TextInputLayout patLayout;
    @BindView(R2.id.login_pat_bn) SubmitButton loginPatBn;

    private String personalAccessToken;

    @Override
    public void onGetTokenSuccess(BasicToken basicToken) {
        loginPatBn.doResult(true);
        mPresenter.getUserInfo(basicToken);
    }

    @Override
    public void onGetTokenError(String errorMsg) {
        loginPatBn.doResult(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginPatBn.reset();
                loginPatBn.setEnabled(true);
            }
        }, 1000);

        Toasty.error(getApplicationContext(), errorMsg).show();
    }

    @Override
    public void onLoginComplete() {
        delayFinish();
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    /**
     * 依赖注入的入口
     *
     * @param appComponent appComponent
     */
    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(new ActivityModule(getActivity()))
                .build()
                .inject(this);
    }

    /**
     * 获取ContentView id
     *
     * @return
     */
    @Override
    protected int getContentView() {
        return R.layout.activity_login_new;
    }

    /**
     * 初始化view
     *
     * @param savedInstanceState
     */
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @OnClick(R2.id.login_pat_bn)
    public void onLoginPatClick(){
        if(loginCheck()){
            loginPatBn.setEnabled(false);
            mPresenter.loginWithPat(personalAccessToken);
        }else{
            loginPatBn.reset();
        }
    }

    private boolean loginCheck(){
        boolean valid = true;
        personalAccessToken = patEt.getText().toString();
        if(StringUtils.isBlank(personalAccessToken)){
            valid = false;
            patLayout.setError(getString(R.string.personal_access_token_hint));
        }else{
            patLayout.setErrorEnabled(false);
        }
        return valid;
    }
}
