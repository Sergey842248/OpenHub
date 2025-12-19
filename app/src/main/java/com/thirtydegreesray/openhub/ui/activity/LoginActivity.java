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

    @BindView(R2.id.username_et) TextInputEditText usernameEt;
    @BindView(R2.id.username_layout) TextInputLayout usernameLayout;
    @BindView(R2.id.password_et) TextInputEditText passwordEt;
    @BindView(R2.id.password_layout) TextInputLayout passwordLayout;
    @BindView(R2.id.login_bn) SubmitButton loginBn;

    private String personalAccessToken;
    private String userName;
    private String password;

    @Override
    public void onGetTokenSuccess(BasicToken basicToken) {
        loginPatBn.doResult(true);
        loginBn.doResult(true);
        mPresenter.getUserInfo(basicToken);
    }

    @Override
    public void onGetTokenError(String errorMsg) {
        loginPatBn.doResult(false);
        loginBn.doResult(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loginPatBn.reset();
                loginPatBn.setEnabled(true);
                loginBn.reset();
                loginBn.setEnabled(true);
            }
        }, 1000);

        Toasty.error(getApplicationContext(), errorMsg).show();
    }

    @Override
    public void onLoginComplete() {
        delayFinish();
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    @Override
    public void showOtpLoginDialog() {
        final androidx.appcompat.widget.AppCompatEditText editText = new androidx.appcompat.widget.AppCompatEditText(getActivity());
        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setTitle("Two-factor authentication")
                .setMessage("Enter the authentication code")
                .setView(editText)
                .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String otp = editText.getText().toString();
                        mPresenter.basicLogin(userName, password, otp);
                    }
                })
                .setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        loginBn.reset();
                        loginBn.setEnabled(true);
                    }
                })
                .show();
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
        if(patLoginCheck()){
            loginPatBn.setEnabled(false);
            mPresenter.loginWithPat(personalAccessToken);
        }else{
            loginPatBn.reset();
        }
    }

    @OnClick(R2.id.login_bn)
    public void onLoginClick(){
        if(basicLoginCheck()){
            loginBn.setEnabled(false);
            mPresenter.basicLogin(userName, password);
        }else{
            loginBn.reset();
        }
    }

    private boolean patLoginCheck(){
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

    private boolean basicLoginCheck(){
        boolean valid = true;
        userName = usernameEt.getText().toString();
        password = passwordEt.getText().toString();
        if(StringUtils.isBlank(userName)){
            valid = false;
            usernameLayout.setError(getString(R.string.user_name_warning));
        }else{
            usernameLayout.setErrorEnabled(false);
        }
        if(StringUtils.isBlank(password)){
            valid = false;
            passwordLayout.setError(getString(R.string.password_warning));
        }else{
            passwordLayout.setErrorEnabled(false);
        }
        return valid;
    }
}
