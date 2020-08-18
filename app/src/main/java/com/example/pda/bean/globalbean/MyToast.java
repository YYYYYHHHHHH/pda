package com.example.pda.bean.globalbean;

import android.view.Gravity;
import android.widget.Toast;

import com.example.pda.ui.LoginActivity;

public class MyToast {
    private static Toast toast = null;
    private static Object object = new Object();
    public static Toast getToast() {
        if (toast == null) {
            synchronized(object) {
                if (toast == null) {
                    toast = Toast.makeText(LoginActivity.context, "", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 70);
                }
            }
        }
        return toast;
    }
}
