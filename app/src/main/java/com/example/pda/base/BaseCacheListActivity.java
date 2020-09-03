package com.example.pda.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.example.pda.bean.BarCodeBean;
import com.example.pda.bean.BarCodeFourBean;
import com.example.pda.bean.GetBarDetailsBean;
import com.example.pda.bean.GetBarDetailsRows;
import com.example.pda.bean.PDASavedBean;
import com.example.pda.bean.PDASavedRows;
import com.example.pda.util.ToastUtils;
import com.example.pda.commpont.MyContent;
import com.example.pda.commpont.SlideLayout;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import okhttp3.Request;
import okhttp3.Response;

public class BaseCacheListActivity extends BaseListActivity{
    protected int allSize = 0;
    protected String quantityPicked;
    protected String autoid;
    protected String trayCode;
    protected String clearBarUrl;
    protected String GetBarsDetailsUrl;
    protected String DeleteBarFromPDAUrl;
    protected String GetBarsFromPDASavedUrl;

    @Override
    protected void init() {
        super.init();
        //dialog需要对应当前页面对象
        initUrl();
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    protected void initUrl() {
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "SavePDABarsToPVs";
        this.checkBarUrl = "CheckBarInfoAndSave";
        this.clearBarUrl = "DeleteAllBarFromPDA";
        this.GetBarsDetailsUrl = "GetBarsDetails";
        this.DeleteBarFromPDAUrl = "DeleteBarFromPDA";
        this.GetBarsFromPDASavedUrl = "GetBarsFromPDASaved";
    }

    @Override
    protected void SubmitBarPostProcessing(String ReturnMessage) {
        allSize = 0;
        super.SubmitBarPostProcessing(ReturnMessage);
    }

    @Override
    protected void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + submitBarUrl + "?loginuser="
                + userBean.getUserId()
                + "&autoid=" + autoid;
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog.setHintText("提交中").show();
        threadPool.execute(new SubmitBarRunable(request));
    }

    @Override
    protected void initClaer() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(BaseCacheListActivity.this).setTitle("确认要清空吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                strArr = new ArrayList<>();
                                allSize = 0;
                                renderList();
                                inputCode.setText("");
                                clearAllCode();

                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();
            }
        });
    }

    @Override
    protected void HandlerProcessing(Message msg) {
        HashMap hashMap = (HashMap) msg.obj;
        Response response = (Response) hashMap.get("response");
        String ReturnMessage = (String) hashMap.get("resStr");
        if (!response.isSuccessful()) {
            ToastUtils.showShort("服务器出错");
            return;
        }
        if (msg.what == 1) {
            CheckBarPostProcessing(ReturnMessage);
        } else if (msg.what == 2) {
            SubmitBarPostProcessing(ReturnMessage);
        } else if (msg.what == 3) {
            InitPickingPostProcessing(ReturnMessage);
        } else if (msg.what == 4) {
            DeletLocalSaveCloudPostProcessing(ReturnMessage);
        } else if (msg.what == 5) {
            ShowBarDetailPostProcessing(ReturnMessage);
        } else if (msg.what == 6) {
            ClearAllCodePostProcessing(ReturnMessage);
        }
    }

    @Override
    protected void CheckBarPostProcessing(String ReturnMessage) {
        BarCodeFourBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeFourBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            if (status == -100) {
                mesg += "，或者扫描不清晰";
            }
            ToastUtils.showShort(mesg);
        } else {
            strArr.add(new MyContent(barcodeStr, barCodeBean.getbTrue()));
            allSize += Integer.parseInt(barCodeBean.getiNum());
            renderList();
        }
    }

    @Override
    protected void onItemDelet(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        SlideLayout slideLayout = (SlideLayout) v.getParent();
        slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
        datas.remove(myContent);
        deletLocalSaveCloud(myContent.getContent());
    }

    @Override
    protected void onItemClick(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        if (Integer.parseInt(myContent.getbTrue()) == 0) {
//            Intent intent = new Intent(BaseCacheListActivity.this, ListSevenActivity.class);
//            intent.putExtra("autoid", autoid);
//            intent.putExtra("barcode", myContent.getContent());
//            startActivity(intent);
        } else {
            showBarDetail(myContent.getContent());
        }
    }

    protected void InitPickingPostProcessing(String ReturnMessage) {
        PDASavedBean bean = new Gson().fromJson(ReturnMessage, PDASavedBean.class);
        PDASavedRows[] rows = bean.getRows();
        strArr.clear();
        allSize = 0;
        for (int i = 0; i < rows.length; i++) {
            strArr.add(new MyContent(rows[i].getScancode(), rows[i].getbTrue()));
            allSize += Integer.parseInt(rows[i].getiNum());
        }
        renderList();
    }

    protected void DeletLocalSaveCloudPostProcessing(String ReturnMessage) {
        BarCodeBean bean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        if (!"0".equals(bean.getStatus())) {
            ToastUtils.showShort(bean.getMsg());
        }
        allSize = 0;
        initPicking();
    }

    protected void ShowBarDetailPostProcessing(String ReturnMessage) {
        GetBarDetailsBean bean = new Gson().fromJson(ReturnMessage, GetBarDetailsBean.class);
        GetBarDetailsRows[] rows = bean.getRows();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            builder.append(rows[i].getBarcode()).append("\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("此条码的组托码")
                .setMessage(builder.toString())
                .show();
    }

    protected void ClearAllCodePostProcessing(String ReturnMessage) {
        initPicking();
    }

    protected void initPicking() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + GetBarsFromPDASavedUrl + "?autoId=" + autoid)
                .get()
                .build();
        dialog.setHintText("加载数据中").show();
        threadPool.execute(new BaseCacheListActivity.InitPickingRunable(request));
    }

    protected void deletLocalSaveCloud(String content) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + DeleteBarFromPDAUrl + "?autoId="
                        + autoid
                        + "&barcode=" + content
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        dialog.setHintText("同步中").show();
        threadPool.execute(new BaseCacheListActivity.DeletLocalSaveCloudRunable(request));
    }

    protected void showBarDetail(String barcodeStr) {
        trayCode = barcodeStr;
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + GetBarsDetailsUrl + "?barcode=" + barcodeStr + "&bTrue=1")
                .get()
                .build();
        dialog.setHintText("获取详情中").show();
        threadPool.execute(new BaseCacheListActivity.ShowBarDetailRunable(request));
    }

    protected void clearAllCode() {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + clearBarUrl + "?autoid=" + autoid
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        threadPool.execute(new BaseCacheListActivity.ClearAllCodeRunable(request));
    }

    @Override
    protected void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strArr.size() <= 0) {
                    ToastUtils.showShort("没有要提交的条码");
                    return;
                }
                new AlertDialog.Builder(BaseCacheListActivity.this).setTitle("提示")
                        .setMessage("一共有" + strArr.size() + "码，" + allSize + "件\n所需发货件数为：" + quantityPicked + "件\n确认要提交吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                submitBarCode();
                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();

            }
        });
    }

    @Override
    protected void checkBarCode(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/MeiliPDAServer/home/" + checkBarUrl
                        + "?barcode=" + barcodeStr
                        + "&autoid=" + autoid
                        + "&LoginUser=" + userBean.getUser())
                .get()
                .build();
        dialog.setHintText("检查条码中");
        dialog.show();
        this.barcodeStr = barcodeStr;
        threadPool.execute(new CheckBarCodeRunable(request));
    }

    @Override
    protected void renderList() {
        BaseListActivity.MyAdapter myAdapter = new BaseListActivity.MyAdapter(BaseCacheListActivity.this, strArr);
        listView.setAdapter(myAdapter);
        numberText.setText(strArr.size() + "码(" + allSize + "件)");
        goToBottom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        autoid = intent.getStringExtra("autoid");
        quantityPicked = intent.getStringExtra("quantityPicked");
        trayCode = "";
        initPicking();
    }

    class InitPickingRunable extends BaseRunable {
        public InitPickingRunable(Request request) {
            super(request);
            what = 3;
        }
    }

    class DeletLocalSaveCloudRunable extends BaseRunable {
        public DeletLocalSaveCloudRunable(Request request) {
            super(request);
            what = 4;
        }
    }

    class ShowBarDetailRunable extends BaseRunable {
        public ShowBarDetailRunable(Request request) {
            super(request);
            what = 5;
        }
    }

    class ClearAllCodeRunable extends BaseRunable {
        public ClearAllCodeRunable(Request request) {
            super(request);
            what = 6;
        }
    }
}
