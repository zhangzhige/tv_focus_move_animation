/**
 * FoucusSonLayout.java 
 * com.example.foucustest.FoucusSonLayout
 * @author: zhangzhi
 * @date: 2014年4月23日 下午5:30:21
 */
package com.example.tvfocusmoveanimationdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/**
 * 
 * @author zhangzhi
 * 实现的主要功能。
 * 
 * 修改记录：修改者，修改日期，修改内容
 */
public class FoucusParentLayout extends RelativeLayout {
	
	FocusMoveManager mFocusMoveManager;
	
	public FoucusParentLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public FoucusParentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FoucusParentLayout(Context context) {
		super(context);
		init();
	}

	private void init(){
		mFocusMoveManager=new FocusMoveManager(this,null);
	}
	
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect){
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		mFocusMoveManager.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}
	
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		mFocusMoveManager.dispatchDraw(canvas);
		
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(mFocusMoveManager.dispatchKeyEvent(event)){
			return true;
		}
		return super.dispatchKeyEvent(event);
		
	}


	
	

}
