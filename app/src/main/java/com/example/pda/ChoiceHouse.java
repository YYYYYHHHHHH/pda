package com.example.pda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ChoiceHouse extends AppCompatActivity {
    final OkHttpClient client = new OkHttpClient();
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==1){
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息",ReturnMessage);
                final WhListBean whListBean = new Gson().fromJson(ReturnMessage, WhListBean.class);
                WhList = whListBean.getRows();
                if (WhList.size() == 1) {
                    editText.setText(WhList.get(0).getWhName());
                }
            } else {

            }

        }
    };
    private List<WhBean> WhList = new ArrayList<>();
    private EditText editText;
    private Button clearButton;
    private Button nextButton;
    private String whId;
    private UserBean userBean;
    private String menuid;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choicehouse);
        SharedPreferences setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        Toast ts = Toast.makeText(getBaseContext(),"欢迎回来：" + userBean.getUser(),Toast.LENGTH_LONG);
        ts.setGravity(Gravity.TOP,0,70);
        ts.show();
        Intent intent = getIntent();
        menuid = intent.getStringExtra("menuid");
        getWhList();
        editText = (EditText) findViewById(R.id.house_name);
        clearButton = (Button)findViewById(R.id.clear);
        nextButton = (Button)findViewById(R.id.next);
        this.editTextOnClick();
        this.clearOnClick();
        this.nextOnClick();
    }

    private void editTextOnClick() {

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WhList.size() <= 0) {
                    Toast ts = Toast.makeText(getBaseContext(), "无数据", Toast.LENGTH_SHORT);
                    ts.setGravity(Gravity.TOP, 0, 70);
                    ts.show();
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
        });
    }
    private void clearOnClick() {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
    }
    private void nextOnClick() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("editText的值", String.valueOf(editText.getText()));
                if ("".equals(String.valueOf(editText.getText()))) {
                    Toast ts = Toast.makeText(getBaseContext(),"请先选择仓库",Toast.LENGTH_LONG);
                    ts.show();
                } else {
                    Intent i = new Intent(ChoiceHouse.this, ListActivity.class);
                    i.putExtra("whId",whId);
                    startActivity(i);
                }

            }
        });
    }
    private void getWhList() {
        final Request request = new Request.Builder()
                .url("http://192.168.11.243/FirstPDAServer/home/GetWhList?loginId=" + userBean.getStatus() + "&menuid=" + menuid)
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
                }
            }
        }).start();

    }

}
