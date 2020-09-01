package com.example.pda.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pda.R;
import com.example.pda.bean.BarCodeBean;
import com.example.pda.bean.UserBean;
import com.example.pda.bean.globalbean.MyOkHttpClient;
import com.example.pda.bean.globalbean.MyToast;
import com.example.pda.commpont.MyContent;
import com.example.pda.commpont.SlideLayout;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BaseListActivity extends AppCompatActivity {
    protected final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    protected static int MAX_BAR = 100;
    protected boolean isScaning = false;
    protected SoundPool soundpool = null;
    protected String barcodeStr;
    protected ScanManager mScanManager;
    protected UserBean userBean;
    protected Set<SlideLayout> sets = new HashSet();
    protected Toast toast = MyToast.getToast();
    protected int soundid;
    protected final OkHttpClient client = MyOkHttpClient.getOkHttpClient();
    protected ArrayList<MyContent> strArr = null;
    protected SharedPreferences setinfo;
    protected Vibrator vibrator;
    protected String checkBarUrl = "";
    protected String submitBarUrl = "";
    protected ZLoadingDialog dialog;
    protected ExecutorService threadPool = Executors.newSingleThreadExecutor();

    protected TextView numberText;
    protected EditText inputCode;
    protected ListView listView;
    protected Button inputButton;
    protected Button clear;
    protected Button submit;
    protected ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {
        setContentView(R.layout.activity_list);
        numberText = findViewById(R.id.numberText);
        inputCode = findViewById(R.id.inputCode);
        listView = findViewById(R.id.codeitem);
        inputButton = findViewById(R.id.inputButton);
        clear = findViewById(R.id.clear);
        submit = findViewById(R.id.submit);
        scrollView = findViewById(R.id.scrollview);
        //重写onCreate的时候要记得给strArr赋值
        strArr = new ArrayList<>();
        initInputButton();
        initSubmit();
        initClaer();
    }

    protected void initInputButton() {
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String code = inputCode.getText().toString();
                if ("".equals(code)) {
                    toast.setText("不能添加空条码");
                    toast.show();
                } else {
                    if (strArr.contains(new MyContent(code))) {
                        toast.setText("不能重复扫码");
                        toast.show();
                        return;
                    }
                    checkBarCode(code);
                }
            }
        });
    }

    protected void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strArr.size() <= 0) {
                    toast.setText("没有要提交的条码");
                    toast.show();
                    return;
                }
                new AlertDialog.Builder(BaseListActivity.this).setTitle("一共有" + strArr.size() + "件，确认要提交吗")
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

    protected void initClaer() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(BaseListActivity.this).setTitle("确认要清空吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                strArr.clear();
                                renderList();
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

    /**
     * @author YZHY
     * @describe 初始化扫描枪
     */
    protected void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    /**
     * @author YZHY
     * @describe 将列表滑至最后一个item
     */
    protected void goToBottom() {
        vibrator.vibrate(200);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * @author YZHY
     * @describe 使用广播接收PDA扫描到的条码
     */
    protected BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            ScanReceiverRun(context, intent);
        }
    };

    protected void ScanReceiverRun(Context context, Intent intent) {
        soundpool.play(soundid, 1, 1, 0, 0, 1);
        byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
        int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
        byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
        android.util.Log.i("debug", "----codetype--" + temp);
        barcodeStr = new String(barcode, 0, barcodelen);
        android.util.Log.i("debug", "----code--" + barcodeStr);
        if (strArr.contains(new MyContent(barcodeStr))) {
            toast.setText("不能重复扫码！");
            toast.show();
            return;
        }
        if (!isScaning) {
            if (strArr.size() >= MAX_BAR) {
                toast.setText("一次扫入的条码不能超过【" + MAX_BAR + "】条");
            } else {
                isScaning = true;
                checkBarCode(barcodeStr);
            }
        }
    }

    /**
     * @author YZHY
     * @describe 执行检查条码的操作
     */
    protected void checkBarCode(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/" + checkBarUrl + "?barcode=" + barcodeStr)
                .get()
                .build();
        dialog.setHintText("检查条码中");
        dialog.show();
        this.barcodeStr = barcodeStr;
        threadPool.execute(new CheckBarCodeRunable(request));
    }

    /**
     * @author YZHY
     * @describe 执行提交条码的操作
     */
    protected void submitBarCode() {
        String url = "http://" + setinfo.getString("Ip", "") + "/FirstPDAServer/home/" + submitBarUrl + "?loginid="
                + userBean.getStatus()
                + "&tDate=" + setinfo.getString("Date", "");
        for (MyContent myContent : strArr) {
            url += "&barcodes=" + myContent.getContent();
        }
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog.setHintText("提交中").show();
        threadPool.execute(new SubmitBarRunable(request));
    }

    /**
     * @author YZHY
     * @describe 作为执行请求类的父类
     */
    public class BaseRunable implements Runnable {
        protected Request request;
        protected int what;

        public BaseRunable(Request request) {
            this.request = request;
        }

        @Override
        public void run() {
            Response response = null;
            try {
                //回调
                response = client.newCall(request).execute();
                HashMap hashMap = new HashMap();
                hashMap.put("response", response);
                String resStr = response.body().string();
                hashMap.put("resStr", resStr);
                mHandler.obtainMessage(what, hashMap).sendToTarget();
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
            } finally {
                dialog.cancel();
                isScaning = false;
            }
        }
    }

    /**
     * @author YZHY
     * @describe 检查条码中 发起请求
     */
    public class CheckBarCodeRunable extends BaseRunable {
        public CheckBarCodeRunable(Request request) {
            super(request);
            what = 1;
        }
    }

    /**
     * @author YZHY
     * @describe 提交条码中 发起请求
     */
    public class SubmitBarRunable extends BaseRunable {

        public SubmitBarRunable(Request request) {
            super(request);
            what = 2;
        }
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            HandlerProcessing(msg);
        }
    };

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
            CheckBarPostProcessing(ReturnMessage);
        } else if (msg.what == 2) {
            SubmitBarPostProcessing(ReturnMessage);
        }
    }

    /**
     * @author YZHY
     * @describe 检查条码请求的后置处理
     */
    protected void CheckBarPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            if (status == -100) {
                mesg += "，或者扫描不清晰";
            }
            toast.setText(mesg);
            toast.show();
        } else {
            strArr.add(new MyContent(barcodeStr));
            renderList();
        }
    }

    /**
     * @author YZHY
     * @describe 提交条码请求的后置处理
     */
    protected void SubmitBarPostProcessing(String ReturnMessage) {
        BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
        int status = Integer.parseInt(barCodeBean.getStatus());
        String mesg = barCodeBean.getMsg();
        if (status != 0) {
            toast.setText(mesg);
            toast.show();
        } else {
            new AlertDialog.Builder(this).setTitle("单号:【" + mesg + "】")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            strArr.clear();
            renderList();
        }
    }

    /**
     * @author YZHY
     * @describe 页面生命周期：获取到焦点时触发
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        setinfo = getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }
        registerReceiver(mScanReceiver, filter);
    }

    /**
     * @author YZHY
     * @describe 页面生命周期：是去焦点时触发
     */
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mScanManager != null) {
            mScanManager.stopDecode();
            isScaning = false;
        }
        unregisterReceiver(mScanReceiver);
    }

    /**
     * @author YZHY
     * @describe 将数组中的值渲染至页面为list
     */
    protected void renderList() {
        BaseListActivity.MyAdapter myAdapter = new BaseListActivity.MyAdapter(BaseListActivity.this, strArr);
        listView.setAdapter(myAdapter);
        numberText.setText("记数：" + strArr.size() + "件");
        goToBottom();
    }

    /**
     * @author YZHY
     * @describe 当list中的item被点击的事项
     */
    protected void onItemClick(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {

    }

    /**
     * @author YZHY
     * @describe 当list中的item被删除的事项
     */
    protected void onItemDelet(int position, View v, ViewGroup parent, ArrayList<MyContent> datas) {
        MyContent myContent = datas.get(position);
        SlideLayout slideLayout = (SlideLayout) v.getParent();
        slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
        datas.remove(myContent);
        renderList();
    }


    /**
     * @author YZHY
     * @describe 渲染list的适配器
     */
    public class MyAdapter extends BaseAdapter {
        protected Context content;
        protected ArrayList<MyContent> datas;

        public MyAdapter(Context context, ArrayList<MyContent> datas) {
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
        public View getView(final int position, View convertView, final ViewGroup parent) {
            BaseListActivity.ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(content).inflate(R.layout.item_slide, null);
                viewHolder = new BaseListActivity.ViewHolder();
                viewHolder.contentView = (TextView) convertView.findViewById(R.id.content);
                viewHolder.menuView = (TextView) convertView.findViewById(R.id.menu);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (BaseListActivity.ViewHolder) convertView.getTag();
            }

            String s = datas.get(position).getContent();
            viewHolder.contentView.setTextSize(20);
            if (s.length() >= 25) {
                viewHolder.contentView.setTextSize(16);
            }

            viewHolder.contentView.setText(datas.get(position).getContent());

            viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position, v, parent, datas);
                }
            });
            viewHolder.menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemDelet(position, v, parent, datas);
                }
            });

            SlideLayout slideLayout = (SlideLayout) convertView;
            slideLayout.setOnStateChangeListener(new BaseListActivity.MyAdapter.MyOnStateChangeListener());


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

