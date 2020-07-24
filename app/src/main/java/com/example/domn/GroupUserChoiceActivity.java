package com.example.domn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.example.domn.bean.CustomerBean;
import com.example.domn.bean.CustomerListBean;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class GroupUserChoiceActivity extends Activity {
    Button search;
    EditText user_name;
    Button clear;
    Button next;
    CheckBox isGroup;
    final OkHttpClient client = new OkHttpClient();
    private List<CustomerBean> customerList;
    private String csId = "";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息", ReturnMessage);
                CustomerListBean customerListBean = new Gson().fromJson(ReturnMessage, CustomerListBean.class);
                customerList = customerListBean.getRows();
                OptionsPickerView pvOptions = new OptionsPickerView.Builder(GroupUserChoiceActivity.this, new OptionsPickerView.OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int options2, int options3, View v) {
                        CustomerBean customerBean = customerList.get(options1);
                        user_name.setText(customerBean.getColumn1());
                        csId = customerBean.getCustId();
                        user_name.setFocusable(false);
                        user_name.setTextIsSelectable(false);
                    }
                })

                        .setDividerColor(Color.BLACK)
                        .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                        .setContentTextSize(20)//设置文字大小
                        .setOutSideCancelable(false)// default is true
                        .setTitleText("一共有 " + customerListBean.get客户列表() + "条 数据")
                        .setCancelText("取消")
                        .setSubmitText("确定")
                        .build();
                if (customerList.size() > 0) {
                    hintKbTwo();
                    pvOptions.setPicker(customerList);//条件选择器
                    pvOptions.show();
                } else {
                    Toast ts = Toast.makeText(getBaseContext(), "没有数据！请检查输入的关键字", LENGTH_SHORT);
                    ts.setGravity(Gravity.TOP, 0, 70);
                    ts.show();
                }


            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupuserchoice);
        search = findViewById(R.id.search);
        user_name = findViewById(R.id.user_name);
        clear = findViewById(R.id.clear);
        next = findViewById(R.id.next);
        isGroup = findViewById(R.id.isGroup);
        this.initUserName();
        this.initClear();
        this.initSearch();
        this.initNext();
    }
    private void initNext() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(csId)) {
                    Toast toast = Toast.makeText(GroupUserChoiceActivity.this, "请先选择客户！", LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 70);
                    toast.show();
                    return;
                }
                Intent intent = new Intent(GroupUserChoiceActivity.this, ListTwoActivity.class);
                intent.putExtra("csId", csId);
                intent.putExtra("isGroup", isGroup.isChecked());
                startActivity(intent);
            }
        });
    }

    private void getUserList() {
        String nameKey = user_name.getText().toString();
        if (nameKey.length() < 2) {
            Toast ts = Toast.makeText(getBaseContext(), "请填写两个以上的关键字", LENGTH_SHORT);
            ts.setGravity(Gravity.TOP, 0, 70);
            ts.show();
            return;
        }
        final Request request = new Request.Builder()
                .url("http://192.168.11.243/FirstPDAServer/home/GetCustList?partName=" + nameKey)
                .get()
                .build();
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

    private void initSearch() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserList();
            }
        });
    }

    private void initUserName() {
        user_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInput(user_name);
                }
            }
        });
        user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInput(user_name);
            }
        });
    }

    private void initClear() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name.setText("");
                user_name.setFocusable(true);
                user_name.setFocusableInTouchMode(true);
                user_name.requestFocus();
                user_name.findFocus();
                user_name.setTextIsSelectable(true);
                csId = "";
            }
        });
    }

    public void showInput(final EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
