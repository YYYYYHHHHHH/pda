package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.pda.bean.LoginBean;
import com.example.pda.bean.UserBean;
import com.example.pda.bean.globalbean.MyOkHttpClient;
import com.example.pda.bean.globalbean.MyToast;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@ContentView(R.layout.activity_loginactivity)
public class LoginActivity extends Activity implements View.OnLayoutChangeListener {
    //用xUtils进行控件绑定
    @ViewInject(R.id.iv_login_logo)
    ImageView iv_login_logo;
    @ViewInject(R.id.ll_login_logobg)
    LinearLayout ll_login_logobg;
    @ViewInject(R.id.ll_login_root)
    LinearLayout activityRootView;//需要操作的布局
    @ViewInject(R.id.isSave)
    CheckBox isSave;
    @ViewInject(R.id.name)
    EditText name;
    @ViewInject(R.id.pass)
    EditText pass;
    @ViewInject(R.id.login)
    Button btn;
    private Toast toast;
    private ZLoadingDialog dialog;
    private int screenHeight = 0;//屏幕高度
    private int keyHeight = 0; //软件盘弹起后所占高度
    public static Context context;
    private final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight(); //获取屏幕高度
        keyHeight = screenHeight / 3;//弹起高度为屏幕高度的1/3
        this.context = getBaseContext();
        toast = MyToast.getToast();
        SharedPreferences setinfo = getPreferences(Activity.MODE_PRIVATE);
        String isSave = setinfo.getString("isSave", "0");
        if ("1".equals(isSave)) {
            this.isSave.setChecked(true);
            name.setText(setinfo.getString("name", ""));
            pass.setText(setinfo.getString("pass", ""));
        }
    }

    @Event(value = R.id.login)
    private void onClick(View view) {
        String username = name.getText().toString().trim();
        String password = pass.getText().toString().trim();
        postRequest(username, password);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRootView.addOnLayoutChangeListener(this);//给需要操作的布局设置监听
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right,
                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
       /* old是改变前的左上右下坐标点值，没有old的是改变后的左上右下坐标点值
        现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起*/
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            ViewGroup.LayoutParams params = iv_login_logo.getLayoutParams();//获取布局，设置键盘弹起后logo的宽高
            params.height = 100;
            params.width = 100;
            iv_login_logo.setLayoutParams(params);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ll_login_logobg.getLayoutParams());
            lp.setMargins(0, 90, 0, 50);//设置包含logo的布局的位置
            ll_login_logobg.setLayoutParams(lp);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {//键盘收回后，logo恢复原来大小，位置同样回到初始位置
            ViewGroup.LayoutParams params = iv_login_logo.getLayoutParams();
            params.height = 200;
            params.width = 200;
            iv_login_logo.setLayoutParams(params);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ll_login_logobg.getLayoutParams());
            lp.setMargins(0, 270, 0, 150);
            ll_login_logobg.setLayoutParams(lp);
        }
    }

    private void postRequest(String username, String password) {
        //建立请求表单，添加上传服务器的参数
        RequestBody formBody = FormBody.create(MediaType.parse("application/json"), new Gson().toJson(new LoginBean(username, password)));
        //发起请求
        final Request request = new Request.Builder()
                .url("http://192.168.11.243/FirstPDAServer/home/UserLogin?username=" + username + "&" + "password=" + password)
                .get()
                .build();
        dialog = new ZLoadingDialog(LoginActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("登陆中")
                .show();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        //将服务器响应的参数response.body().string())发送到hanlder中，并更新ui
                        mHandler.obtainMessage(1, response.body().string()).sendToTarget();

                    } else {
                        dialog.cancel();
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (IOException e) {
                    dialog.cancel();
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        toast.setText("请求超时！");
                        toast.show();
                    }
                    if (e instanceof ConnectException) {
                        toast.setText("和服务器连接异常！");
                        toast.show();

                    }
                }
            }
        }).start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息", ReturnMessage);
                final UserBean userBean = new Gson().fromJson(ReturnMessage, UserBean.class);
                final int status = Integer.parseInt(userBean.getStatus());
                final String mes = userBean.getMsg();
                if (status > 0) {
                    Intent i = new Intent(LoginActivity.this, MenuActivity.class);
                    SharedPreferences setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
                    setinfo.edit().putString("user", new Gson().toJson(userBean)).commit();
                    SharedPreferences setinfo2 = getPreferences(Activity.MODE_PRIVATE);
                    if (isSave.isChecked()) {
                        setinfo2.edit()
                                .putString("name", name.getText().toString())
                                .putString("pass", pass.getText().toString())
                                .putString("isSave", "1")
                                .commit();
                    } else {
                        setinfo2.edit()
                                .putString("name", "")
                                .putString("pass", "")
                                .putString("isSave", "0")
                                .commit();
                    }
                    LoginActivity.this.finish();
                    startActivity(i);
                } else {
                    Toast ts = Toast.makeText(getBaseContext(), mes, Toast.LENGTH_LONG);
                    ts.show();
                }
                dialog.cancel();

            } else {

            }
        }
    };
}