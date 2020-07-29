package com.example.pda;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.example.pda.bean.UserBean;
import com.example.pda.bean.WhBean;
import com.example.pda.bean.WhListBean;
import com.example.pda.bean.globalbean.MyOkHttpClient;
import com.example.pda.bean.globalbean.MyToast;
import com.google.gson.Gson;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ContentView(R.layout.activity_choicehouse)
public class ChoiceHouse extends AppCompatActivity {
    @ViewInject(R.id.house_name)
    private EditText editText;
    @ViewInject(R.id.clear)
    private Button clearButton;
    @ViewInject(R.id.next)
    private Button nextButton;
    private final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    private Toast toast = MyToast.getToast();
    private List<WhBean> WhList = new ArrayList<>();
    private String whId;
    private UserBean userBean;
    private String menuid;
    private SharedPreferences setinfo;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        x.view().inject(this);
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        Intent intent = getIntent();
        menuid = intent.getStringExtra("menuid");
        getWhList();
    }

    @Event(value = R.id.house_name, type = View.OnClickListener.class)
    private void editTextOnClick(View view) {
        if (WhList.size() <= 0) {
            toast.setText("无数据");
            toast.show();
        } else {
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(ChoiceHouse.this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    WhBean wh = WhList.get(options1);
                    editText.setText(wh.getWhName());
                    whId = wh.getWhId();
                }
            })

                    .setDividerColor(Color.BLACK)
                    .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                    .setContentTextSize(20)//设置文字大小
                    .setOutSideCancelable(false)// default is true
                    .setTitleText("选择仓库")
                    .setCancelText("取消")
                    .setSubmitText("确定")
                    .build();
            pvOptions.setPicker(WhList);//条件选择器
            pvOptions.show();
        }
    }

    @Event(value = R.id.clear, type = View.OnClickListener.class)
    private void clearOnClick(View view) {
        editText.setText("");
    }

    @Event(value = R.id.next, type = View.OnClickListener.class)
    private void nextOnClick(View view) {
        Log.i("editText的值", String.valueOf(editText.getText()));
        if ("".equals(String.valueOf(editText.getText()))) {
            toast.setText("请先选择仓库");
            toast.show();
        } else {
            Intent i = new Intent(ChoiceHouse.this, ListActivity.class);
            i.putExtra("whId", whId);
            startActivity(i);
        }

    }

    private void getWhList() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/GetWhList?loginId=" + userBean.getStatus() + "&menuid=" + 1)
                .get()
                .build();
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
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (IOException e) {
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
                final WhListBean whListBean = new Gson().fromJson(ReturnMessage, WhListBean.class);
                WhList = whListBean.getRows();
                if (WhList.size() == 1) {
                    editText.setText(WhList.get(0).getWhName());
                }
            } else {

            }

        }
    };
}
