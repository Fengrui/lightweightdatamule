package info.fshi.lightweightdatamule.network;

import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Messenger;
/**
 * controller to manage interface of bluetooth
 * @author fshi
 *
 */
public class BTController {

	private BTCom mBTHelper;
	private Messenger mMessenger;

	public BTController(Handler btHandler){

		// init bt utility
		mBTHelper = BTCom.getObject();
		mMessenger = new Messenger(btHandler);
		mBTHelper.setCallback(mMessenger);
	}

	/**
	 * start or stop a scan service
	 * @param isStart
	 */
	public void startBTScan(boolean isStart, long duration){
		mBTHelper.startScan(isStart, duration);
	}

	/**
	 * start bluetooth server thread
	 */
	public void startBTServer(){
		mBTHelper.startServer();
	}
	
	/**
	 * stop bt server thread
	 */
	public void stopBTServer(){
		mBTHelper.stopServer();
	}

	/**
	 * connect to a device
	 * @param btDevice
	 */
	public void connectBTServer(BluetoothDevice btDevice, long timeout){
		mBTHelper.connect(btDevice, timeout);
	}

	/**
	 * send sth to a BT device
	 * @param String mac
	 * @param JSONObject data
	 */
	public void sendToBTDevice(String mac, JSONObject data){
		mBTHelper.send(mac, data);
	}
	/**
	 * send sth to a BT device
	 * @param String mac
	 * @param JSONObject data
	 */
	public void sendToBTDevice(String mac, String data){
		mBTHelper.send(mac, data);
	}

	
	
	/**
	 * stop connection
	 * @param mac
	 */
	public void stopConnection(String mac){
		mBTHelper.stopConnection(mac);
	}	
}
