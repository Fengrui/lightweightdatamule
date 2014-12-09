package info.fshi.lightweightdatamule.network;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * register wifi broadcast receiver here and use a callback to send message to main activity
 * @author fshi
 *
 */
public class WifiController extends BroadcastReceiver {
	private WifiP2pManager mManager;
	private Channel mChannel;
	private Context mActivity;
	private PeerListListener mPeerListListener;
	private ArrayList<String> connectedWifiDevices;

	NetworkInfo networkInfo;
	private WifiCom mWifiHelper;

	private static final String TAG = "WifiController";

	public WifiController(Context mContext, PeerListListener listener){
		mWifiHelper = WifiCom.getObject();
		// do initialization here
		mActivity = mContext;
		mPeerListListener = listener;
		mManager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(mActivity, mActivity.getMainLooper(), null);
		connectedWifiDevices = new ArrayList<String>();
	}

	public boolean isConnected(String mac){
		for(String connectedDevice: connectedWifiDevices){
			if(mac.equalsIgnoreCase(connectedDevice)){
				return true;
			}
		}
		return false;
	}

	public void createGroup(){
		mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
					@Override
					public void onGroupInfoAvailable(final WifiP2pGroup wifiP2pGroup) {
						// Following removal necessary to not have the manager busy for other stuff, subsequently
					}
				});
			}
			@Override
			public void onFailure(int i) {
				Log.d(TAG, String.valueOf(i));
			}
		});
	}

	public void removeGroup(){
		mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.i("", "Removed");
			}

			@Override
			public void onFailure(int i) {
				Log.i("", "Failed " + i);
			}
		});
	}


	/**
	 * start a scan
	 */
	public void startWifiScan(){
		// start wifi manager and call the discoverpeers method
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "discovery success");
			}

			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "discovery fail");
			}
		});
	}

	public void stopWifiScan(){
		Log.d(TAG, "scanning stopped");
		mManager.stopPeerDiscovery(mChannel, null);
	}

	/**
	 * start WiFi server
	 */
	public void startWifiServer(){
		// start a sample wifi server instance to listen for incoming connections
		mWifiHelper.startServer();
	}

	/**
	 * connect to a device
	 * add necessary parameters here
	 */
	public void connectWifiServer(final String mac){
		// connect to a wifi server
		// call manager.connect method
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = mac;
		config.wps.setup = WpsInfo.PBC;
		Log.d(TAG, "connect to " + mac);
		mManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				//success logic
				Log.d(TAG, "connected");
				if(!connectedWifiDevices.contains(mac)){
					Log.d(TAG, "add a connected device " + mac);
					connectedWifiDevices.add(mac);
				}
			}

			@Override
			public void onFailure(int reason) {
				//failure logic
				Log.d(TAG, "failed " + String.valueOf(reason));
				/*
				 * reason 
				 * ERROR(Value: 0):Indicates that the operation failed due to an internal error.
				 * NO_SERVICE_REQUESTS(Value: 3): Indicates that the discoverServices(WifiP2pManager.Channel, 
				 * WifiP2pManager.ActionListener) failed because no service requests are added. Use 
				 * addServiceRequest(WifiP2pManager.Channel, WifiP2pServiceRequest, WifiP2pManager.ActionListener) to add a service request.
				 * BUSY (Value: 2):Indicates that the operation failed because the framework is busy and unable to service 
				 * the request
				 */
			}
		});

	}

	/**
	 * send sth to a WiFi device
	 */
	public void sendToWifiDevice(String mac, JSONObject data){
		mWifiHelper.sendData(mac, data);	
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			// Determine if Wifi P2P mode is enabled or not, alert
			// the Activity.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				Log.d(TAG, "Wifi p2p is is enabled");
			} else {
				Log.d(TAG, "Wifi p2p is is disabled");
			}

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			Log.d(TAG, "peer list change");
			mManager.requestPeers(mChannel, mPeerListListener);
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
			if(mManager == null){
				return;
			}
			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (networkInfo.isConnected()) {
				String message = "Received request to connect to another device.";
				Log.v(TAG, message);
				// we are connected with the other device, request connection
				// info to find group owner IP
				mManager.requestConnectionInfo(mChannel, mWifiHelper.getConnectionInfoListener());
			} else {
				Log.i(TAG, "Received a disconnect action.");
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
		}
	}

}
