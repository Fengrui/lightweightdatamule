package info.fshi.lightweightdatamule;

import info.fshi.lightweightdatamule.utils.Constants;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View.OnClickListener;

public class BTDevice{
	short rssi;
	String name;
	int connState;
	public BluetoothDevice btRawDevice;
	OnClickListener btConnect;
	int retryCounter;
	private long retryBackoff;
	private long lastTry;
	
	private final static String TAG = "BT Device";
	
	/**
	 * init a self-defined bluetooth device using android bluetooth object
	 * @param device
	 */
	public BTDevice(BluetoothDevice device){
		this.btRawDevice = device;
		this.retryBackoff = 0;
		this.lastTry = 0;
		this.retryCounter = Constants.BT_CONN_MAX_RETRY;
	}
	
	public void decRetryCounter(){
		this.retryCounter--;
	}
		
	public void resetRetryCounter(){
		this.retryCounter = Constants.BT_CONN_MAX_RETRY;
	}
	
	/**
	 * check if the device is able to connect, aka pass the retry backoff time
	 * @return
	 */
	public boolean isConnectable(){
		if((System.currentTimeMillis() - this.lastTry) > retryBackoff){
			this.lastTry = System.currentTimeMillis();
			return true;
		}
		return false;
	}
	
	public void updateRetryBackoff(boolean backoff){
		if(backoff){
			if(retryBackoff == 0){
				Log.d(TAG, "init retry backoff time");
				retryBackoff = Constants.BT_RETRY_BACK_OFF;
			}else{
				Log.d(TAG, "increase retry backoff time");
				retryBackoff *= 2; // back off 2 folds
			}
		}else{
			// connect successful, reset to 0
			Log.d(TAG, "reset retry backoff time");
			retryBackoff = 0;
		}
	}
}
