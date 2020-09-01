package com.example.pda.ui.lists;

import android.graphics.Color;

import com.example.pda.R;
import com.example.pda.base.BaseListActivity;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;

public class ListFourActivity extends BaseListActivity {
    @Override
    protected void init() {
        setContentView(R.layout.activity_listthree);
        numberText = findViewById(R.id.numberText);
        listView = findViewById(R.id.codeitem);
        submit = findViewById(R.id.submit);
        scrollView = findViewById(R.id.scrollview);
        //重写onCreate的时候要记得给strArr赋值
        strArr = new ArrayList<>();
        initSubmit();
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }



}
