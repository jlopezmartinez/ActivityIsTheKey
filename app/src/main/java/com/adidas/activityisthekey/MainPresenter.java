package com.adidas.activityisthekey;

/**
 * Created by mengujua on 11/11/17.
 */

class MainPresenter {

    MainView mView;
    public void MainPresenter(MainView v) {
        mView = v;
    }

    public void onReadyToStart() {
        mView.showActivitySummary();
    }


    public void onUnknownError() {
        mView.showError("unknown");
    }

    public void onPermissionError() {
        mView.showError("permissions");
    }

    public void onMaxReached(int activity, int max) {

    }

    public interface MainView {
        void showActivitySummary();
        void showError(String error);


    }
}
