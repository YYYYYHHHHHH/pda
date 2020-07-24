package com.example.domn;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.domn.bean.BarCodeBean;
import com.example.domn.bean.UserBean;
import com.example.domn.commpont.MyContent;
import com.example.domn.commpont.SlideLayout;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListActivity extends Activity {
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private boolean isScaning = false;
    private SoundPool soundpool = null;
    private String barcodeStr;
    private ScanManager mScanManager;
    private ZLoadingDialog dialog;
    private UserBean userBean;
    private String whId;
    private Set<SlideLayout> sets = new HashSet();
    private Toast toast;

    private int soundid;
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            isScaning = false;
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
            checkBarCode(barcodeStr);
        }
    };
    final OkHttpClient client = new OkHttpClient();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dialog.cancel();
            if (msg.what == 1) {
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息", ReturnMessage);
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
                    MyAdapter myAdapter = new ListActivity.MyAdapter(ListActivity.this, strArr);
                    listView.setAdapter(myAdapter);
                    numberText.setText("记数：" + strArr.size() + "件");
                }
            } else if (msg.what == 2) {
                String ReturnMessage = (String) msg.obj;
                Log.i("获取的返回信息", ReturnMessage);
                BarCodeBean barCodeBean = new Gson().fromJson(ReturnMessage, BarCodeBean.class);
                int status = Integer.parseInt(barCodeBean.getStatus());
                String mesg = barCodeBean.getMsg();

                toast.setText(mesg);
                toast.show();
                if (status != 0) {

                } else {
                    strArr.clear();
                    MyAdapter myAdapter = new ListActivity.MyAdapter(ListActivity.this, strArr);
                    listView.setAdapter(myAdapter);
                    numberText.setText("记数：" + strArr.size() + "件");
                }
            }
        }
    };

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
//        showScanResult.setText("");
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

    private TextView numberText = null;
    private EditText inputCode = null;
    private ListView listView = null;
    private ArrayList<MyContent> strArr = null;
    private Button inputButton = null;
    private Button clear = null;
    private Button submit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.inputCode = (EditText) findViewById(R.id.inputCode);
        this.listView = (ListView) findViewById(R.id.codeitem);
        this.numberText = (TextView) findViewById(R.id.numberText);
        this.clear = (Button) findViewById(R.id.clear);
        this.inputButton = (Button) findViewById(R.id.inputButton);
        this.submit = (Button) findViewById(R.id.submit);

        SharedPreferences setinfo =  getSharedPreferences("GlobalData",Context.MODE_PRIVATE);
        userBean = new Gson().fromJson(setinfo.getString("user", ""), UserBean.class);
        Intent intent = getIntent();
        whId = intent.getStringExtra("whId");
        toast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 70);
        this.inputCode();
        this.listView();
        this.initClaer();
        this.initInputButton();
        this.initSubmit();

    }

    private void initSubmit() {
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strArr.size() <= 0) {
                    toast.setText("没有要提交的条码");
                    toast.show();
                    return;
                }
                new AlertDialog.Builder(ListActivity.this).setTitle("一共有" + strArr.size() + "件，确认要提交吗")
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

    private void initInputButton() {
        this.inputButton.setOnClickListener(new View.OnClickListener() {
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

    private void initClaer() {
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ListActivity.this).setTitle("确认要清空吗")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                strArr = new ArrayList<>();
                                MyAdapter myAdapter = new ListActivity.MyAdapter(ListActivity.this, strArr);
                                listView.setAdapter(myAdapter);
                                numberText.setText("记数：" + strArr.size() + "件");
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

    private void inputCode() {
        inputCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputCode.setFocusable(true);
                inputCode.setFocusableInTouchMode(true);
                inputCode.requestFocus();
            }
        });
    }

    private void checkBarCode(String barcodeStr) {
        final Request request = new Request.Builder()
                .url("http://192.168.11.243/FirstPDAServer/home/GetBarStatus?barcode=" + barcodeStr)
                .get()
                .build();
        dialog = new ZLoadingDialog(ListActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("检查条码中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        mHandler.obtainMessage(1, response.body().string()).sendToTarget();

                    } else {
                        dialog.cancel();
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (IOException e) {
                    dialog.cancel();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void submitBarCode() {
        String url = "http://192.168.11.243/FirstPDAServer/home/CommitBarToStock?loginId=" + userBean.getStatus() + "&whId=" + whId;
        for (MyContent myContent : strArr) {
            url += "&barcodes=" + myContent.getContent();
        }
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        dialog = new ZLoadingDialog(ListActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("提交中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        mHandler.obtainMessage(2, response.body().string()).sendToTarget();

                    } else {
                        dialog.cancel();
                        throw new IOException("Unexpected code:" + response);
                    }
                } catch (IOException e) {
                    dialog.cancel();
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private void listView() {
        strArr = new ArrayList<>();
        MyAdapter myAdapter = new ListActivity.MyAdapter(this, strArr);
        listView.setAdapter(myAdapter);
    }
    class MyAdapter extends BaseAdapter
    {
        private Context content;
        private ArrayList<MyContent> datas;
        private MyAdapter(Context context, ArrayList<MyContent> datas)
        {
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
            testActivity.ViewHolder viewHolder=null;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(content).inflate(R.layout.item_slide, null);
                viewHolder = new testActivity.ViewHolder();
                viewHolder.contentView= (TextView) convertView.findViewById(R.id.content);
                viewHolder.menuView = (TextView) convertView.findViewById(R.id.menu);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (testActivity.ViewHolder) convertView.getTag();
            }
            viewHolder.contentView.setText(datas.get(position).getContent());

            viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            final MyContent myContent = datas.get(position);
            viewHolder.menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SlideLayout slideLayout = (SlideLayout) v.getParent();
                    slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
                    datas.remove(myContent);
                    numberText.setText("记数：" + strArr.size() + "件");
                    notifyDataSetChanged();
                }
            });

            SlideLayout slideLayout = (SlideLayout) convertView;
            slideLayout.setOnStateChangeListener(new ListActivity.MyAdapter.MyOnStateChangeListener());


            return convertView;
        }

        public SlideLayout slideLayout = null;
        class MyOnStateChangeListener implements SlideLayout.OnStateChangeListener
        {
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
                if (slideLayout != null && slideLayout !=layout)
                {
                    slideLayout.closeMenu();
                }
            }

            @Override
            public void onClose(SlideLayout layout) {
                if (sets.size() > 0) {
                    sets.remove(layout);
                }
                if(slideLayout ==layout){
                    slideLayout = null;
                }
            }
        }
    }
    static class ViewHolder
    {
        public TextView contentView;
        public TextView menuView;
    }
}
