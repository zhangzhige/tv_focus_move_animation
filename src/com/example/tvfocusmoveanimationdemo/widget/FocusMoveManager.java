/**
 * FocusMoveManager.java 
 * com.xunlei.cloud.widget.FocusMoveManager
 * @author: zhangzhi
 * @date: 2014年5月4日 上午10:19:41
 */
package com.example.tvfocusmoveanimationdemo.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.example.tvfocusmoveanimationdemo.R;

/**
 * 
 * @author zhangzhi 实现的主要功能。
 * 
 *         修改记录：修改者，修改日期，修改内容
 */
public class FocusMoveManager {

	private XL_Log log = new XL_Log(FocusMoveManager.class);

	private Rect mSelectedPaddingRect = new Rect();

	private View mLastFoucusSonView = null;

	private View mCurrentFocusSonView = null;

	private Float currentScaleY = 0f;

	private Float currentScaleX = 0f;

	private ScaleType mCurrentScaleType = ScaleType.None;

	static final float DEFINE_SCALE_SIZE = 1.07f;

	private float screen_width;// 屏幕宽度，用来判断是否可滑动

	private static final float DEFINE_MARGIN = 40 * Constant.DENSITY;// 左右的margin值，当当前的focusView的left或者right值大于这个值得时候我们就需要滑动

	private static final int ANIMATION_DURATION = 2000;

	private Drawable mMySelectedDrawable = null;

	/**
	 * move动画开始的时候此类已经滑动的距离，此时只用向右滑动
	 */
	private int onMoveAnimationStartsScrollX = 0;

	/**
	 * move状态时，当前焦点对应的view的GlobalVisibleRect，在animation的时候需要用到
	 * 因为调用scaleX或者scaleY的时候此值会变，所以我们取一个全局的初始化值，即开始动画是就保存此变量
	 */
	private Rect mCurrentViewGlobalRect = null;
	
	private ValueAnimator mValueAnimator;
	
	private GetFocusSonView mGetFocusSonView;

	private static enum ScaleType {
		SingleZoom, // 单个放大，适用于从其他focus移动到当前FoucusParentLayout
		SingleNarrow, // 单个缩小，适用于从当前FoucusParentLayout移动到其他view
		Move, // 平移，适用于子view之间的焦点切换
		None
	}

	private ViewGroup mParent;

	public FocusMoveManager(ViewGroup v,GetFocusSonView gfs) {
		this.mParent = v;
		this.mParent.setFocusable(true);
		this.mMySelectedDrawable = this.mParent.getResources().getDrawable(R.drawable.tui_grid_focus);
		this.mSelectedPaddingRect = new Rect();
		this.mMySelectedDrawable.getPadding(this.mSelectedPaddingRect);
		log.debug("FocusMoveManager mMySelectedDrawable="+mSelectedPaddingRect);
		screen_width = this.mParent.getResources().getDisplayMetrics().widthPixels;
		
		if(gfs!=null){
			mGetFocusSonView=gfs;
		}else{
			mGetFocusSonView=new SimpleGetFocusSonView();
		}
	}
	
	class SimpleGetFocusSonView implements GetFocusSonView{

		@Override
		public View getFirstChild() {
			return mParent.getChildAt(0);
		}

		@Override
		public boolean needbringToFront() {
			return true;
		}

		
		@Override
		public View getDirectKeyEventView(View currentView, KeyEvent event) {
			View nextView=null;
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:// 向上
				nextView = mCurrentFocusSonView.focusSearch(View.FOCUS_UP);
				if(nextView==null || nextView.getParent() != mParent){
					nextView = mParent.focusSearch(View.FOCUS_UP);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:// 向下事件
				nextView = mCurrentFocusSonView.focusSearch(View.FOCUS_DOWN);
				if(nextView==null || nextView.getParent() != mParent){
					nextView = mParent.focusSearch(View.FOCUS_DOWN);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				nextView = mCurrentFocusSonView.focusSearch(View.FOCUS_LEFT);
				if(nextView==null || nextView.getParent() != mParent){
					nextView = mParent.focusSearch(View.FOCUS_LEFT);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				nextView = mCurrentFocusSonView.focusSearch(View.FOCUS_RIGHT);
				if(nextView==null || nextView.getParent() != mParent){
					nextView = mParent.focusSearch(View.FOCUS_RIGHT);
					log.debug("getDirectKeyEventView nextView="+nextView+",id="+mParent.getNextFocusRightId());
				}
				break;
			default:
				break;
			}
			return nextView;
		}
	}

	public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		log.debug("onFocusChanged gainFocus="+gainFocus+",direction="+direction+",previouslyFocusedRect="+previouslyFocusedRect+",this="+this);
		if (gainFocus) {// 单个放大
			if (mCurrentFocusSonView == null && this.mParent.getChildCount() > 0) {
				mCurrentFocusSonView = mGetFocusSonView.getFirstChild();
			}
			initSingleFocusScaleAnimation(mCurrentFocusSonView, gainFocus);
		} else {// 单个缩小
			// 此事件放到dispatchKeyEvent()的KeyEvent.KEYCODE_DPAD_UP里面去
		}
	}

	public void dispatchDraw(Canvas canvas) {
		switch (mCurrentScaleType) {
		case SingleZoom:
			drawSingleZoom(canvas, mCurrentFocusSonView);
			break;
		case SingleNarrow:
			drawSingleZoom(canvas, mCurrentFocusSonView);
			break;
		case Move:
			drawFocus(canvas, mCurrentFocusSonView, mLastFoucusSonView);
			break;
		default:
			break;
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {// 只处理up事件
			if (mCurrentFocusSonView == null) {
				mCurrentFocusSonView = this.mParent.getFocusedChild();
			}
			View nextfocusChildView = mGetFocusSonView.getDirectKeyEventView(mCurrentFocusSonView, event);
			log.debug("dispatchKeyEvent="+nextfocusChildView+",event.getKeyCode()="+event.getKeyCode());
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:// 向上
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (nextfocusChildView != null) {
					log.debug("dispatchKeyEvent="+nextfocusChildView.getParent());
					if (nextfocusChildView.getParent() != mParent) {// 此时我们判断为他的焦点外移，即此类没有焦点了，则单个缩小
						nextfocusChildView.requestFocus();//同时将焦点转移到外面去
						initSingleFocusScaleAnimation(mCurrentFocusSonView, false);
					} else {
						initMoveAnimation(nextfocusChildView, mCurrentFocusSonView);
					}
					return true;
				}else{
					if(mCurrentFocusSonView!=null){
						initSingleFocusScaleAnimation(mCurrentFocusSonView, false);
					}
				}
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 画单个图
	 * 
	 * @param focusSonView
	 * @param isZoom
	 *            :是否放大
	 */
	private void initSingleFocusScaleAnimation(View focusSonView,final boolean isZoom) {
		log.debug("initSingleFocusScaleAnimation="+focusSonView+",isZoom="+isZoom);
		if(mValueAnimator!=null && mValueAnimator.isRunning()){
			mValueAnimator.cancel();
		}
		
		mCurrentScaleType = isZoom ? ScaleType.SingleZoom : ScaleType.SingleNarrow;

		mCurrentFocusSonView = focusSonView;
		if(mGetFocusSonView.needbringToFront()){
			focusSonView.bringToFront();
		}
		if(isZoom){//放大时才需要焦点
			focusSonView.requestFocus();
		}

		PropertyValuesHolder pvhScaleX = null;
		PropertyValuesHolder pvhScaleY = null;
		if (isZoom) {
			pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, DEFINE_SCALE_SIZE);
			pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, DEFINE_SCALE_SIZE);
		} else {
			pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", DEFINE_SCALE_SIZE, 1.0f);
			pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", DEFINE_SCALE_SIZE, 1.0f);
		}
		mValueAnimator = ValueAnimator.ofPropertyValuesHolder(pvhScaleX, pvhScaleY).setDuration(ANIMATION_DURATION);
		mValueAnimator.setInterpolator(new DecelerateInterpolator());
		mValueAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				currentScaleX = isZoom ? DEFINE_SCALE_SIZE:1f;
				currentScaleY = isZoom ? DEFINE_SCALE_SIZE:1f;
				mParent.invalidate();
			}
		});
		mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				currentScaleX = (Float) animation.getAnimatedValue("scaleX");
				currentScaleY = (Float) animation.getAnimatedValue("scaleY");
				mParent.invalidate();
			}
		});
		mValueAnimator.start();
	}

	private void initMoveAnimation(View currentfocus, View mlastfocus) {
		if(mLastFoucusSonView!=null && mLastFoucusSonView.getScaleX()!=1.0f){
			mLastFoucusSonView.setScaleX(1.0f);
			mLastFoucusSonView.setScaleY(1.0f);
			mParent.invalidate();
		}
		if(mCurrentFocusSonView!=null && mCurrentFocusSonView.getScaleX()!=1.0f){
			mCurrentFocusSonView.setScaleX(1.0f);
			mCurrentFocusSonView.setScaleY(1.0f);
			mParent.invalidate();
		}
		if(mValueAnimator!=null && mValueAnimator.isRunning()){
			mValueAnimator.cancel();
		}
		log.debug("drawMoveAnimation mlastfocus=" + mlastfocus + ",currentfocus=" + currentfocus);
		mCurrentFocusSonView = currentfocus;
		if(mGetFocusSonView.needbringToFront()){
			mCurrentFocusSonView.bringToFront();
		}

		mLastFoucusSonView = mlastfocus;
		mCurrentScaleType = ScaleType.Move;

		onMoveAnimationStartsScrollX = mParent.getScrollX();
		Rect mLRect = new Rect(currentfocus.getLeft() - onMoveAnimationStartsScrollX, 
				currentfocus.getTop(), 
				currentfocus.getRight() - onMoveAnimationStartsScrollX, 
				currentfocus.getBottom());// 此处获取的是currentfocus所在矩阵减去滑动距离
		float ScaleOffset = DEFINE_SCALE_SIZE - 1.0f;
		mCurrentViewGlobalRect = new Rect(// 此处实际是获取的是放大之后的具体位置
				(int) (mLRect.left - ScaleOffset * (mLRect.right - mLRect.left) / 2), 
				(int) (mLRect.top - ScaleOffset * (mLRect.bottom - mLRect.top) / 2), 
				(int) (mLRect.right + ScaleOffset* (mLRect.right - mLRect.left) / 2), 
				(int) (mLRect.bottom + ScaleOffset * (mLRect.bottom - mLRect.top) / 2));

		PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, DEFINE_SCALE_SIZE);
		PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, DEFINE_SCALE_SIZE);

		mValueAnimator = ValueAnimator.ofPropertyValuesHolder(pvhScaleX, pvhScaleY).setDuration(ANIMATION_DURATION);
		mValueAnimator.setInterpolator(new DecelerateInterpolator());
		mValueAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mCurrentFocusSonView.requestFocus();
				
				currentScaleX = DEFINE_SCALE_SIZE;
				currentScaleY = DEFINE_SCALE_SIZE;
				mParent.invalidate();
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				currentScaleX = DEFINE_SCALE_SIZE;
				currentScaleY = DEFINE_SCALE_SIZE;
				mParent.invalidate();
			}
		});
		mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				currentScaleX = (Float) animation.getAnimatedValue("scaleX");
				currentScaleY = (Float) animation.getAnimatedValue("scaleY");
				mParent.invalidate();
			}
		});
		mValueAnimator.start();
	}

	private void drawSingleZoom(Canvas canvas, View focusView) {
		if (focusView == null) {
			return;
		}
		
		float currentX = currentScaleX - 1;
		float currentY = currentScaleY - 1;
		float ScaleOffset = DEFINE_SCALE_SIZE - 1;
		
		focusView.setScaleX(currentScaleX);
		focusView.setScaleY(currentScaleY);

		Rect lastRect = new Rect(
				focusView.getLeft()+focusView.getPaddingLeft(), 
				focusView.getTop()+focusView.getPaddingTop(), 
				focusView.getRight()-focusView.getPaddingRight(), 
				focusView.getBottom()-focusView.getPaddingBottom());

		lastRect.top -= mSelectedPaddingRect.top;
		lastRect.left -= mSelectedPaddingRect.left;
		lastRect.right += mSelectedPaddingRect.right;
		lastRect.bottom += mSelectedPaddingRect.bottom;

		Rect focusRect = new Rect(
				focusView.getLeft()+focusView.getPaddingLeft(), 
				focusView.getTop()+focusView.getPaddingTop(), 
				focusView.getRight()-focusView.getPaddingRight(), 
				focusView.getBottom()-focusView.getPaddingBottom());
		
		Rect currentRect = new Rect(
				Math.round(focusRect.left - ScaleOffset * (focusRect.right - focusRect.left) / 2), 
				Math.round(focusRect.top - ScaleOffset * (focusRect.bottom - focusRect.top) / 2),
				Math.round(focusRect.right + ScaleOffset * (focusRect.right - focusRect.left) / 2), 
				Math.round(focusRect.bottom + ScaleOffset * (focusRect.bottom - focusRect.top) / 2));

		currentRect.top -= mSelectedPaddingRect.top;
		currentRect.left -= mSelectedPaddingRect.left;
		currentRect.right += mSelectedPaddingRect.right;
		currentRect.bottom += mSelectedPaddingRect.bottom;

		int left = Math.round(((currentRect.left - lastRect.left) * currentX / ScaleOffset + lastRect.left));
		int right =Math.round(((currentRect.right - lastRect.right) * currentX / ScaleOffset + lastRect.right));
		int top = Math.round(((currentRect.top - lastRect.top) * currentY / ScaleOffset + lastRect.top));
		int bottom = Math.round(((currentRect.bottom - lastRect.bottom) * currentY / ScaleOffset + lastRect.bottom));

		Rect dstRect = new Rect(left, top, right, bottom);
		mMySelectedDrawable.setBounds(dstRect);
		int alpha = (int) (255 * (currentX / ScaleOffset));
		mMySelectedDrawable.setAlpha(alpha);
		mMySelectedDrawable.draw(canvas);
		mMySelectedDrawable.setVisible(true, true);
	}

	private void drawFocus(Canvas canvas, View focusView, View mLastFocusView) {
		float currentX = currentScaleX - 1;
		float currentY = currentScaleY - 1;

		float ScaleOffset = DEFINE_SCALE_SIZE - 1;
		
		focusView.setScaleX(currentScaleX);
		focusView.setScaleY(currentScaleY);
		
		mLastFocusView.setScaleX(DEFINE_SCALE_SIZE + 1 - currentScaleX);
		mLastFocusView.setScaleY(DEFINE_SCALE_SIZE + 1  - currentScaleY);
		log.debug("drawFocus mLastFocusView="+mLastFocusView.getScaleX()+",focusView="+focusView.getScaleX());
		

		if (mParent.getWidth() > screen_width) {// 可以滑动
			float scrollOffset = 0.0f;
			if (mCurrentViewGlobalRect.right > screen_width - DEFINE_MARGIN) {// 如果当前的焦点view超过这个阈值
				scrollOffset = mCurrentViewGlobalRect.right - screen_width + DEFINE_MARGIN;// 需要向左滑动的距离
				int scrollTo = onMoveAnimationStartsScrollX + (int) (scrollOffset * currentX / ScaleOffset);
				mParent.scrollTo(scrollTo, 0);
			} else if (mCurrentViewGlobalRect.left < DEFINE_MARGIN) {// 需要向左滑动
				scrollOffset = DEFINE_MARGIN - mCurrentViewGlobalRect.left;
				int scrollTo = onMoveAnimationStartsScrollX - (int) (scrollOffset * currentX / ScaleOffset);
				mParent.scrollTo(scrollTo, 0);
			}
		}
		
		Rect mLRect = new Rect(
				mLastFocusView.getLeft()+mLastFocusView.getPaddingLeft(), 
				mLastFocusView.getTop()+mLastFocusView.getPaddingTop(), 
				mLastFocusView.getRight()-mLastFocusView.getPaddingRight(), 
				mLastFocusView.getBottom()-mLastFocusView.getPaddingBottom());
		Rect lastRect = new Rect(
				Math.round(mLRect.left - ScaleOffset * (mLRect.right - mLRect.left) / 2), 
				Math.round(mLRect.top - ScaleOffset * (mLRect.bottom - mLRect.top) / 2),
				Math.round(mLRect.right + ScaleOffset * (mLRect.right - mLRect.left) / 2), 
				Math.round(mLRect.bottom + ScaleOffset * (mLRect.bottom - mLRect.top) / 2));
		lastRect.top -= mSelectedPaddingRect.top;
		lastRect.left -= mSelectedPaddingRect.left;
		lastRect.right += mSelectedPaddingRect.right;
		lastRect.bottom += mSelectedPaddingRect.bottom;

		Rect focusRect = new Rect(
				focusView.getLeft()+focusView.getPaddingLeft(), 
				focusView.getTop()+focusView.getPaddingTop(), 
				focusView.getRight()-focusView.getPaddingRight(), 
				focusView.getBottom()-focusView.getPaddingBottom()
				);
		Rect currentRect = new Rect(
				Math.round(focusRect.left - ScaleOffset * (focusRect.right - focusRect.left) / 2), 
				Math.round(focusRect.top - ScaleOffset * (focusRect.bottom - focusRect.top) / 2),
				Math.round(focusRect.right + ScaleOffset * (focusRect.right - focusRect.left) / 2), 
				Math.round(focusRect.bottom + ScaleOffset * (focusRect.bottom - focusRect.top) / 2));
		currentRect.top -= mSelectedPaddingRect.top;
		currentRect.left -= mSelectedPaddingRect.left;
		currentRect.right += mSelectedPaddingRect.right;
		currentRect.bottom += mSelectedPaddingRect.bottom;
		
		int left = Math.round(((currentRect.left - lastRect.left) * currentX / ScaleOffset + lastRect.left));
		int right = Math.round(((currentRect.right - lastRect.right) * currentX / ScaleOffset + lastRect.right));
		int top = Math.round(((currentRect.top - lastRect.top) * currentY / ScaleOffset + lastRect.top));
		int bottom = Math.round(((currentRect.bottom - lastRect.bottom) * currentY / ScaleOffset + lastRect.bottom));
		
		Rect dstRect = new Rect(left, top, right, bottom);
		mMySelectedDrawable.setAlpha(255);//还原因为drawSingleZoom设置的alpha值
		this.mMySelectedDrawable.setBounds(dstRect);
		this.mMySelectedDrawable.draw(canvas);
		this.mMySelectedDrawable.setVisible(true, true);
	}
	
	public static interface GetFocusSonView{
		/**
		 * 获取从外部进入时候的第一个子焦点view
		 * mParent.onFocusChange获取焦点时候，应该让第一个子view首先放大获取焦点
		 * 此处获取的是第一个子view
		 * 外部可以自定义返回的到底是哪个
		 * @return
		 */
		public View getFirstChild();
		/**
		 * 是否需要调用bringToFront函数
		 * 因为GridView或者ListView调用bringToFront函数后会将第一个子view移到最后面去
		 * 所以暂时加一个这个函数来判断，后续可以去掉
		 * @return
		 */
		public boolean needbringToFront();
		/**
		 * 获取下一个焦点位置
		 * 传入当前的焦点view，和点击事件，继承类自定义需要返回的下一个焦点view
		 * @param currentView
		 * @param event
		 * @return
		 */
		public View getDirectKeyEventView(View currentView,KeyEvent event);
	}

}
