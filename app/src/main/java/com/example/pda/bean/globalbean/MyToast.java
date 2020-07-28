package com.example.pda.bean.globalbean;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.pda.LoginActivity;

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
