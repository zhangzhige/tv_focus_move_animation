/**
 * Constant.java 
 * com.example.tvfocusmoveanimationdemo.widget.Constant
 * @author: zhangzhi
 * @date: 2014年5月7日 下午1:16:52
 */
package com.example.tvfocusmoveanimationdemo.widget;

import android.content.Context;

/**
 * 
 * @author zhangzhi
 * 实现的主要功能。
 * 
 * 修改记录：修改者，修改日期，修改内容
 */
public class Constant {

	public static float DENSITY = 0;
	
	public static final void initConstant(Context context){
		DENSITY=context.getResources().getDisplayMetrics().density;
	}

}
