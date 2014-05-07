/**
 * GridViewActivity.java 
 * com.example.tvfocusmoveanimationdemo.GridViewActivity
 * @author: zhangzhi
 * @date: 2014年5月7日 下午2:42:46
 */
package com.example.tvfocusmoveanimationdemo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.tvfocusmoveanimationdemo.widget.FocusGridView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * @author zhangzhi
 * 实现的主要功能。
 * 
 * 修改记录：修改者，修改日期，修改内容
 */
public class GridViewActivity extends Activity {
	
	FocusGridView mFocusGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview);
		
		mFocusGridView=(FocusGridView) findViewById(R.id.layout_live_play);
		mFocusGridView.setAdapter(new LivePlayAdapter(this));
	}
	
	
	private class LivePlayAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private static final float SCALE_XY=38.0f/27.0f;
		
		AbsListView.LayoutParams mLayoutParams;

		public LivePlayAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			int paddingleft=mFocusGridView.getPaddingLeft();
			int paddingRight=mFocusGridView.getPaddingRight();
			int verticalSpacing=mFocusGridView.getVerticalSpacing();
			int numColumns=mFocusGridView.getNumColumns();
			if(numColumns<0){
				numColumns=6;
			}
			int screen_width =getResources().getDisplayMetrics().widthPixels;
			
			int item_width=(screen_width-paddingleft-paddingRight-(numColumns-1)*verticalSpacing)/numColumns;
			mLayoutParams=new AbsListView.LayoutParams(item_width, (int) (SCALE_XY*item_width));
			android.util.Log.d("mLayoutParams", "mLayoutParams="+mLayoutParams.width+",="+mLayoutParams.height);
		}

		@Override
		public int getCount() {
			return 30;
		}

		@Override
		public String getItem(int position) {
			return Integer.toString(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.channel_recommend_item, null);
				convertView.setLayoutParams(mLayoutParams);

				holder = new Holder();
				holder.channelLogo = (RelativeLayout) convertView.findViewById(R.id.iv_channel_logo);
				holder.curPgm = (TextView) convertView.findViewById(R.id.tv_cur_program);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.curPgm.setText(getItem(position));
			return convertView;
		}
		
		public String getdate() {
			Date dt = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			return sdf.format(dt);
		}

		private class Holder {
			RelativeLayout channelLogo;
			 TextView curPgm;
		}
	}

}
