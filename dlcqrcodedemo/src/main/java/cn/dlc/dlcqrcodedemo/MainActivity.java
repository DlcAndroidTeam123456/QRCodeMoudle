package cn.dlc.dlcqrcodedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import cn.dlc.commonlibrary.ui.base.BaseCommonActivity;
import cn.dlc.dlcqrcodemodule.QrCodeFragment;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import java.util.ArrayList;

public class MainActivity extends BaseCommonActivity
    implements QrCodeFragment.QrCodeListener // 必须实现这个接口 
{

    private QrCodeFragment mQrCodeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 创建并添加二维码Fragment
        mQrCodeFragment =
            QrCodeFragment.addToParent(fragmentManager, fragmentTransaction, R.id.qr_code_container,
                true);

        // 提交Fragment事务
        fragmentTransaction.commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 选择图片后返回
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == QrCodeFragment.REQUEST_GET_IMAGE) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(
                    ImagePicker.EXTRA_RESULT_ITEMS);

                if (images != null && images.size() > 0) {
                    // 解析图片二维码
                    mQrCodeFragment.parseImage(images.get(0).path);
                }
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onScanQRCodeSuccess(String result, QrCodeFragment fragment) {

        showOneToast("扫描结果=" + result);

        // 弹对话框界面啥的搞事情
        fragment.startSpot(); // 再扫描
        // 这里返回false表示不自动再此扫描，可以手动调用fragment.startSpot()再次扫描
        // 返回true就表示会连续扫描了
        return false;
    }

    @Override
    public void openCameraError() {
        showOneToast("打开相机失败");
    }

    @Override
    public void onParseQRCodeFailure() {
        showOneToast("解析二维码失败");
    }

    @Override
    public void onClickSelectImage() {

        // 选择图片
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setMultiMode(false);

        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, QrCodeFragment.REQUEST_GET_IMAGE);
    }
}
