package com.example.pda;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pda.R;
import com.example.pda.commpont.MyContent;
import com.example.pda.commpont.SlideLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class testActivity extends Activity {
    private ListView listView;
    private ArrayList<MyContent> mDatas;
    private MyAdapter myAdapter;
    private Set<SlideLayout> sets = new HashSet();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_slide);
        listView = (ListView) findViewById(R.id.main_list);

        mDatas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mDatas.add(new MyContent("content"+i));
        }
        myAdapter = new MyAdapter(this, mDatas);
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
            ViewHolder viewHolder=null;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(content).inflate(R.layout.item_slide, null);
                viewHolder = new ViewHolder();
                viewHolder.contentView= (TextView) convertView.findViewById(R.id.content);
                viewHolder.menuView = (TextView) convertView.findViewById(R.id.menu);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.contentView.setText(datas.get(position).getContent());

            viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(content, "click "+((TextView)v).getText(), Toast.LENGTH_SHORT).show();
                }
            });
            final MyContent myContent = datas.get(position);
            viewHolder.menuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SlideLayout slideLayout = (SlideLayout) v.getParent();
                    slideLayout.closeMenu(); //解决删除item后下一个item变成open状态问题
                    datas.remove(myContent);
                    notifyDataSetChanged();
                }
            });

            SlideLayout slideLayout = (SlideLayout) convertView;
            slideLayout.setOnStateChangeListener(new MyOnStateChangeListener());


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

