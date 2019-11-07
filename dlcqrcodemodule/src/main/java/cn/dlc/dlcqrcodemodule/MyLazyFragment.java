package cn.dlc.dlcqrcodemodule;

import android.view.View;
import cn.dlc.commonlibrary.ui.base.BaseCommonFragment;
import com.licheedev.myutils.LogPlus;

/**
 * 可以实现懒加载的Fragment，通过实现onVisiable()和onInvisiable()方法，可以实现在Fragment显示或者隐藏时进行相应操作
 *
 * @author Administrator
 */
abstract class MyLazyFragment extends BaseCommonFragment {
    /**
     * 判断是否已经进行显示时加载数据的操作
     */
    private boolean loaded;

    /**
     * Fragment显示时进行的操作
     */
    public abstract void onVisible();

    /**
     * Fragment隐藏时进行的操作
     */
    public abstract void onInvisible();

    /**
     * 根据是否可见，进行对应的处理
     *
     * @param isVisibleToUser
     */
    private void handleVisibleOrInvisibleEvent(boolean isVisibleToUser) {
        try {
            if (isVisibleToUser) {
                if (isVisible2() && !loaded) {
                    loaded = true;
                    onVisible();
                }
            } else {
                if (isVisible2() && loaded) {
                    loaded = false;
                    onInvisible();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        LogPlus.e("setUserVisibleHint,onStart");

        super.setUserVisibleHint(isVisibleToUser);
        handleVisibleOrInvisibleEvent(isVisibleToUser);
    }

    /**
     * 在onStart()方法中调用，会间接调用onVisiable()方法
     */
    private void doOnVisibleOnStart() {
        if (getUserVisibleHint() && isVisible2() && !loaded) {
            loaded = true;
            onVisible();
        }
    }

    private boolean isVisible2() {
        return isAdded()
            && !isHidden()
            && getView() != null
            && getView().getVisibility() == View.VISIBLE;
    }

    @Override
    public void onStart() {
        super.onStart();
        doOnVisibleOnStart();
    }

    /**
     * 在onStop()方法中调用，会间接调用onInvisiable()方法
     */
    private void doOnInvisibleOnStop() {
        if (getUserVisibleHint() && isVisible2() && loaded) {
            loaded = false;
            onInvisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        doOnInvisibleOnStop();
    }
}
