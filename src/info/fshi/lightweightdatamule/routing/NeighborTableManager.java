package info.fshi.lightweightdatamule.routing;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class NeighborTableManager extends BroadcastReceiver {

	private ArrayList<Neighbor> neighborTable; // current neighbor table
	private Neighbor mDevice; // my own device status

	public final static long neighborTimeout = 10000; // 60 seconds neighbor timeout

	private static final String TAG = "neighbor table manager";

	public NeighborTableManager(){
		super();
		neighborTable = new ArrayList<Neighbor>();
	}

	public Neighbor nextRelay(){
		int myBacklogSize = mDevice.getBacklogSize();
		
		int minBacklog = myBacklogSize;
		Neighbor nextRelay = null;
		for(Neighbor neighbor : neighborTable){
			if(neighbor.getExpiration() > System.currentTimeMillis()){
				if(minBacklog > neighbor.getBacklogSize()){
					minBacklog = neighbor.getBacklogSize();
					nextRelay = neighbor;
				}
			}else{
				Log.d(TAG, "neighbor outdated");
			}
		}
		if(nextRelay != null){
			Log.d(TAG, "my back log size " + String.valueOf(myBacklogSize));
			Log.d(TAG, "min neighbor back log size " + String.valueOf(minBacklog));
		}else{
			Log.d(TAG, "no next relay available");
		}
		return nextRelay;
	}

	public void setMyWifiMac(String mac){
		this.mDevice.setWifiMac(mac);
	}
	
	public void setMyDevice(Neighbor myDevice){
		this.mDevice = myDevice;
	}

	/**
	 * add a new or update an existing neighbor to neighbortable
	 * @param neighbor
	 */
	public void addOrUpdateNeighbor(Neighbor neighbor){
		// if my own device, return directly
		if(mDevice.getMAC().equalsIgnoreCase(neighbor.getMAC())){
			// my own device
			return;
		}
		int oldHopCount = neighbor.getHopCount();
		neighbor.setHopCount(oldHopCount + 1);
		for(Neighbor currentNeighbor : neighborTable){
			if(neighbor.getMAC().equalsIgnoreCase(currentNeighbor.getMAC())){
				// if already exists
				if(neighbor.getExpiration() > currentNeighbor.getExpiration()){
					// update
					currentNeighbor.setHopCount(neighbor.getHopCount());
					currentNeighbor.setExpiration(neighbor.getExpiration());
					Log.d(TAG, "update an old neighbor" + neighbor.toString());
					Log.d(TAG, "current neighbor count : " + this.neighborTable.size());
				}
				return;
			}
		}
		this.neighborTable.add(neighbor);
		Log.d(TAG, "add a new neighbor :" + neighbor.toString());
		Log.d(TAG, "current neighbor count : " + this.neighborTable.size());
	}

	public void addNeighborTable(ArrayList<Neighbor> neighborTable){
		for(Neighbor neighbor : neighborTable){
			addOrUpdateNeighbor(neighbor);
		}
	}

	public String getNextHopToDes(String mac){
		for(Neighbor neighbor: neighborTable){
			if(neighbor.getMAC().equals(mac)){
				return neighbor.getNextHop();
			}
		}
		return null;
	}

	/**
	 * get the entire neighbortable, together with my own device and other neighbors
	 * @return
	 */
	public ArrayList<Neighbor> getNeighborTable(){
		ArrayList<Neighbor> allNeighbors = new ArrayList<Neighbor>();
		mDevice.setExpiration(System.currentTimeMillis() + neighborTimeout); // timeout after neighborTimeout seconds
		allNeighbors.add(mDevice);
		for(int i = 0; i < neighborTable.size(); i++){
			if(neighborTable.get(i).getExpiration() > System.currentTimeMillis()){
				allNeighbors.add(neighborTable.get(i));
			}
		}
		return allNeighbors;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// bluetooth radio status receiver
		// wifi radio status receiver

		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Determine if Wifi P2P mode is enabled or not, alert
			// the Activity.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				if(mDevice != null)
					mDevice.getWifiStatus().setOnState(true);
				Toast.makeText(context, 
						"WiFi Direct STATE_ON", 
						Toast.LENGTH_SHORT).show();
			} else {
				if(mDevice != null)
					mDevice.getWifiStatus().setOnState(false);
				Toast.makeText(context, 
						"WiFi Direct STATE_OFF", 
						Toast.LENGTH_SHORT).show();			
			}
		}
	}
}
