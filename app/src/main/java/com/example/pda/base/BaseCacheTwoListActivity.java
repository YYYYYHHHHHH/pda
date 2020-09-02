package com.example.pda.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.example.pda.R;
import com.example.pda.bean.BarCodeBean;
import com.example.pda.bean.GetBarDetailsBean;
import com.example.pda.bean.GetBarDetailsRows;
import com.example.pda.commpont.MyContent;
import com.example.pda.commpont.SlideLayout;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Request;
import okhttp3.Response;

public class BaseCacheTwoListActivity extends BaseListActivity {
    protected String autoid;
    protected String barcode;
    protected String GetBarsDetailsUrl;
    protected String DeleteBarInTrayUrl;

    @Override
    protected void init() {
        setContentView(R.layout.activity_listthree);
        numberText = findViewById(R.id.numberText);
        listView = findViewById(R.id.codeitem);
        submit = findViewById(R.id.submit);
        scrollView = findViewById(R.id.scrollview);
        //重写onCreate的时候要记得给strArr赋值
        strArr = new ArrayList<>();
        initUrl();
        initSubmit();
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    protected void initUrl() {
        this.GetBarsDetailsUrl = "GetBarsDetails";
        this.DeleteBarInTrayUrl = "DeleteBarInTray";
    }

    @Override
    protected void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseCacheTwoListActivity.this.finish();
            }
        });
    }

    @Override
    protected void onItemDelet(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        SlideLayout slideLayout = (SlideLayout) v.getParent();
        slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
        datas.remove(myContent);
        initDeleteBarInTray(myContent.getContent());
    }

    private void initPicking() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + GetBarsDetailsUrl + "?barcode=" + barcode + "&bTrue=0")
                .get()
                .build();
        dialog.setHintText("加载数据中").show();
        threadPool.execute(new BaseCacheTwoListActivity.InitPickingRunable(request));
    }
    private void initDeleteBarInTray(String trayCode) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + DeleteBarInTrayUrl + "?barcode=" + trayCode
                        + "&autoid=" + autoid
                        + "&trayCode=" + barcode
                        + "&loginuser=" + userBean.getUser())
                .get()
                .build();
        threadPool.execute(new BaseCacheTwoListActivity.DeleteBarInTrayRunable(request));
    }

    @Override
    protected void HandlerProcessing(Message msg) {
        HashMap hashMap = (HashMap) msg.obj;
        Response response = (Response) hashMap.get("response");
        String ReturnMessage = (String) hashMap.get("resStr");
        if (!response.isSuccessful()) {
            toast.setText("服务器出错");
            toast.show();
            return;
        }
        if (msg.what == 1) {
            GetBarsDetailsPostProcessing(ReturnMessage);
        } else if (msg.what == 2) {
            DeleteBarInTrayPostProcessing(ReturnMessage);
        }
    }

    public void GetBarsDetailsPostProcessing(String ReturnMessage) {
        GetBarDetailsBean bean = new Gson().fromJson(ReturnMessage, GetBarDetailsBean.class);
        GetBarDetailsRows[] rows = bean.getRows();
        strArr.clear();
        for (GetBarDetailsRows row : rows) {
            strArr.add(new MyContent(row.getBarcode()));
        }
        renderList();
    }

    public void DeleteBarInTrayPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            toast.setText(mesg);
            toast.show();
        }
        renderList();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        autoid = intent.getStringExtra("autoid");
        barcode = intent.getStringExtra("barcode");
        initPicking();
    }
    class InitPickingRunable extends BaseRunable {
        public InitPickingRunable(Request request) {
            super(request);
            what = 1;
        }
    }
    class DeleteBarInTrayRunable extends BaseRunable {
        public DeleteBarInTrayRunable(Request request) {
            super(request);
            what = 2;
        }
    }
}
