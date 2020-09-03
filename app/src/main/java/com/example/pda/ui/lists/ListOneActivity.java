package com.example.pda.ui.lists;

import android.content.Intent;
import android.graphics.Color;

import com.example.pda.base.BaseListActivity;
import com.example.pda.bean.BarCodeBean;
import com.example.pda.util.ToastUtils;
import com.example.pda.commpont.MyContent;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import okhttp3.Request;

public class ListOneActivity extends BaseListActivity {
    private String whId;

    @Override
    protected void init() {
        super.init();
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "CommitBarToStock";
        this.checkBarUrl = "GetBarStatus";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        whId = intent.getStringExtra("whId");
    }

    @Override
    protected void CheckBarPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            if (status == -100) {
                mesg += "，或者扫描不清晰";
            }
            ToastUtils.showShort(mesg);
        } else {
            strArr.add(new MyContent(barcodeStr, barCodeBean.getProId()));
            renderList();
        }
    }

    @Override
    protected void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/CommitBarToStock?loginId=" + userBean.getStatus() + "&whId=" + whId;
        for (MyContent myContent : strArr) {
            url += "&ids=" + myContent.getProId();
        }
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog.setHintText("提交中").show();
        threadPool.execute(new SubmitBarRunable(request));
    }


}
