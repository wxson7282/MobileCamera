package com.wxson.mobilecontroller.connection;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.wxson.mobilecontroller.R;

/**
 * Created by wxson on 2018/3/5.
 * Package com.wxson.remote_camera.connection.
 */

public class LoadingDialog extends Dialog {

    private ImageView ivLoading;
    private TextView tvHint;
    private Animation animation;

    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialogTheme);
        setContentView(R.layout.dialog_loading);
        ivLoading = (ImageView) findViewById(R.id.iv_loading);
        tvHint = (TextView) findViewById(R.id.tv_hint);
        animation = AnimationUtils.loadAnimation(context, R.anim.loading_dialog);
    }

    public void show(String hintText, boolean cancelable, boolean canceledOnTouchOutside) {
        setCancelable(cancelable);
        setCanceledOnTouchOutside(canceledOnTouchOutside);
        tvHint.setText(hintText);
        ivLoading.startAnimation(animation);
        show();
    }

//    public void show(@StringRes int hintTextRes, boolean cancelable, boolean canceledOnTouchOutside) {
//        setCancelable(cancelable);
//        setCanceledOnTouchOutside(canceledOnTouchOutside);
//        tvHint.setText(hintTextRes);
//        ivLoading.startAnimation(animation);
//        show();
//    }

    @Override
    public void cancel() {
        super.cancel();
        animation.cancel();
        ivLoading.clearAnimation();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animation.cancel();
        ivLoading.clearAnimation();
    }

}
