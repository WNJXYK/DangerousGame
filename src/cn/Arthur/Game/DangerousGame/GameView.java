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
	 * ��ʼ������
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
	 * �����
	 */
	public void ActiveView(){
		//����Ч��
		if (++Dt>10){
			Df=(Df+1)%12;
			Dt=0;
		}
		invalidate();
		//���»��ƻ���
	}
	
	/**
	 * ���ƽ���
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
			//�ȴ�ƽ��
			drawBitmap(tipWaitforstill,screenW/2,screenH/2,tipW,tipH,canvas,null);
			break;
		case 2:
			//�ȴ���ת
			drawBitmap(tipWaitforplay,screenW/2,screenH/2,tipW,tipH,canvas,null);
			break;
		case 3:
			//��ת��
			drawBitmap(bgFight,screenW/2,screenH/2,bgW,bgH,canvas,null);
			StartH=screenH/2-bgH/2+bgNUH;
			deltaH=bgUH/5;
			StartH+=deltaH/2;
			drawText("ʣ��ʱ�䣺"+String.format("%.2f", (float)mActivity.getRestTime()/100)+"S",canvas,screenW/2,StartH+deltaH*0,bgW*9/10,deltaH,(mActivity.getRestTime()>=300?Color.GRAY:Color.RED));
			drawText("�÷֣�"+String.valueOf(mActivity.getScore()),canvas,screenW/2,StartH+deltaH*1,bgW,deltaH,Color.GRAY);
			drawText("��תָ����"+String.valueOf(mActivity.getXR()),canvas,screenW/2,StartH+deltaH*2,bgW,deltaH,Color.GRAY);
			drawText("�շ�ָ����"+String.valueOf(mActivity.getYR()),canvas,screenW/2,StartH+deltaH*3,bgW,deltaH,Color.GRAY);
			drawText("�෭ָ����"+String.valueOf(mActivity.getZR()),canvas,screenW/2,StartH+deltaH*4,bgW,deltaH,Color.GRAY);
			break;
		case 4:
			//��Ϸ�ѽ���
			drawBitmap(bgResult,screenW/2,screenH/2,bgW,bgH,canvas,null);
			StartH=screenH/2-bgH/2+bgNUH;
			deltaH=bgUH/5;
			StartH+=deltaH/2;
			drawText(mActivity.getRank(),canvas,screenW/2,StartH+deltaH*0,bgW,deltaH,Color.GRAY);
			drawText("�÷֣�"+String.valueOf(mActivity.getScore())+"",canvas,screenW/2,StartH+deltaH*1,bgW*9/10,deltaH,Color.GRAY);
			drawText("��תָ����"+String.valueOf(mActivity.getXR()),canvas,screenW/2,StartH+deltaH*2,bgW,deltaH,Color.GRAY);
			drawText("�շ�ָ����"+String.valueOf(mActivity.getYR()),canvas,screenW/2,StartH+deltaH*3,bgW,deltaH,Color.GRAY);
			drawText("�෭ָ����"+String.valueOf(mActivity.getZR()),canvas,screenW/2,StartH+deltaH*4,bgW,deltaH,Color.GRAY);
			drawBitmap(btnReplay,screenW/4,screenH-btnH,btnW,btnH,canvas,null);
			drawBitmap(btnRanklist,screenW/4*3,screenH-btnH,btnW,btnH,canvas,null);
			drawBitmap(btnScore,screenW/2+Dx[Df],btnH+Dy[Df],btnW*2,btnH,canvas,null);
			break;
		}
	}
	
	/**
	 * ���Ʊ���ɫ
	 */
	private void drawBackGround(Canvas canvas){
		Paint paint=new Paint();
		paint.setColor(Color.rgb(255, 215, 50));
		canvas.drawRect(new Rect(0,0,screenW,screenH), paint);
	}
	
	/**
	 * ��д�����ͼ����
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		drawBackGround(canvas);
		refreshUI(canvas);
		super.onDraw(canvas);
	}

	/**
	 * ����ͼ��
	 * @param res
	 * @param src
	 * @param canvas
	 * @param paint
	 */
	private void drawBitmap(Bitmap res,int x,int y,int w,int h,Canvas canvas,Paint paint){
		canvas.drawBitmap(res, new Rect(0,0,res.getWidth(),res.getHeight()), new Rect(x-w/2,y-h/2,x+w/2,y+h/2), paint);
	}
	/**
	 * ��������
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
	 * ����ͼƬ��Դ
	 */
	public Bitmap getRes(int resID) {
		return BitmapFactory.decodeResource(getResources(),resID);
	}
	
	
	/**
	 * �����Ƿ�����
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
		//��ʼ
		if (screenW/4-btnW<=x && x<=screenW/4+btnW/2 && screenH-btnH-btnH/2<=y && y<=screenH-btnH/2){
			mActivity.buttonClicked();
			return true;
		}
		
		//����
		if (screenW/4*3-btnW<=x && x<=screenW/4*3+btnW/2 && screenH-btnH-btnH/2<=y && y<=screenH-btnH/2){
			mActivity.rankList();
			return true;
		}
		
		//����
		if (isInRect(x,y,screenW/2,screenH/4*3,btnCW,btnCH)){
			if (mActivity.getGameState()==0){
				mActivity.showShare("��������������Ϸ�������ߴ���ת��������ʹ��ʲô�ķ�������֮������ֻ�������ת����ȡ�ø߷ְɣ��������������Ŷ�� ���ص�ַ��http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk","http://androidapplication.qiniudn.com/DangerousGame/ic_launcher-web.png","http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk");
			}
			return true;
		}
		
		//��ҫ
		if (isInRect(x,y,screenW/2,btnH,btnW*2,btnH)){
			if (mActivity.getGameState()==4){
				mActivity.showShare("���ڱ��ߴ���ת�л����"+String.valueOf(mActivity.getScore())+"�֣���Ҳ��������԰ɣ� ���ص�ַ��http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk", "http://androidapplication.qiniudn.com/DangerousGame/ic_launcher-web.png","http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk");
			}
			return true;
		}
		
		//����
		if (mActivity.getGameState()==0 && isInRect(x,y,screenW/8,bgH/5*3+bgH/4,bgW/4,bgH/3)){
			mActivity.HelpDialog();
			return true;
		}
		
		//invalidate();
		return true;
	}

}
