package com.sinpo.xnfc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sinpo.xnfc.R;
import com.sinpo.xnfc.bean.CardInfo;

import java.util.List;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename CardListAdapter.java
 * @time 2018/6/28 10:00
 * @copyright(C) 2018 song
 */
public class CardListAdapter extends BaseAdapter {
    private Context context;
    private int layoutId;
    private List<CardInfo.ConsumeRecord> datas;

    public CardListAdapter(Context context, int layoutId, List<CardInfo.ConsumeRecord> datas) {
        this.context = context;
        this.layoutId =layoutId;
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
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = View.inflate(context,layoutId, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder =  (ViewHolder) convertView.getTag();
        }
        //设置数据
        CardInfo.ConsumeRecord item = datas.get(position);
        viewHolder.tv_transactionNo.setText(item.getTransactionNo());
        viewHolder.tv_consumeTime.setText(item.getConsumeTime());
        viewHolder.tv_consumeMoney.setText(item.getConsumeMoney());
        return convertView;
    }

    class ViewHolder{
        TextView tv_transactionNo;
        TextView tv_consumeTime;
        TextView tv_consumeMoney;

        public ViewHolder(View convertView) {
            tv_transactionNo = (TextView) convertView.findViewById(R.id.tv_transactionNo);
            tv_consumeTime = (TextView) convertView.findViewById(R.id.tv_consumeTime);
            tv_consumeMoney = (TextView) convertView.findViewById(R.id.tv_consumeMoney);
        }
    }
}
