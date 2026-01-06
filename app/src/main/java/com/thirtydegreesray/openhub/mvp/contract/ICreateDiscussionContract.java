package com.thirtydegreesray.openhub.mvp.contract;

import com.thirtydegreesray.openhub.mvp.contract.base.IBaseContract;
import com.thirtydegreesray.openhub.mvp.model.Discussion;
import com.thirtydegreesray.openhub.mvp.model.DiscussionCategory;

import java.util.ArrayList;

/**
 * Contract for creating discussions
 */

public interface ICreateDiscussionContract {

    interface View extends IBaseContract.View {
        void showNewDiscussion(Discussion discussion);
        void showCategories(ArrayList<DiscussionCategory> categories);
    }

    interface Presenter extends IBaseContract.Presenter<ICreateDiscussionContract.View> {
        String getTitle();
        String getBody();
        void createDiscussion(String title, String body);
    }

}
