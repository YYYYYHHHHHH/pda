package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.example.pda.bean.CustomerBean;
import com.example.pda.bean.CustomerListBean;
import com.example.pda.bean.WhBean;
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
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

@ContentView(R.layout.activity_groupuserchoice)
public class GroupUserChoiceActivity extends AppCompatActivity {
    @ViewInject(R.id.search)
    private Button search;
    @ViewInject(R.id.user_name)
    private EditText user_name;
    @ViewInject(R.id.clear)
    private Button clear;
    @ViewInject(R.id.next)
    private Button next;
    @ViewInject(R.id.isGroup)
    private CheckBox isGroup;
    @ViewInject(R.id.groupnum)
    private TextView groupnum;

    private final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    private Toast toast = MyToast.getToast();
    private List<CustomerBean> customerList;
    private String csId = "";
    private String csName = "";
    private List<String> numList;
    private String numberOfGroups = "12";
    private SharedPreferences setinfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        numList = new ArrayList<>();
        numList.add("空");
        for (int i = 1; i < 36; i++) {
            String item = "";
            item += i;
            if (item.length() == 1)
                item = "0" + item;
            numList.add(item);
        }
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        this.initUserName();
    }

    @Event(R.id.next)
    private void initNext(View view) {
        if ("".equals(csId)) {
            toast.setText("请先选择客户！");
            toast.show();
            return;
        }
        Intent intent = new Intent(GroupUserChoiceActivity.this, ListTwoActivity.class);
        intent.putExtra("csId", csId);
        intent.putExtra("isGroup", isGroup.isChecked());
        intent.putExtra("csName", csName);
        intent.putExtra("numberOfGroups", numberOfGroups);
        startActivity(intent);
    }

    @Event(R.id.groupnum)
    private void initGroupnum(View view) {
        this.hintKbTwo();
        OptionsPickerView pvOptions = new OptionsPickerView.Builder(GroupUserChoiceActivity.this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                final String s = numList.get(options1);
                if (!s.equals(numberOfGroups)) {
                    new AlertDialog.Builder(GroupUserChoiceActivity.this).setTitle("确认要更改组托单数量吗")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    numberOfGroups = s;
                                    groupnum.setText("组托数量为：" + s + "件");
                                }
                            })
                            .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }

            }
        })
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)//设置文字大小
                .setOutSideCancelable(false)// default is true
                .setTitleText("选择组托数量")
                .setCancelText("取消")
                .setSubmitText("确定")
                .build();
        pvOptions.setPicker(numList);//条件选择器
        pvOptions.setSelectOptions(numList.indexOf(numberOfGroups));
        pvOptions.show();
    }

    private void getUserList() {
        String nameKey = user_name.getText().toString();
//        if (nameKey.length() < 2) {
//            toast.setText("请填写两个以上的关键字");
//            toast.show();
//            return;
//        }
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/GetCustList?partName=" + nameKey)
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

    @Event(R.id.search)
    private void initSearch(View view) {
        getUserList();
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

    @Event(R.id.clear)
    private void initClear(View view) {
        user_name.setText("");
        user_name.setFocusable(true);
        user_name.setFocusableInTouchMode(true);
        user_name.requestFocus();
        user_name.findFocus();
        user_name.setTextIsSelectable(true);
        csId = "";
        csName = "";
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
                        csName = customerBean.getColumn1();
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
                if (customerList.size() == 1) {
                    hintKbTwo();
                    CustomerBean customerBean = customerList.get(0);
                    user_name.setText(customerBean.getColumn1());
                    csId = customerBean.getCustId();
                    csName = customerBean.getColumn1();
                    user_name.setFocusable(false);
                    user_name.setTextIsSelectable(false);
                } else if (customerList.size() > 0) {
                    hintKbTwo();
                    pvOptions.setPicker(customerList);//条件选择器
                    pvOptions.show();
                } else {
                    toast.setText("没有数据！请检查输入的关键字");
                    toast.show();
                }


            }
        }
    };
}
