package info.fshi.lightweightdatamule.packet;

public abstract class BasicPacket {
	// BT messge header
	// format:  type|data
	public static final String PACKET_TYPE = "type";
	public static final String PACKET_DATA = "data";
	
	// data type identifier
//	public static final int PACKET_TYPE_TIMESTAMP_DATA = 100; // format 100|timestamp
//	public static final int PACKET_TYPE_TIMESTAMP_ACK = 101; // format 101|
	public static final int PACKET_TYPE_NEIGHBOR_TABLE = 200;
	public static final int PACKET_TYPE_NEIGHBOR_TABLE_ACK = 201;
	public static final int PACKET_TYPE_WIFI_CONTROL = 300;
//	public static final int PACKET_TYPE_WIFI_CONTROL = 
	
	public abstract void parse(String origin, String packet);
	public abstract String serialize();
	
}
