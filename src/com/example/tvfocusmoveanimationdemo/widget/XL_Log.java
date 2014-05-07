/**
 * XL_Log.java 
 * com.example.tvfocusmoveanimationdemo.widget.XL_Log
 * @author: zhangzhi
 * @date: 2014年5月7日 下午1:13:15
 */
package com.example.tvfocusmoveanimationdemo.widget;

import com.example.tvfocusmoveanimationdemo.BuildConfig;

import android.util.Log;

/**
 * 
 * @author zhangzhi
 * 实现的主要功能。
 * 
 * 修改记录：修改者，修改日期，修改内容
 */
public class XL_Log {
	
	private String TAG=null;
	
	public XL_Log(Class<?> class1) {
		TAG=class1.getName();
	}
	
	public void debug(String string) {
		if(BuildConfig.DEBUG){
			Log.d(TAG, string);
		}
	}

}
