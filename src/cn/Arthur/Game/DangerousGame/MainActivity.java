package cn.Arthur.Game.DangerousGame;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.CloudCodeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.CellLocation;     
import android.telephony.PhoneStateListener;     
import android.telephony.TelephonyManager;     

public class MainActivity extends Activity implements SensorEventListener,
		OnClickListener {

	private SensorManager manager;
	private Sensor aSensor;
	private Sensor oSensor;
	private Sensor lSensor;
	private Vibrator vibrator;

	private TextView SensorInfo;
	private TextView GameInfo;
	private Button ReadyButton;
	private GameView view;
	private double OX, OY, OZ;
	private float XR=0, YR=0, ZR=0;
	private int Score=0;
	private String RankInfo="";
	private double LT = 0, MLT = 0;
	private double LX, LY, LZ;
	private Thread GameThread;
	private double AX,AY,AZ,AS;
	
	private boolean isVibrate,isSound;
	
	// 判断方法+1
	private int DegreeX[] = { 15, 45, 75, 105, 135, 165, 195, 215, 245, 275,
			305, 335 }, DegreeY[] = { -165, -135, -105, -75, -45, -15, 15, 45,
			75, 105, 135, 165 }, DegreeZ[] = { -15, -30, -45, -60, -75, -90,
			90, 75, 60, 45, 30, 15 };
	private int XT[] = new int[12], YT[] = new int[12], ZT[] = new int[12];
	private int LockTimes = 11;
	// 判断方法+2
	private ArrayList<Double> XL, YL, ZL;
	private int minDelta = 10;
	private int minDeltaX = 300, minDeltaY = 300, minDeltaZ = 150;
	boolean isRefreshSensor = false;
	// 判断方法
	private boolean X[] = new boolean[12], Y[] = new boolean[4],
			Z[] = new boolean[4];
	//判断方法+3
	private long oldTime;
	private double KX=80,KY=80,KZ=100;

	private boolean isReady;// 确认游戏准备完毕
	private boolean isStart;// 等待手机平稳，开始游戏
	private boolean isGoing;// 游戏是否进行中
	private boolean isEnd;// 游戏是否结束
	private int StopGameTime = 0;// 用于判断游戏结束
	private int DeadLine=100;
	
	/*云代码*/
	AsyncCustomEndpoints ace;
	
	private int ADTimes=0;
	
	/* 游戏存档 */
	public SharedPreferences GameSave;
	private String userName="New User";
	
	/*音效*/
	 SoundPool soundPool;  
	 HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	
	Long rtime=(long) 0;
	
	//public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private String IMEICODE="";

	private Handler GameHandler = new Handler();
	
	//Share
	final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		GameSave = getSharedPreferences("cn.Arthur.Game.DangerousGame", 0);
		view = new GameView(MainActivity.this);
		setContentView(view);
		initSound();
		initSensor();
		initRank();
		initAD();
		initShare();
		// initLayout();
		//soundPool.play(soundMap.get(1), 1, 1, 0, 0, 1);  
		runGameThread();
	}
	
	 protected void onDestroy() {
         SpotManager.getInstance(this)
                         .unregisterSceenReceiver();
         super.onDestroy();
	 }
	
	/**
	 * 初始化传感器
	 */
	private void initSensor() {
		manager = (SensorManager) getSystemService(SENSOR_SERVICE);
		aSensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		oSensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		lSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		manager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_GAME);
		manager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_GAME);
		manager.registerListener(this, lSensor,SensorManager.SENSOR_DELAY_GAME);
		vibrator=(Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		AX=AY=AZ=AS=OX = OY = OZ = LX = LY = LZ = 0;
		XL = new ArrayList<Double>();
		YL = new ArrayList<Double>();
		ZL = new ArrayList<Double>();
		
		
		isVibrate=GameSave.getBoolean("Vibrate",true);
		isSound=GameSave.getBoolean("Sound",true);
	}

	/**
	 * 初始化布局
	 */
	private void initLayout() {
		SensorInfo = (TextView) findViewById(R.id.SenorInfo);
		GameInfo = (TextView) findViewById(R.id.GameInfo);
		ReadyButton = (Button) findViewById(R.id.Ready);
		ReadyButton.setOnClickListener(this);
	}
	
	
	/**
	 * 初始化声音
	 */
	private void initSound(){
		soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 5);
		soundMap.put(1, soundPool.load(this, R.raw.goal1, 1));  
        soundMap.put(2, soundPool.load(this, R.raw.goal2, 1));  
        soundMap.put(3, soundPool.load(this, R.raw.goal3, 1)); 
	}
	/**
	 * 初始化广告
	 */
	private void initAD(){
		AdManager.getInstance(this).init("e0711f219289ab2b", "eac8ecff040fc624", false);
		AdManager.getInstance(this).setUserDataCollect(true);
		SpotManager.getInstance(this).loadSpotAds();
		SpotManager.getInstance(this).setSpotTimeout(5000); 
	}
	
	/**
	 * 初始化排行榜相关
	 */
	private void initRank(){
		userName=GameSave.getString("uName","New User");
		if (userName.equals("New User")) ChangeUserName();
		TelephonyManager telephonyManager= (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		IMEICODE=telephonyManager.getDeviceId();
		Bmob.initialize(this, "8ff9a052310dcbd86bbe80d28eac1d48");
	}

	/**
	 * 初始化分享
	 */
	private void initShare(){
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(MainActivity.this, "1102045064","gZAJpPRcE6NoVRCV");
		qZoneSsoHandler.addToSocialSDK();
		//mController.getConfig().setSsoHandler(new SinaSsoHandler());
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
	}
	
	/**
	 * 刷新传感器信息文本
	 */
	private void refreshSensorInfo() {
		SensorInfo.setText("姿态传感器：" + "\nX:"
				+ new DecimalFormat("000.00").format(OX) + "\nY:"
				+ new DecimalFormat("000.00").format(OY) + "\nZ:"
				+ new DecimalFormat("000.00").format(OZ) + "\n线性加速度传感器："
				+ "\nX:" + new DecimalFormat("000.00").format(LX) + "\nY:"
				+ new DecimalFormat("000.00").format(LY) + "\nZ:"
				+ new DecimalFormat("000.00").format(LZ) + "\nTotal:"
				+ new DecimalFormat("000.00").format(LT) + "\nMTotal:"
				+ new DecimalFormat("000.00").format(MLT));
	}

	/**
	 * 更新游戏信息
	 */
	private void refreshGameInfo() {
		// 游戏结束
		if (isEnd == true) {
			GameInfo.setText("手机完成旋转……" + "\n旋转：" + String.valueOf(XR) + "圈"
					+ "\n前空翻：" + String.valueOf(YR) + "圈" + "\n侧空翻："
					+ String.valueOf(ZR) + "圈");
		}

		// 手机正在旋转中，等待结束
		if (isGoing == true && isEnd == false) {
			GameInfo.setText("手机努力旋转中……" + "\n旋转：" + String.valueOf(XR) + "圈"
					+ "\n前空翻：" + String.valueOf(YR) + "圈" + "\n侧空翻："
					+ String.valueOf(ZR) + "圈" + "\n闲置时间："
					+ String.valueOf(StopGameTime * 10) + "毫秒");
		}

		// 手机已平稳，等待玩家抛出
		if (isStart == true && isGoing == false) {
			GameInfo.setText("请旋转手机……");
		}

		// 玩家已准备好，等待手机平稳
		if (isReady == true && isStart == false) {
			GameInfo.setText("请平放手机……");
		}

		// 等待玩家准备好游戏
		if (isReady == false) {
			GameInfo.setText("请玩家准备……");
		}
	}

	public int getGameState() {
		// 游戏结束
		if (isEnd == true) {
			return 4;
		}
		// 手机正在旋转中，等待结束
		if (isGoing == true && isEnd == false) {
			return 3;
		}
		// 手机已平稳，等待玩家抛出
		if (isStart == true && isGoing == false) {
			return 2;
		}
		// 玩家已准备好，等待手机平稳
		if (isReady == true && isStart == false) {
			return 1;
		}
		// 等待玩家准备好游戏
		if (isReady == false) {
			return 0;
		}
		return -1;
	}

	/**
	 * 获得游戏信息
	 */
	public int getXR(){return (int)XR;}
	public int getYR(){return (int)YR;}
	public int getZR(){return (int)ZR;}
	public String getRank(){return RankInfo;}
	public int getScore(){
		Score=((int)XR)*7+((int)ZR)*5+((int)YR)*10;
		return Score;
	}
	public int getRestTime(){
		return StopGameTime;
	}
	
	
	/**
	 * 获得排名信息
	 */
	private void getRankInfo(){
		if (IMEICODE.equals("")){
			Toast.makeText(MainActivity.this,"无法获得手机唯一识别码！无法上传成绩！", Toast.LENGTH_SHORT).show();
			RankInfo="无效识别码！";
			return ;
		}
		ace= new AsyncCustomEndpoints();
		JSONObject params = new JSONObject();
		try {
			params.put("IMEI", IMEICODE);
			params.put("NAME", userName);
			params.put("SCORE", getScore());
		} catch (JSONException e) {
			Toast.makeText(MainActivity.this,"出现异常错误！", Toast.LENGTH_SHORT).show();
			RankInfo="异常错误！";
			e.printStackTrace();
			return ;
		}
		
		ace.callEndpoint(MainActivity.this, "refreshScore", params, 
		    new CloudCodeListener() {
		            @Override
		            public void onSuccess(Object object) {
		                // TODO Auto-generated method stub
		            	RankInfo="您击败了全球"+object.toString()+"%的玩家！";
		            	Toast.makeText(MainActivity.this,"成绩上传成功！", Toast.LENGTH_SHORT).show();
		            }
		            @Override
		            public void onFailure(int code, String msg) {
		                // TODO Auto-generated method stub
		                RankInfo="网络连接异常！";
		                Toast.makeText(MainActivity.this,"网络连接异常！", Toast.LENGTH_SHORT).show();
		            }
		        });
	}
	
	/**
	 * 游戏结束处理
	 */
	Runnable GameEnd = new Runnable() {
		public void run() {
			solveAD();
			if (isVibrate)vibrator.vibrate(1000);
			getRankInfo();
		}
	};
	
	

	/**
	 * UI更新线程
	 */
	Runnable refreshUI = new Runnable() {
		public void run() {
			// refreshSensorInfo();
			// refreshGameInfo();
			view.ActiveView();
		}
	};

	/**
	 * 激活游戏
	 */
	private void activeGame() {
		// 手机正在旋转中，等待结束
		if (isGoing == true && isEnd == false) {
			//StopGameTime = 1000;
		}
		// 手机已平稳，等待玩家抛出
		if (isStart == true && isGoing == false) {
			isGoing = true;
			oldTime=System.currentTimeMillis();
			StopGameTime=1000;
		}
	}

	/**
	 * 获得传感器
	 */
	private void getSensor() {
		// 保证游戏精度
		if (isGoing == true && isEnd == false)
			rotateLogic();
		if (isStart == true && isGoing == false)
			rotateLogicStart();
	}

	/**
	 * 旋转逻辑
	 */
	private void rotateLogicOld1() {

		if (340 <= OX || OX <= 20)
			X[0] = true;
		if (70 <= OX && OX <= 110)
			X[1] = true;
		if (160 <= OX && OX <= 200)
			X[2] = true;
		if (250 <= OX && OX <= 290)
			X[3] = true;
		if (X[0] && X[1] && X[2] && X[3]) {
			activeGame();
			if (isGoing == true)
				XR++;
			X[0] = X[1] = X[2] = X[3] = false;
		}

		if (Math.abs(OY) <= 20)
			Y[0] = true;
		if (70 <= OY && OY <= 110)
			Y[1] = true;
		if (Math.abs(OY) >= 160)
			Y[2] = true;
		if (-110 <= OY && OY <= -70)
			Y[3] = true;
		if (Y[0] && Y[1] && Y[2] && Y[3]) {
			activeGame();
			if (isGoing == true)
				YR++;
			Y[0] = Y[1] = Y[2] = Y[3] = false;
		}

		if (Math.abs(OZ) <= 20)
			Z[0] = true;
		if (70 <= OZ && OZ <= 110)
			Z[1] = true;
		if (-110 <= OZ && OZ <= -70)
			Z[2] = true;
		if (Z[0] && Z[1] && Z[2]) {
			activeGame();
			if (isGoing == true)
				ZR++;
			Z[0] = Z[1] = Z[2] = Z[3] = false;
		}
	}

	private void rotateLogicOld2() {
		int sum = 0;
		// X
		for (int i = 0; i < DegreeX.length; i++) {
			if (DegreeX[i] - 15 <= OX && OX <= DegreeX[i] + 15)
				XT[i] = 1;
			sum += XT[i];
		}
		if (sum >= LockTimes) {
			activeGame();
			if (isGoing)
				XR++;
			for (int i = 0; i < DegreeX.length; i++)
				XT[i] = 0;
		}
		// Y
		sum = 0;
		for (int i = 0; i < DegreeY.length; i++) {
			if (DegreeY[i] - 15 <= OY && OY <= DegreeY[i] + 15)
				YT[i] = 1;
			sum += YT[i];
		}
		if (sum >= LockTimes) {
			activeGame();
			if (isGoing)
				YR++;
			for (int i = 0; i < DegreeY.length; i++)
				YT[i] = 0;
		}
		// Z
		sum = 0;
		for (int i = 0; i < DegreeZ.length; i++) {
			if (DegreeZ[i] - 15 <= OZ && OZ <= DegreeZ[i] + 15)
				ZT[i] = 1;
			sum += ZT[i];
		}
		if (sum >= LockTimes) {
			activeGame();
			if (isGoing)
				ZR++;
			for (int i = 0; i < DegreeZ.length; i++)
				ZT[i] = 0;
		}
	}

	private void rotateLogicStart() {
		if (isRefreshSensor == false)
			return;

		double min = 361, max = -361;
		double temp;

		min = 361;
		max = -361;
		for (int i = 0; i < XL.size(); i++) {
			min = Math.min(min, XL.get(i));
			max = Math.max(max, XL.get(i));
		}
		if (Math.abs(OZ)<=80 && Math.abs(OY)<=170)
		if (max - min > minDeltaX)
			for (int i = 0; i < XL.size(); i++) {
				if (Math.abs(XL.get(i) - OX) <= minDelta) {
					temp = LT;
					if (temp / 5 >= 1)
						activeGame();
					if (isGoing){
						if (isSound)soundPool.play(soundMap.get(1), 1, 1, 0, 0, 1);  
						XR +=  ((float)temp / (float)5);
					}
					XL.clear();
					isRefreshSensor = false;
					break;
				}
			}
		XL.add(OX);
		if (XL.size() > 1000)
			XL.clear();

		min = 361;
		max = -361;
		for (int i = 0; i < YL.size(); i++) {
			min = Math.min(min, YL.get(i));
			max = Math.max(max, YL.get(i));
		}
		if (Math.abs(OZ)<=80 && 10<=OX && OX<=350)
		if (max - min > minDeltaY)
			for (int i = 0; i < YL.size(); i++) {
				if (Math.abs(YL.get(i) - OY) <= minDelta) {
					temp = LT;
					if (temp / 5 >= 1)
						activeGame();
					if (isGoing){
						if (isSound)soundPool.play(soundMap.get(2), 1, 1, 0, 0, 1);  
						YR += ((float)temp / (float)5);
					}
					YL.clear();
					isRefreshSensor = false;
					break;
				}
			}
		YL.add(OY);
		if (YL.size() > 1000)
			YL.clear();

		min = 361;
		max = -361;
		for (int i = 0; i < ZL.size(); i++) {
			min = Math.min(min, ZL.get(i));
			max = Math.max(max, ZL.get(i));
		}
		if (Math.abs(OY)<=170 && 10<=OX && OX<=350)
		if (max - min > minDeltaZ)
			for (int i = 0; i < ZL.size(); i++) {
				if (Math.abs(ZL.get(i) - OZ) <= minDelta) {
					temp = LT;
					if (temp / 5 >= 1)
						activeGame();
					if (isGoing){
						if (isSound)soundPool.play(soundMap.get(3), 1, 1, 0, 0, 1);  
						ZR +=  ((float)temp / (float)5);
					}
					ZL.clear();
					isRefreshSensor = false;
					break;
				}
			}
		ZL.add(OZ);
		if (ZL.size() > 1000)
			ZL.clear();
	}
	
	
	private double getSS(double x){
		return 1;
	}
	
	private void rotateLogic(){
		if (isRefreshSensor==false) return ;
		long nowTime=System.currentTimeMillis();
		double deltaTime=((double)(nowTime-oldTime))/(double)1000;
		if (Math.abs(LX)>=1) {
			XR+=LX*deltaTime*deltaTime*KX*getSS(AS);
			if (isSound)soundPool.play(soundMap.get(1), 1, 1, 0, 0, 1);  
			isRefreshSensor=false;
		}
		if (Math.abs(LY)>=1) {
			YR+=LY*deltaTime*deltaTime*KY*getSS(AS);
			if (isSound)soundPool.play(soundMap.get(2), 1, 1, 0, 0, 1);  
			isRefreshSensor=false;
		}
		if (Math.abs(LZ)>=1) {
			ZR+=LZ*deltaTime*deltaTime*KZ*getSS(AS);
			if (isSound)soundPool.play(soundMap.get(3), 1, 1, 0, 0, 1);  
			isRefreshSensor=false;
		}
		oldTime=System.currentTimeMillis();
	}

	/**
	 * 游戏主逻辑
	 */
	private void GameLogic() {

		// 游戏结束
		if (isEnd == true) {

		}

		// 手机正在旋转中，等待结束
		if (isGoing == true && isEnd == false) {
			if (StopGameTime <=0) {
				isEnd = true;
				GameHandler.post(GameEnd);
			}
			if (StopGameTime>0)StopGameTime--;
		}

		// 手机已平稳，等待玩家抛出
		if (isStart == true && isGoing == false) {

		}

		// 玩家已准备好，等待手机平稳
		if (isReady == true && isStart == false) {
			if (Math.abs(OZ) <= 10 && Math.abs(OY) <= 10) {
				ZR = YR = XR = 0;
				Score=0;
				// X[0] = X[1] = X[2] = X[3] = false;
				// Y[0] = Y[1] = Y[2] = Y[3] = false;
				// Z[0] = Z[1] = Z[2] = Z[3] = false;
				XL.clear();
				YL.clear();
				ZL.clear();
				for (int i = 0; i < DegreeX.length; i++)
					XT[i] = 0;
				for (int i = 0; i < DegreeY.length; i++)
					YT[i] = 0;
				for (int i = 0; i < DegreeZ.length; i++)
					ZT[i] = 0;
				RankInfo="正在联网中……";
				StopGameTime=1000;
				isStart = true;
			}
		}

		// 等待玩家准备好游戏
		if (isReady == false) {

		}

	}

	/**
	 * 游戏线程
	 */
	private void runGameThread() {
		GameThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					GameLogic();
					getSensor();
					GameHandler.post(refreshUI);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		GameThread.start();
	}

	/**
	 * 游戏线程销毁
	 */
	public void destroyGameThread() {
		if (GameThread != null)
			GameThread.destroy();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			LX = Math.abs(event.values[0]);
			LY = Math.abs(event.values[1]);
			LZ = Math.abs(event.values[2]);
			LT = Math.sqrt((LX * LX + LY * LY + LZ * LZ)/2);
			MLT = Math.max(LT, MLT);
		}
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			OX = event.values[0];
			OY = event.values[1];
			OZ = event.values[2];
			isRefreshSensor = true;
		}
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			AX = Math.abs(event.values[0])>100?AX:Math.abs(event.values[0]);
			AY = Math.abs(event.values[1])>100?AY:Math.abs(event.values[1]);
			AZ = Math.abs(event.values[2])>100?AZ:Math.abs(event.values[2]);
			AS = Math.sqrt((AX * AX + AY * AY + AZ * AZ));
		}
	}

	@Override
	public void onClick(View v) {
		if (v == ReadyButton) {
			// 游戏结束
			if (isEnd == true) {
				isEnd = isReady = isGoing = isStart = false;
				ReadyButton.setText("准备");
				ReadyButton.setEnabled(true);
			}
			// 等待玩家准备好游戏
			if (isReady == false) {
				isReady = true;
				ReadyButton.setText("已准备");
				ReadyButton.setEnabled(false);
			}
		}
		
	}

	public void buttonClicked() {
		// 游戏结束
		if (isEnd == true) {
			isEnd = isReady = isGoing = isStart = false;
			isReady=true;
		}
		// 等待玩家准备好游戏
		if (isReady == false) {
			isReady = true;
		}
	}
	
	/**
	 * 排行榜
	 */
	public void rankList(){
		if (isEnd == true || isReady==false) {
        	if (Math.abs(rtime-System.currentTimeMillis())>=1000){
        		this.startActivity( new Intent(this,RankListActivity.class));
        		rtime=System.currentTimeMillis();
        	}
		}
	}
	
    //返回键
    public boolean onKeyDown(int keyCode,KeyEvent event){
    	if (getGameState()==3) return true;
		switch(keyCode){
			case KeyEvent.KEYCODE_BACK:
				ExitDialog();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
    //程序退出
    protected void onUserLeaveHint() {
        if (getGameState()==3){
        	Intent intent = new Intent(this,MainActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	this.startActivity(intent);
        }
        super.onUserLeaveHint();
    }
    
    //退出
    private void ExitDialog(){
		new AlertDialog.Builder(MainActivity.this)
		.setIcon(R.drawable.ic_launcher)
		.setTitle(this.getString(R.string.app_name))
    	//.setMessage("点击确认退出。")
    	.setPositiveButton("退出", new DialogInterface.OnClickListener() {              
    	    public void onClick(DialogInterface dialog, int which) {
    	    	finish();
    	    	dialog.dismiss();
    	    }
    	})
    	.setNegativeButton("返回", new DialogInterface.OnClickListener() {              
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	})
    	.show();
	}
    
    //退出
    public void HelpDialog(){
    	ImageView image=new ImageView(this);
    	image.setImageResource(R.drawable.help);
    	ScrollView scroll=new ScrollView(this);
    	LinearLayout layout=new LinearLayout(this);
    	scroll.addView(layout);
    	scroll.setFadingEdgeLength(0);
    	layout.addView(image);
		new AlertDialog.Builder(MainActivity.this)
		.setIcon(R.drawable.ic_launcher)
    	.setView(scroll)
    	.setPositiveButton("我知道了！", new DialogInterface.OnClickListener() {              
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	})
    	.show();
	}
    
    /**
     * 更改用户名
     */
    private void ChangeUserName(){
    	final EditText namet=new EditText(this);
    	namet.setText(userName);
    	new AlertDialog.Builder(MainActivity.this)
		.setIcon(R.drawable.ic_launcher)
		.setTitle("更改用户名")
    	.setView(namet)
    	.setPositiveButton("确定", new DialogInterface.OnClickListener() {              
    	    public void onClick(DialogInterface dialog, int which) {
    	    	userName=namet.getText().toString();
    	    	GameSave.edit().putString("uName", userName).commit();
    	    }
    	})
    	.setNegativeButton("取消", new DialogInterface.OnClickListener() {              
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	})
    	.show();
    }
    
    /**
     * 更改设置
     */
    private void ChangeSetting(){
    	final LinearLayout layout=new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	layout.setGravity(Gravity.CENTER);
    	final CheckBox soundBox=new CheckBox(this);
    	final CheckBox vibrateBox=new CheckBox(this);
    	if (isVibrate) vibrateBox.setChecked(true); else vibrateBox.setChecked(false);
    	if (isSound) soundBox.setChecked(true); else soundBox.setChecked(false);
    	vibrateBox.setText("震动");soundBox.setText("音效");
    	layout.addView(vibrateBox);
    	layout.addView(soundBox);
    	soundBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				isSound=isChecked;
				GameSave.edit().putBoolean("Sound", isSound).commit();
			}
        });
    	vibrateBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				isVibrate=isChecked;
				GameSave.edit().putBoolean("Vibrate", isVibrate).commit();
			}
        });
    	
    	new AlertDialog.Builder(MainActivity.this)
		.setIcon(R.drawable.ic_launcher)
		.setTitle("设置")
    	.setView(layout)
    	.setPositiveButton("确定", new DialogInterface.OnClickListener() {              
    	    public void onClick(DialogInterface dialog, int which) {
    	    	
    	    }
    	})
    	.show();
    }
    
    
    /**
     * 分享
     * @param Content
     * @param PicUrl
     * @param ReturnUrl
     */
    public void showShare(String Content,String PicUrl,String ReturnUrl){
    	mController.setShareContent(Content);
		// 设置分享图片, 参数2为图片的url地址
		mController.setShareMedia(new UMImage(MainActivity.this, ReturnUrl));
		mController.setAppWebSite(ReturnUrl);
		mController.openShare(MainActivity.this, false);
    }
    
    
    /**
     * 显示广告
     */
    public void solveAD(){
    	ADTimes++;
    	if (ADTimes>=3){
    		if(SpotManager.getInstance(this).checkLoadComplete()){
    			SpotManager.getInstance(this).showSpotAds(this);
    			ADTimes=0;
    		}
    	}
    }
    
	/**
	 * Menu Created
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	/**
	 * Menu Selected
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.ChangeUserName:
			ChangeUserName();
			break;
		case R.id.Share:
			showShare("超级好玩的体感游戏：《暴走大旋转》！不管使用什么的方法，总之让你的手机高速旋转起来取得高分吧！我在这里等着你哦！ 下载地址：http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk","http://androidapplication.qiniudn.com/DangerousGame/ic_launcher-web.png","http://androidapplication.qiniudn.com/DangerousGame/DangerousGame.apk");
			//finish();
			break;
		case R.id.Setting:
			ChangeSetting();
			break;
		case R.id.Help:
			HelpDialog();
			break;
		case R.id.Exit:
			finish();
			break;
		}
		return true;
	}

}


