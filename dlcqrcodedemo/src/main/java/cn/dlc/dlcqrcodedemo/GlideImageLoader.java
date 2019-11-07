package cn.dlc.dlcqrcodedemo;

import android.app.Activity;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.lzy.imagepicker.loader.ImageLoader;

/**
 * Created by John on 2018/3/24.
 */

public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width,
        int height) {

        Glide.with(activity).load(path).into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, String path, ImageView imageView, int width,
        int height) {

        Glide.with(activity).load(path).into(imageView);
    }

    @Override
    public void clearMemoryCache() {

    }
}
