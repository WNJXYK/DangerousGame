package cn.Arthur.Game.DangerousGame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GameView extends View {

	MainActivity mActivity;
	private int screenH,screenW;
	private Typeface mFace;   
	int btnW,btnH;
	int btnCW,btnCH;
	int tipW,tipH;
	int bgW,bgH;
	int bgNUH;
	int bgUW,bgUH;
	Bitmap btnReady,btnReplay,btnRanklist,btnShare,btnScore;
	Bitmap tipWaitforstill, tipWaitforplay;
	Bitmap bgFight,bgResult,bgTitle;
	int Dx[]={0,0,0,0,0,0,0,0,0,0,0,0},Dy[]={0,1,2,3,2,1,0,-1,-2,-3,-2,-1};
	int Df=0;
	int Dt=0;
	long oldTime=0;
	
	public GameView(Context context) {
		super(context);
		mActivity = (MainActivity) context;
		initView();
		
		setFocusable(true);
		
	}

	/**
	 * 初始化画布
	 */
	private void initView(){
		//GetScreenInfo
		screenW =  getResources().getDisplayMetrics().widthPixels;
		screenH =  getResources().getDisplayMetrics().heightPixels;
		Log.e("ScreenW", String.valueOf(screenW));
		Log.e("ScreenH", String.valueOf(screenH));
		//GetRes
		mFace = Typeface.createFromAsset(getContext().getAssets(),"font.otf");
		btnReady=getRes(R.drawable.btn_ready);
		btnReplay=getRes(R.drawable.btn_replay);
		btnShare=getRes(R.drawable.btn_share);
		btnScore=getRes(R.drawable.btn_sharescore);
		btnRanklist=getRes(R.drawable.btn_ranklist);
		bgFight=getRes(R.drawable.bg_fight);
		bgResult=getRes(R.drawable.bg_result);
		bgTitle=getRes(R.drawable.bg_title);
		tipWaitforstill=getRes(R.drawable.tip_waitforstill);
		tipWaitforplay=getRes(R.drawable.tip_waitforplay);
		btnW=screenW/3;
		btnH=btnReady.getHeight()*btnW/btnReady.getWidth();
		btnCW=screenW/3;
		btnCH=btnShare.getHeight()*btnCW/btnShare.getWidth();
		bgW=screenW/20*18;
		bgH=bgFight.getHeight()*bgW/bgFight.getWidth();
		bgNUH=bgH*185/600;
		bgUH=bgH*405/600;
		bgUW=bgW*480/500;
		tipW=screenW/4*3;
		tipH=tipWaitforstill.getHeight()*tipW/tipWaitforstill.getWidth();
	}

	/**
	 * 激活画布
	 */
	public void ActiveView(){
		//浮动效果
		if (++Dt>10){
			Df=(Df+1)%12;
			Dt=0;
		}
		invalidate();
		//重新绘制画布
	}
	
	/**
	 * 绘制界面
	 * @param canvas
	 */
	private void refreshUI(Canvas canvas){
		int state=mActivity.getGameState();
		int StartH=0;
		int deltaH=0;
		switch(state){
		case 0:
			drawBitmap(bgTitle,screenW/2,bgH/5*3,bgW,bgH,canvas,null);
			drawBitmap(btnReady,screenW/4,screenH-btnH,btnW,btnH,canvas,null);
			drawBitmap(btnRanklist,screenW/4*3,screenH-btnH,btnW,btnH,canvas,null);
			drawBitmap(btnShare,screenW/2+Dx[Df],screenH/4*3+Dy[Df],btnCW,btnCH,canvas,null);
			break;
		case 1:
			//等待平放
			drawBitmap(tipWaitforstill,screenW/2,screenH/2,tipW,tipH,canvas,null);
			break;
		case 2:
			//等待旋转
			drawBitmap(tipWaitforplay,screenW/2,screenH/2,tipW,tipH,canvas,null);
			break;
		case 3:
			//旋转中
			drawBitmap(bgFight,screenW/2,screenH/2,bgW,bgH,canvas,null);
			StartH=screenH/2-bgH/2+bgNUH;
			deltaH=bgUH/5;
			StartH+=deltaH/2;
			drawText("剩余时间："+String.format("%.2f", (float)mActivity.getRestTime()/100)+"S",canvas,screenW/2,StartH+deltaH*0,bgW*9/10,deltaH,(mActivity.getRestTime()>=300?Color.GRAY:Color.RED));
			drawText("得分："+String.valueOf(mActivity.getScore()),canvas,screenW/2,StartH+deltaH*1,bgW,deltaH,Color.GRAY);
			drawText("旋转指数："+String.valueOf(mActivity.getXR()),canvas,screenW/2,StartH+deltaH*2,bgW,deltaH,Color.GRAY);
			drawText("空翻指数："+String.valueOf(mActivity.getYR()),canvas,screenW/2,StartH+deltaH*3,bgW,deltaH,Color.GRAY);
			drawText("侧翻指数："+String.valueOf(mActivity.getZR()),canvas,screenW/2,StartH+deltaH*4,bgW,deltaH,Color.GRAY);
			break;
		case 4:
			//游戏已结束
			drawBitmap(bgResult,screenW/2,screenH/2,bgW,bgH,canvas,null);
			StartH=screenH/2-bgH/2+bgNUH;
			deltaH=bgUH/5;
			StartH+=deltaH/2;
			drawText(mActivity.getRank(),canvas,screenW/2,StartH+deltaH*0,bgW,deltaH,Color.GRAY);
			drawText("得分："+String.valueOf(mActivity.getScore())+"",canvas,screenW/2,StartH+deltaH*1,bgW*9/10,deltaH,Color.GRAY);
			drawText("旋转指数："+String.valueOf(mActivity.getXR()),canvas,screenW/2,StartH+deltaH*2,bgW,deltaH,Color.GRAY);
			drawText("空翻指数："+String.valueOf(mActivity.getYR()),canvas,screenW/2,StartH+deltaH*3,bgW,deltaH,Color.GRAY);
			drawText("侧翻指数："+String.valueOf(mActivity.getZR()),canvas,screenW/2,StartH+deltaH*4,bgW,deltaH,Color.GRAY);
			drawBitmap(btnReplay,screenW/4,screenH-btnH,btnW,btnH,canvas,null);
			drawBitmap(btnRanklist,screenW/4*3,screenH-btnH,btnW,btnH,canvas,null);
			drawBitmap(btnScore,screenW/2+Dx[Df],btnH+Dy[Df],btnW*2,btnH,canvas,null);
			break;
		}
	}
	
	/**
	 * 绘制背景色
	 */
	private void drawBackGround(Canvas canvas){
		Paint paint=new Paint();
		paint.setColor(Color.rgb(255, 215, 50));
		canvas.drawRect(new Rect(0,0,screenW,screenH), paint);
	}
	
	/**
	 * 重写父类绘图函数
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		drawBackGround(canvas);
		refreshUI(canvas);
		super.onDraw(canvas);
	}

	/**
	 * 绘制图像
	 * @param res
	 * @param src
	 * @param canvas
	 * @param paint
	 */
	private void drawBitmap(Bitmap res,int x,int y,int w,int h,Canvas canvas,Paint paint){
		canvas.drawBitmap(res, new Rect(0,0,res.getWidth(),res.getHeight()), new Rect(x-w/2,y-h/2,x+w/2,y+h/2), paint);
	}
	/**
	 * 绘制文字
	 * @param str
	 * @param canvas
	 */
	private void drawText(String str,Canvas canvas,int x,int y,int width,int height,int color){
		Paint countPaint = new Paint();
		countPaint.setColor(color);
		countPaint.setTextSize(100);
		countPaint.setTypeface(mFace);
		countPaint.setTextAlign(Paint.Align.CENTER);
		Rect textBounds = new Rect();
		countPaint.getTextBounds(str, 0, str.length(), textBounds);//get text bounds, that can get the text width and height
		int textWidth =textBounds.right-textBounds.left;
		int textHeight=textBounds.bottom-textBounds.top;
		while (textWidth>width || textHeight>height){
			countPaint.setTextSize(countPaint.getTextSize()-5);
			countPaint.getTextBounds(str, 0, str.length(), textBounds);
			textWidth =textBounds.right-textBounds.left;
			textHeight=textBounds.bottom-textBounds.top;
		}
		canvas.drawText(str, x,y+textHeight/3,countPaint);
	}
	
	/**
	 * 返回图片资源
	 */
	public Bitmap getRes(int resID) {
		return BitmapFactory.decodeResource(getResources(),resID);
	}
	
	
	/**
	 * 返回是否点击中
	 * @param x
	 * @param y
	 * @param mx
	 * @param my
	 * @param w
	 * @param h
	 * @return
	 */
	private boolean isInRect(int x,int y,int mx,int my,int w,int h){
		if (mx-w/2<=x && x<=mx+w/2 && my-h/2<=y && y<=my+h/2){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x=(int) event.getX();
		int y=(int) event.getY();
		if (Math.abs(oldTime-System.currentTimeMillis())<=500) return true;
		oldTime=System.currentTimeMillis();
		//开始
		if (screenW/4-btnW<=x && x<=screenW/4+btnW/2 && screenH-btnH-btnH/2<=y && y<=screenH-btnH/2){
			mActivity.buttonClicked();
			return true;
		}
		
		//排行
		if (screenW/4*3-btnW<=x && x<=screenW/4*3+btnW/2 && screenH-btnH-btnH/2<=y && y<=screenH-btnH/2){
			mActivity.rankList();
			return true;
		}
		
		//分享
		if (isInRect(x,y,screenW/2,screenH/4*3,btnCW,btnCH)){
			if (mActivity.getGameState()==0){
				mActivity.showShare("超级好玩的体感游戏：《暴走大旋转》！不管使用什么的方法，总之让你的手机高速旋转起来取得高分吧！我在这里等着你哦！ 下载地址：http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk","http://androidapplication.qiniudn.com/DangerousGame/ic_launcher-web.png","http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk");
			}
			return true;
		}
		
		//炫耀
		if (isInRect(x,y,screenW/2,btnH,btnW*2,btnH)){
			if (mActivity.getGameState()==4){
				mActivity.showShare("我在暴走大旋转中获得了"+String.valueOf(mActivity.getScore())+"分！你也快点来试试吧！ 下载地址：http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk", "http://androidapplication.qiniudn.com/DangerousGame/ic_launcher-web.png","http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk");
			}
			return true;
		}
		
		//帮助
		if (mActivity.getGameState()==0 && isInRect(x,y,screenW/8,bgH/5*3+bgH/4,bgW/4,bgH/3)){
			mActivity.HelpDialog();
			return true;
		}
		
		//invalidate();
		return true;
	}

}
