package com.example.pda;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.example.pda.bean.MenuBean;
import com.example.pda.bean.MenuBgBean;
import com.example.pda.bean.UserBean;
import com.example.pda.bean.globalbean.MyOkHttpClient;
import com.example.pda.bean.globalbean.MyToast;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@ContentView(R.layout.activity_menuactivity)
public class MenuActivity extends AppCompatActivity {
    @ViewInject(R.id.gridView)
    GridView gridView;
    final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    private List<MenuBean> rows;
    private ZLoadingDialog dialog;
    private View view;
    private MyAdapter myAdapter;
    private ArrayList<HashMap<String, Object>> arrayList;
    private UserBean userBean;
    private Toast toast = MyToast.getToast();
    private SharedPreferences setinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        x.view().inject(this);
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        toast.setText("欢迎回来：" + userBean.getUser());
        toast.show();
        getMenus();
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                myAdapter.setSelection(arg2);
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getMenus() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/GetMenuList?loginId=" + userBean.getStatus())
                .get()
                .build();
        dialog = new ZLoadingDialog(MenuActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("加载菜单中")
                .show();
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

    class MyAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> arrayList;
        Context context;
        HashMap<String, Object> hashMap;
        int selectItem = -1;

        public MyAdapter(ArrayList<HashMap<String, Object>> arrayList, Context context) {
            // TODO Auto-generated constructor stub
            this.arrayList = arrayList;
            this.context = context;
        }

        public void setSelection(int position) {
            selectItem = position;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (null == arrayList) {
                return 0;
            } else {
                return arrayList.size();
            }

        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arrayList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            view = LayoutInflater.from(context).inflate(R.layout.mylayout, arg2, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            final TextView textView = (TextView) view.findViewById(R.id.textView);
            hashMap = (HashMap<String, Object>) getItem(arg0);
            imageView.setImageResource((Integer) hashMap.get("image"));
            textView.setText((CharSequence) hashMap.get("text"));
//            if (selectItem == arg0) {
//                view.setBackgroundColor(Color.GREEN);
//            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = textView.getText().toString();
                    if (title.contains("组托单")) {
                        Intent intent = new Intent(MenuActivity.this, GroupUserChoiceActivity.class);
                        startActivity(intent);
                    } else if (title.contains("成品待入库")) {
                        Intent intent = new Intent(MenuActivity.this, ChoiceHouse.class);
                        intent.putExtra("menuid", rows.get(arg0).getMenuId());
                        startActivity(intent);
                    }
                }
            });

            return view;
        }//设置适配器或更新适配器调用
    }
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息", ReturnMessage);
                MenuBgBean menuBgBean = new Gson().fromJson(ReturnMessage, MenuBgBean.class);
                rows = menuBgBean.getRows();
                arrayList = new ArrayList<HashMap<String, Object>>();
                for (int i = 0; i < rows.size(); i++) {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("text", rows.get(i).getMenuTitle());
                    Object image = R.mipmap.mima;
                    switch (rows.get(i).getMenuTitle()) {
                        case "组托单\r\n": {
                            image = R.mipmap.dan;
                            break;
                        }
                        case "成品待入库\r\n": {
                            image = R.mipmap.ruku;
                        }
                    }
                    hashMap.put("image", image);
                    arrayList.add(hashMap);
                    myAdapter = new MyAdapter(arrayList, MenuActivity.this);
                    gridView.setAdapter(myAdapter);
                }

            } else {

            }
            dialog.cancel();
        }
    };
}