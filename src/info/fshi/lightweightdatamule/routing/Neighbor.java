package info.fshi.lightweightdatamule.routing;

public class Neighbor {
	private WifiStatus wifiStatus;
	private BTStatus btStatus;
	private String MAC;
	private long expiration;
	private int hopCount;
	private String nextHop; // mac address of next hop
	private int backLogSize;
	private String wifiMac;
	
	public Neighbor(String mac, String nextHop, String wifiMac){
		this.MAC = mac;
		this.wifiMac = wifiMac;
		this.nextHop = nextHop;
	}
	
	/**
	 * time stamp when it is not valid
	 * @param time
	 */
	public void setExpiration(long time){
		this.expiration = time;
	}
	
	public long getExpiration(){
		return this.expiration;
	}
	
	public void setHopCount(int hops){
		this.hopCount = hops;
	}
	
	public int getHopCount(){
		return this.hopCount;
	}
	
	public void setNextHop(String nextHop){
		this.nextHop = nextHop;
	}
	
	public String getNextHop(){
		return this.nextHop;
	}
	
	public void setBacklogSize(int size){
		this.backLogSize = size;
	}
	
	public int getBacklogSize(){
		return this.backLogSize;
	}
	
	public boolean isExpired(){
		if(this.expiration < System.currentTimeMillis()){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void setWifiMac(String mac){
		this.wifiMac = mac;
	}
	
	public void setBTStatus(BTStatus btStatus){
		this.btStatus = btStatus;
	}
	
	public void setWifiStatus(WifiStatus wifiStatus){
		this.wifiStatus = wifiStatus;
	}
	
	public String getMAC(){
		return this.MAC;
	}
	
	public BTStatus getBTStatus(){
		return this.btStatus;
	}
	
	public String getWifiMac(){
		return this.wifiMac;
	}
	
	public WifiStatus getWifiStatus(){
		return this.wifiStatus;
	}
	
	public String toString(){
		return MAC;
		
	}

}
