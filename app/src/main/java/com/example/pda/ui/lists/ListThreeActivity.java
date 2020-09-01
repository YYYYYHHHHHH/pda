package com.example.pda.ui.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.example.pda.base.BaseListActivity;
import com.example.pda.bean.BarCodeBean;
import com.example.pda.bean.BarCodeFourBean;
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

public class ListThreeActivity extends BaseListActivity {
    private int allSize = 0;

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
    protected void onItemClick(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        if (Integer.parseInt(myContent.getbTrue()) == 0) {
            Intent intent = new Intent(ListThreeActivity.this, ListFourActivity.class);
//            intent.putExtra("barcode", myContent.getContent());
            startActivity(intent);
        }
    }

    @Override
    protected void onItemDelet(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        SlideLayout slideLayout = (SlideLayout) v.getParent();
        slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
        datas.remove(myContent);
        allSize -=  Integer.parseInt(myContent.getiNum());
        renderList();
    }


    @Override
    protected void renderList() {
        BaseListActivity.MyAdapter myAdapter = new BaseListActivity.MyAdapter(ListThreeActivity.this, strArr);
        listView.setAdapter(myAdapter);
        numberText.setText(strArr.size() + "码(" + allSize + "件)");
        goToBottom();
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
            toast.setText(mesg);
            toast.show();
        } else {
            strArr.add(new MyContent(barcodeStr, barCodeBean.getbTrue(), barCodeBean.getiNum()));
            allSize += Integer.parseInt(barCodeBean.getiNum());
            renderList();
        }
    }

    @Override
    protected void initClaer() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ListThreeActivity.this).setTitle("确认要清空吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                strArr = new ArrayList<>();
                                allSize = 0;
                                renderList();
                                inputCode.setText("");
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
    protected void SubmitBarPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            toast.setText(mesg);
            toast.show();
        } else {
            toast.setText("返工出库成功");
            toast.show();
            strArr.clear();
            renderList();
        }
    }
}
