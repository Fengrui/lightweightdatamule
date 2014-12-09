package info.fshi.lightweightdatamule.packet;

import info.fshi.lightweightdatamule.routing.BTStatus;
import info.fshi.lightweightdatamule.routing.Neighbor;
import info.fshi.lightweightdatamule.routing.WifiStatus;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NeighborTablePacket extends BasicPacket {

	private JSONArray data = null;
	private String originMac;
	private final String KEY_MAC = "mac";
	private final String KEY_WIFI_MAC = "wifimac";
	private final String KEY_BT = "bt";
	private final String KEY_WIFI = "wifi";
	private final String KEY_VALID_TILL = "expires";
	private final String KEY_HOP_COUNT = "hops";
	private final String KEY_NEXT_HOP = "nexthop";
	private final String KEY_BACKLOG_SIZE = "backlogsize";
	// format
	// JSONArray : [{mac}, {bton: T/F}, {wifion: T/F}, {battery: L/M/H}]
	//
//	private static final String TAG = "neighbor table packet";
	
	public NeighborTablePacket(){
		data = new JSONArray();
	}

	public NeighborTablePacket(ArrayList<Neighbor> neighbors){
		data = new JSONArray();
		for(Neighbor neighbor : neighbors){
			JSONObject json = new JSONObject();
			try {
				json.put(KEY_MAC, neighbor.getMAC());
				json.put(KEY_WIFI_MAC, neighbor.getWifiMac());
				json.put(KEY_BT, neighbor.getBTStatus().isOn);
				json.put(KEY_WIFI, neighbor.getWifiStatus().isOn);
				json.put(KEY_VALID_TILL, neighbor.getExpiration());
				json.put(KEY_HOP_COUNT, neighbor.getHopCount());
				json.put(KEY_NEXT_HOP, neighbor.getNextHop());
				json.put(KEY_BACKLOG_SIZE, neighbor.getBacklogSize());
				data.put(json);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<Neighbor> getNeighborTable(){
		ArrayList<Neighbor> neighborTable = new ArrayList<Neighbor>();
		if(data != null){
			for (int i = 0; i < data.length(); i++) {
				try {
					Neighbor neighbor = new Neighbor(originMac, data.getJSONObject(i).getString(KEY_MAC), data.getJSONObject(i).getString(KEY_WIFI_MAC));
					BTStatus btStatus = new BTStatus();
					btStatus.setOnState(data.getJSONObject(i).getBoolean(KEY_BT));
					WifiStatus wifiStatus = new WifiStatus();
					wifiStatus.setOnState(data.getJSONObject(i).getBoolean(KEY_WIFI));
					neighbor.setBTStatus(btStatus);
					neighbor.setWifiStatus(wifiStatus);
					neighbor.setExpiration(data.getJSONObject(i).getLong(KEY_VALID_TILL));
					neighbor.setHopCount(data.getJSONObject(i).getInt(KEY_HOP_COUNT));
					neighbor.setNextHop(data.getJSONObject(i).getString(KEY_NEXT_HOP));
					neighbor.setBacklogSize(data.getJSONObject(i).getInt(KEY_BACKLOG_SIZE));
					neighborTable.add(neighbor);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }  
		}
		
		return neighborTable; 
	}
	
	@Override
	public void parse(String mac, String packet) {
		// TODO Auto-generated method stub
		try {
			originMac = mac;
			data = new JSONArray(packet);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String serialize() {
		return data.toString();
	}

}
