package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBaseListContract;
import com.thirtydegreesray.openhub.mvp.contract.base.IBasePagerContract;
import com.thirtydegreesray.openhub.mvp.model.Discussion;

import java.util.ArrayList;

/**
 * Contract for discussions functionality
 */

public interface IDiscussionsContract {

    interface View extends IBaseContract.View, IBasePagerContract.View, IBaseListContract.View{
        void showDiscussions(ArrayList<Discussion> discussions);
    }

    interface Presenter extends IBasePagerContract.Presenter<IDiscussionsContract.View>{
        void loadDiscussions(int page, boolean isReload);
    }

}
