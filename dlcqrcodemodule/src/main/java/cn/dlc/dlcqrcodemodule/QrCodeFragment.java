package cn.dlc.dlcqrcodemodule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import com.trello.rxlifecycle2.android.FragmentEvent;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;

public class QrCodeFragment extends MyLazyFragment
    implements View.OnClickListener, QRCodeView.Delegate {

    public static final int REQUEST_GET_IMAGE = 233;
    public static final String TAG = QrCodeFragment.class.getSimpleName();

    protected ZXingView mZxingView;
    protected ImageButton mBtnTorch;
    protected ImageButton mBtnPicture;

    private boolean mIsTorchOn;
    private QrCodeListener mQrCodeListener;

    public interface QrCodeListener {

        /**
         * 解析成功
         *
         * @param result 扫描结果
         * @param fragment QrCodeFragment实例
         * @return true 表示自动再次扫描
         */
        boolean onScanQRCodeSuccess(String result, QrCodeFragment fragment);

        /**
         * 打开相机失败
         */
        void openCameraError();

        /**
         * 点击选择图片
         */
        void onClickSelectImage();

        /**
         * 解析失败
         */
        void onParseQRCodeFailure();
    }

    public static QrCodeFragment newInstance(boolean canParseImage) {
        Bundle args = new Bundle();
        args.putBoolean("canParseImage", canParseImage);
        QrCodeFragment qrCodeFragment = new QrCodeFragment();
        qrCodeFragment.setArguments(args);
        return qrCodeFragment;
    }

    /**
     * 把这个fragment加到界面上面
     *
     * @param fragmentManager
     * @param transaction
     * @param containerId
     * @param canParseImage
     * @return
     */
    public static QrCodeFragment addToParent(FragmentManager fragmentManager,
        FragmentTransaction transaction, int containerId, boolean canParseImage) {

        QrCodeFragment fragment = (QrCodeFragment) fragmentManager.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment = newInstance(canParseImage);
            transaction.add(containerId, fragment, TAG);
        } else {
            transaction.show(fragment);
        }
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof QrCodeListener) {
            mQrCodeListener = (QrCodeListener) context;
        } else {
            throw new RuntimeException((context.getClass().getName() + "必须实现 QrCodeListener"));
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_default_qr_code;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(getLayoutId(), container, false);

        mZxingView = view.findViewById(R.id.zxing_view);
        mZxingView.setDelegate(this);

        mBtnTorch = view.findViewById(R.id.btn_torch);
        mBtnTorch.setOnClickListener(this);

        mBtnPicture = view.findViewById(R.id.btn_picture);
        mBtnPicture.setOnClickListener(this);

        if (getArguments() != null) {
            boolean canParseImage = getArguments().getBoolean("canParseImage", true);
            if (!canParseImage) {
                mBtnPicture.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switchTorch(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_torch) {
            switchTorch(!mIsTorchOn);
        } else if (v.getId() == R.id.btn_picture) {
            mQrCodeListener.onClickSelectImage();
        }
    }

    /**
     * 切换闪光灯
     */
    private void switchTorch(boolean isTorchOn) {
        mIsTorchOn = isTorchOn;
        updateTorchButtonState(mIsTorchOn, mBtnTorch);
        if (mIsTorchOn) {
            mZxingView.openFlashlight();
        } else {
            mZxingView.closeFlashlight();
        }
    }

    /**
     * 更新按钮状态，可以重写这个方法，修改闪关灯的图标
     *
     * @param isTorchOn
     */
    protected void updateTorchButtonState(boolean isTorchOn, ImageButton torchButton) {
        if (isTorchOn) {
            torchButton.setImageResource(R.mipmap.ic_default_torch_on);
        } else {
            torchButton.setImageResource(R.mipmap.ic_default_torch_off);
        }
    }

    @Override
    public void onVisible() {
        // 开始预览摄像头
        mZxingView.startCamera();
        mZxingView.startSpotAndShowRect();
    }

    @Override
    public void onInvisible() {
        // 停止预览摄像头
        mZxingView.stopSpot();
        switchTorch(false);
        mZxingView.stopCamera();
    }

    /**
     * 开始识别
     */
    public void startSpot() {
        try {
            mZxingView.startSpot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止识别
     */
    public void stopSpot() {
        try {
            mZxingView.stopSpot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 震动
     */
    @SuppressLint("MissingPermission")
    private void vibrate() {
        try {
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        vibrate();
        // 回调
        if (mQrCodeListener.onScanQRCodeSuccess(result, this)) {
            // 再次扫描
            startSpot();
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        mQrCodeListener.openCameraError();
    }

    @Override
    protected void onDestroyUnbindView() {
        mZxingView.onDestroy();
    }

    /**
     * 解析图片
     *
     * @param imagePath
     */
    public void parseImage(final String imagePath) {
        Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                return Observable.just(QRCodeDecoder.syncDecodeQRCode(imagePath));
            }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(this.<String>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
            .subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(String s) {
                    onScanQRCodeSuccess(s);
                }

                @Override
                public void onError(Throwable e) {
                    mQrCodeListener.onParseQRCodeFailure();
                }

                @Override
                public void onComplete() {

                }
            });
    }
}
