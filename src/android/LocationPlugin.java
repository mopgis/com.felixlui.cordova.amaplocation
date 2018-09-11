package com.felixlui.cordova.amaplocation;

import java.util.Locale;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

public class LocationPlugin extends CordovaPlugin {

	private static final String ACTION_START_WACTCHLOCATION = "startwatchlocation";
    private static final String ACTION_STOP_WACTCHLOCATION = "stopwatchlocation";

	private AMapLocationClient watchLocationClient = null;

	private CallbackContext watchCallbackContext = null;
	private WatchLocation watchLocation = new WatchLocation();
	private Context context;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		context = this.cordova.getActivity().getApplicationContext();
		super.initialize(cordova, webView);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		if(ACTION_START_WACTCHLOCATION.equals(action.toLowerCase(Locale.CHINA))) {
			if(null == this.watchCallbackContext){
				this.watchCallbackContext = callbackContext;
				watchLocation.startWatchLocation(context);
			}
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return true;
        }else if(ACTION_STOP_WACTCHLOCATION.equals(action.toLowerCase(Locale.CHINA))) {
			if(null != this.watchCallbackContext){
				this.watchLocation.stopWatchLocation(context);
				this.watchCallbackContext = null;
			}
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(false);
            callbackContext.sendPluginResult(r);
            return true;
        }
		return false;
	}

	JSONObject jo = new JSONObject();

	public class WatchLocation implements AMapLocationListener {
		@Override
		public void onLocationChanged(AMapLocation amapLocation) {

			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				jo = assembleLocation(amapLocation);

				// Execute an asynchronous task
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						// callbackContext.success(jo);
						PluginResult r = new PluginResult(PluginResult.Status.OK, jo);
						r.setKeepCallback(true);
						watchCallbackContext.sendPluginResult(r);
					}
				});
			} else {
				watchCallbackContext.error(amapLocation.getErrorInfo());
			}
		}

		public void startWatchLocation(Context context) {
			if(null == watchLocationClient){
				watchLocationClient = new AMapLocationClient(context);
				AMapLocationClientOption locationOption = new AMapLocationClientOption();
				//低功耗   Battery_Saving,高精度   Hight_Accuracy GPS,Device_Sensors
				// 设置定位模式为高精度模式
				locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
				// 设置定位监听
				watchLocationClient.setLocationListener(this);
				// 设置定位参数
				watchLocationClient.setLocationOption(locationOption);
				// 启动定位
				watchLocationClient.startLocation();
			}
		}

		public void stopWatchLocation(Context context) {
			if(null != watchLocationClient ){
				watchLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
				watchLocationClient = null;
			}
		}
	}

	public JSONObject assembleLocation(AMapLocation amapLocation) {
		JSONObject locationObj = new JSONObject();

		// 获取位置信息
		Double latitude = amapLocation.getLatitude();
		Double longitude = amapLocation.getLongitude();
		boolean hasAccuracy = amapLocation.hasAccuracy();
		float accuracy = amapLocation.getAccuracy();
		String address = amapLocation.getAddress();
		String province = amapLocation.getProvince();
		String road = amapLocation.getRoad();
		// 速度
		float speed = amapLocation.getSpeed();
		// 角度
		float bearing = amapLocation.getBearing();
		// 星数
		int satellites = amapLocation.getExtras().getInt("satellites", 0);
		// 时间
		long time = amapLocation.getTime();

		try {
			locationObj.put("latitude", latitude);
			locationObj.put("longitude", longitude);
			locationObj.put("hasAccuracy", hasAccuracy);
			locationObj.put("accuracy", accuracy);
			locationObj.put("address", address);
			locationObj.put("province", province);
			locationObj.put("road", road);
			locationObj.put("speed", speed);
			locationObj.put("bearing", bearing);
			locationObj.put("satellites", satellites);
			locationObj.put("time", time);

		} catch (JSONException e) {
			locationObj = null;
			e.printStackTrace();
		}

		return locationObj;
	}
}
