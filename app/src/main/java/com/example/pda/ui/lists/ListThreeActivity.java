package com.example.pda.ui.lists;


import android.graphics.Color;

import com.example.pda.base.BaseListActivity;
import com.example.pda.bean.BarCodeBean;
import com.example.pda.util.ToastUtils;
import com.example.pda.commpont.MyContent;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Request;

public class ListThreeActivity extends BaseListActivity {
    @Override
    protected void init() {
        super.init();
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "ReturnBarFromStock";
        this.checkBarUrl = "CheckBarStatus";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    @Override
    protected void submitBarCode() {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        String url = "http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/" + submitBarUrl + "?loginid="
                + userBean.getStatus()
                + "&tDate=" + formatter.format(new Date(System.currentTimeMillis()));
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
    protected void SubmitBarPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            ToastUtils.showShort(mesg);
        } else {
            ToastUtils.showShort("返工出库成功");
            strArr.clear();
            renderList();
        }
    }
}
