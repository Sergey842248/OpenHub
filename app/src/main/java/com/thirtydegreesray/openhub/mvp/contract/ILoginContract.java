

package com.thirtydegreesray.openhub.mvp.contract;

import android.content.Intent;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.model.BasicToken;

/**
 * Created on 2017/7/12.
 *
 * @author ThirtyDegreesRay
 */

public interface ILoginContract {

    interface View extends IBaseContract.View{

        void onGetTokenSuccess(BasicToken basicToken);

        void onGetTokenError(String errorMsg);

        void onLoginComplete();

        void openOauthPage(String url);

    }

    interface Presenter extends IBaseContract.Presenter<ILoginContract.View>{

        void startOauthLogin();

        void loginWithPat(String pat);

        void handleOauth(Intent intent);

        void getUserInfo(BasicToken basicToken);

    }

}
