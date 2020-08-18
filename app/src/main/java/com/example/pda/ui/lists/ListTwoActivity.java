package com.example.pda.ui.lists;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.pda.R;
import com.example.pda.base.BaseListActivity;
import com.example.pda.bean.BarCodeTwoBean;
import com.example.pda.commpont.MyContent;
import com.example.pda.commpont.MyTwoContent;
import com.example.pda.commpont.SlideLayout;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import okhttp3.Request;

public class ListTwoActivity extends BaseListActivity {
    private String csId;
    private String csName;
    private Boolean isGroup;
    private String numberOfGroups;
    private ArrayList<MyTwoContent> strArr = null;
    private AlertDialog.Builder alert;

    @Override
    protected void init() {
        super.init();
        this.InitInputCode();
        //设置好提交请求和checkBar的url
        this.submitBarUrl = "CommitBarToPackage";
        this.checkBarUrl = "GetBarStatusAndInvClass";
        //dialog需要对应当前页面对象
        this.dialog = new ZLoadingDialog(this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK);//颜色
    }

    @Override
    protected void CheckBarPostProcessing(String ReturnMessage) {
        final BarCodeTwoBean barCodeTwoBean = new Gson().fromJson(ReturnMessage, BarCodeTwoBean.class);
        int status = Integer.parseInt(barCodeTwoBean.getStatus());
        String mesg = barCodeTwoBean.getMsg();
        if (status < 0) {
            if (status == -100) {
                mesg += "，或者扫描不清晰";
            }
            toast.setText(mesg);
            toast.show();
        } else {
            if (!checkNumberOfGroups()) return;
            if (!"0".equals(barCodeTwoBean.getCustId()) && !barCodeTwoBean.getCustId().equals(csId)) {
                alert.setMessage("现在在为【" + csName + "】组托, 该批卷是为【" + barCodeTwoBean.getCustName() + "】生成的, 您确认组托吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (strArr.contains(new MyTwoContent(barcodeStr))) {
                                    toast.setText("条码已存在！");
                                    toast.show();
                                    return;
                                }
                                MyTwoContent myTwoContent = new MyTwoContent(barcodeStr, barCodeTwoBean.getInvClass());
                                if (isGroup && strArr.size() != 0 && !strArr.get(0).getInvClass().equals(myTwoContent.getInvClass())) {
                                    myTwoContent.setGroup(false);
                                }
                                strArr.add(myTwoContent);
                                renderList();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                alert.show();

            } else {
                MyTwoContent myTwoContent = new MyTwoContent(barcodeStr, barCodeTwoBean.getInvClass());
                if (isGroup && strArr.size() != 0 && !strArr.get(0).getInvClass().equals(myTwoContent.getInvClass())) {
                    myTwoContent.setGroup(false);
                }
                strArr.add(myTwoContent);
                MyAdapter myAdapter = new MyAdapter(this, strArr);
                listView.setAdapter(myAdapter);
                numberText.setText("记数：" + strArr.size() + "件");
                goToBottom();
            }
        }
    }

    @Override
    protected void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/CommitBarToPackage?loginId=" + userBean.getStatus() + "&CustId=" + csId;
        for (MyTwoContent myTwoContent : strArr) {
            url += "&barcodes=" + myTwoContent.getContent();
        }
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog.setHintText("提交中").show();
        threadPool.execute(new SubmitBarRunable(request));
    }

    @Override
    protected void initInputButton() {
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String code = inputCode.getText().toString();
                if ("".equals(code)) {
                    toast.setText("不能添加空条码");
                    toast.show();
                } else {
                    if (strArr.contains(new MyTwoContent(code))) {
                        toast.setText("不能重复扫码");
                        toast.show();
                        return;
                    }
                    if (checkNumberOfGroups()) {
                        checkBarCode(code);
                    }
                }
            }
        });
    }
    private void InitInputCode() {
        inputCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputCode.setFocusable(true);
                inputCode.setFocusableInTouchMode(true);
                inputCode.requestFocus();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        csId = intent.getStringExtra("csId");
        csName = intent.getStringExtra("csName");
        isGroup = intent.getBooleanExtra("isGroup", false);
        numberOfGroups = intent.getStringExtra("numberOfGroups");
        alert = new AlertDialog.Builder(this);
    }

    private Boolean checkNumberOfGroups() {
        if ("空".equals(numberOfGroups) || strArr.size() < Integer.parseInt(numberOfGroups)) {
            return true;
        } else {
            toast.setText("条码的数量不能多于选定组托数量");
            toast.show();
            return false;
        }
    }

    class MyAdapter extends BaseAdapter {
        private Context content;
        private ArrayList<MyTwoContent> datas;

        private MyAdapter(Context context, ArrayList<MyTwoContent> datas) {
            this.content = context;
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(content).inflate(R.layout.item_slide, null);
                viewHolder = new ViewHolder();
                viewHolder.contentView = (TextView) convertView.findViewById(R.id.content);
                viewHolder.menuView = (TextView) convertView.findViewById(R.id.menu);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.contentView.setText(datas.get(position).getContent());
            if (!datas.get(position).getGroup()) {
                viewHolder.contentView.setTextColor(Color.RED);
            } else {
                viewHolder.contentView.setTextColor(Color.BLACK);
            }
            viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            final MyTwoContent myTwoContent = datas.get(position);
            viewHolder.menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SlideLayout slideLayout = (SlideLayout) v.getParent();
                    slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
                    datas.remove(myTwoContent);
                    numberText.setText("记数：" + strArr.size() + "件");
                    if (datas.size() == 1) {
                        datas.get(0).setGroup(true);
                    }
                    notifyDataSetChanged();
                }
            });

            SlideLayout slideLayout = (SlideLayout) convertView;
            slideLayout.setOnStateChangeListener(new MyAdapter.MyOnStateChangeListener());


            return convertView;
        }

        public SlideLayout slideLayout = null;

        class MyOnStateChangeListener implements SlideLayout.OnStateChangeListener {
            /**
             * 滑动后每次手势抬起保证只有一个item是open状态，加入sets集合中
             **/
            @Override
            public void onOpen(SlideLayout layout) {
                slideLayout = layout;
                if (sets.size() > 0) {
                    for (SlideLayout s : sets) {
                        s.closeMenu();
                        sets.remove(s);
                    }
                }
                sets.add(layout);
            }

            @Override
            public void onMove(SlideLayout layout) {
                if (slideLayout != null && slideLayout != layout) {
                    slideLayout.closeMenu();
                }
            }

            @Override
            public void onClose(SlideLayout layout) {
                if (sets.size() > 0) {
                    sets.remove(layout);
                }
                if (slideLayout == layout) {
                    slideLayout = null;
                }
            }
        }
    }
    static class ViewHolder {
        public TextView contentView;
        public TextView menuView;
    }
}
