package cn.Arthur.Game.DangerousGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.FindListener;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class RankListActivity extends ListActivity {

	/** Called when the activity is first created. */

	TextView Info;
	ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	Handler handler=new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.ranklist);
		Info = (TextView) findViewById(R.id.Info);
		Info.setText("正在联网获取中……");

		ArrayList<String> temp= new ArrayList<String>();
		temp.add("正在联网获取中……");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new ArrayList<String>());
		setListAdapter(adapter);
		
		handler.post(RankInfo);
		handler.post(RankList);
	}

	/**
	 * 排行榜信息
	 */
	public void getRankList() {
		BmobQuery<RankList> query = new BmobQuery<RankList>();
		query.order("-SCORE");
		query.setLimit(50);
		query.findObjects(this, new FindListener<RankList>() {
			@Override
			public void onSuccess(List<RankList> object) {
				mData.clear();
				int index=0;
				for (RankList x : object) {
					Map<String, Object> item = new HashMap<String, Object>();
					index++;
					item.put("Name","第"+String.valueOf(index)+"名："+x.getNAME());
					item.put("Score","游戏得分："+String.valueOf(x.getSCORE()));
					mData.add(item);
				}
				SimpleAdapter adapter = new SimpleAdapter(
						RankListActivity.this, mData,
						android.R.layout.simple_list_item_2, new String[] {
								"Name", "Score" }, new int[] {
								android.R.id.text1, android.R.id.text2 });
				setListAdapter(adapter);
			}

			@Override
			public void onError(int code, String msg) {
				handler.post(RankList);

			}
		});
	}
	Runnable RankList = new Runnable() {
		public void run() {
			getRankList();
		}
	};

	public void getRankInfo() {
		TelephonyManager telephonyManager= (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String Imei="";
		Imei=telephonyManager.getDeviceId();
		if (Imei.equals("")){
			Info.setText("无法获得IMEI");
			return ;
		}
		
		JSONObject params = new JSONObject();
		try {
			params.put("IMEI", Imei);
		} catch (JSONException e) {
			Info.setText("异常错误");
			e.printStackTrace();
			return ;
		}
		
		AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
		// 第一个参数是上下文对象，第二个参数是云端代码的方法名称，第三个参数是上传到云端代码的参数列表（JSONObject
		// cloudCodeParams），第四个参数是回调类
		ace.callEndpoint(RankListActivity.this, "getRank", params,
				new CloudCodeListener() {
					@Override
					public void onSuccess(Object object) {
						// TODO Auto-generated method stub
						String temp=object.toString();
						String ans="";
						for (int i=0;i<temp.length();i++){
							char x=temp.toCharArray()[i];
							if (x=='#'){
								ans+='\n';
							}else{
								ans+=x;
							}
						}
						Info.setText(ans);
					}

					@Override
					public void onFailure(int code, String msg) {
						// TODO Auto-generated method stub
						Info.setText("网络异常，重新获取中……");
						handler.post(RankInfo);
					}
				});
	}
	Runnable RankInfo = new Runnable() {
		public void run() {
			getRankInfo();
		}
	};


}