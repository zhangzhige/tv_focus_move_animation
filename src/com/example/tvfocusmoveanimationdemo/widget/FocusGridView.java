/**
 * FoucusSonLayout.java 
 * com.example.foucustest.FoucusSonLayout
 * @author: zhangzhi
 * @date: 2014年4月23日 下午5:30:21
 */
package com.example.tvfocusmoveanimationdemo.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.example.tvfocusmoveanimationdemo.widget.FocusMoveManager.GetFocusSonView;

/**
 * 
 * @author zhangzhi GridView的焦点移动动画
 * 此类必须外部设置setNextFocusUpId，不然可能会造成不可预见的向上按键错误
 */
public class FocusGridView extends GridView implements GetFocusSonView {

	XL_Log log = new XL_Log(FocusGridView.class);

	private FocusMoveManager mFocusMoveManager;

	private int mCurrentPosition = 0;

	private ListAdapter mLastAdapter;
	DataSetObserver mDataSetObserver=new DataSetObserver() {
		@Override
		public void onChanged() {
			log.debug("setAdapter onChanged mCurrentPosition="+mCurrentPosition+",count="+getChildCount());
			if(mCurrentPosition>=0 && getChildCount()>mCurrentPosition){
				View focusView=getFirstChild();
				log.debug("setAdapter onChanged focusView="+focusView);
				focusView.setScaleX(FocusMoveManager.DEFINE_SCALE_SIZE);
				focusView.setScaleY(FocusMoveManager.DEFINE_SCALE_SIZE);
				invalidate();
			}
		}

		@Override
		public void onInvalidated() {
			log.debug("setAdapter onInvalidated mCurrentPosition="+mCurrentPosition);
		}
	};
	public FocusGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public FocusGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FocusGridView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mFocusMoveManager = new FocusMoveManager(this, this);
		setSelected(true);
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if(mLastAdapter!=null){
			mLastAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		if(adapter!=null){
			mLastAdapter=adapter;
			adapter.registerDataSetObserver(mDataSetObserver);
		}
		super.setAdapter(adapter);
	}

	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		mFocusMoveManager.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		mFocusMoveManager.dispatchDraw(canvas);

	}

	public void setCurrentPosition(int pos) {// 刷新adapter前，在activity中调用这句传入当前选中的item在屏幕中的次序
		this.mCurrentPosition = pos;
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		// return super.getChildDrawingOrder(childCount, i);
		if (i == childCount - 1) {// 这是最后一个需要刷新的item
			return mCurrentPosition;
		}
		if (i == mCurrentPosition) {// 这是原本要在最后一个刷新的item
			return childCount - 1;
		}
		return i;// 正常次序的item
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mFocusMoveManager.dispatchKeyEvent(event)) {
			return true;
		}
		log.debug("dispatchKeyEvent super.dispatchKeyEvent");
		return super.dispatchKeyEvent(event);
	}

	@Override
	public View getFirstChild() {
		setSelection(mCurrentPosition);
		return getChildAt(mCurrentPosition);
	}

	public int getSelectedItemPosition() {
		return this.mCurrentPosition;
	}
	
	@Override
	public boolean needbringToFront() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.xunlei.cloud.widget.FocusMoveManager.GetFocusSonView#getDirectKeyEventView(android.view.View, android.view.KeyEvent)
	 */
	@Override
	public View getDirectKeyEventView(View currentView, KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_DPAD_UP:
			if(mCurrentPosition-getNumColumns()>=0){
				mCurrentPosition-=getNumColumns();
				setSelection(mCurrentPosition);
				return getChildAt(mCurrentPosition);
			}else{
				return focusSearch(View.FOCUS_UP);
			}
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if(mCurrentPosition+getNumColumns()<=getCount()-1){
				mCurrentPosition+=getNumColumns();
				setSelection(mCurrentPosition);
				return getChildAt(mCurrentPosition);
			}else{
				return focusSearch(View.FOCUS_DOWN);
			}
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if(mCurrentPosition>0){
				mCurrentPosition-=1;
				setSelection(mCurrentPosition);
				return getChildAt(mCurrentPosition);
			}else{
				return focusSearch(View.FOCUS_LEFT);
			}
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if(mCurrentPosition<getCount()-1){
				mCurrentPosition+=1;
				setSelection(mCurrentPosition);
				return getChildAt(mCurrentPosition);
			}else{
				return focusSearch(View.FOCUS_RIGHT);
			}
		default:
			break;
		}
		return null;
	}

}
