package com.example.pda.util;

import android.app.Application;
import android.view.Gravity;
import android.widget.Toast;

import com.example.pda.ui.LoginActivity;

public class ToastUtils {
    private static Toast toast;

    public static void showShort(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(LoginActivity.context, sequence, Toast.LENGTH_SHORT);
        } else {
            toast.cancel();
            toast = Toast.makeText(LoginActivity.context, sequence, Toast.LENGTH_LONG);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.TOP, 0, 70);
        toast.show();
    }

    public static void showLong(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(LoginActivity.context, sequence, Toast.LENGTH_LONG);
        } else {
            toast.cancel();
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.setGravity(Gravity.TOP, 0, 70);
        toast.show();
    }

}