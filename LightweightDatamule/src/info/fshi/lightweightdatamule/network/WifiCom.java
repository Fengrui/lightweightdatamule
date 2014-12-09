package info.fshi.lightweightdatamule.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONObject;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class WifiCom {


	private final String TAG = "WifiCom";

	// constants
	public final static int WIFI_CLIENT_CONNECT_FAILED = 20404;
	public final static int WIFI_CLIENT_CONNECTED = 20401;
	public final static int WIFI_SERVER_CONNECTED = 20402;
	public final static int WIFI_DATA = 20403;
	public final static String WIFI_DATA_CONTENT = "wifi_data"; // data received from another device
	public static final String WIFI_DEVICE_MAC = "wifi_device_mac";
	
	public static final int WHAT_WIFI_DISCOVERY_SUCCESS = 10000;
	
	private int serverPort = 9999;
			
	// all received messages are sent through this messenger to its parent
	Messenger mMessenger = null;

	private ArrayList<ConnectedThread> connections = new ArrayList<ConnectedThread>();

	// current connection state, only one server thread and one client thread
	private ServerThread mServerThread = null;

	private StringBuffer sbLock = new StringBuffer("WifiP2pService");

	private Handler timeoutHandler = new Handler();

	public static WifiCom _obj = null;

	// time control params
	private long mTimeout = 5000;

	// server/client status
	private boolean serverRunning = false;
	
	//WifiP2pConnectionInfo
	private WifiP2pInfo mWifiP2pInfo = null;
	
	private onConnectionInfoListener wifiP2pConnectionInfoListener = null;
	
	private WifiP2pDevice mWifiP2pDevice;
	
	public void updateThisDevice(WifiP2pDevice device){
		mWifiP2pDevice = device;
	}
	
	public WifiP2pDevice getThisDevice(){
		return mWifiP2pDevice;
	}
	
	/**
	 * Constructor. Prepares a new BT interface. 
	 */
	
	private WifiCom() {
		wifiP2pConnectionInfoListener = new onConnectionInfoListener();
		mWifiP2pDevice = new WifiP2pDevice();
	}

	/**
	 * singleton
	 * @return
	 */
	public static WifiCom getObject(){
		if(_obj == null){
			_obj = new WifiCom();
		}
		return _obj;
	}

	/**
	 * set wifi com timeout
	 * @param timeout
	 */
	public void setTimeout(long timeout){
		mTimeout = timeout;
	}

	public boolean setCallback(Messenger callback){
		if(mMessenger == null){
			mMessenger = callback;
			return true;
		}
		else{
			return false;
		}
	}
	//start a client to send data
	public synchronized void startClient(){
		
	}
	
	/**
	 * Start listening
	 * @return
	 */
	public synchronized void startServer(){
		stopServer();
		mServerThread = new ServerThread();             
		mServerThread.start();
	}

	/**
	 * stop listening
	 * @return
	 */
	public void stopServer(){
		if(serverRunning){
			mServerThread.cancel();
		}
	}

	class ServerThread extends Thread{
		private final ServerSocket mServerSocket;
		
		public ServerThread(){
			ServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = new ServerSocket(serverPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mServerSocket = tmp;
		}
		
        @Override
		public void run() {
        	Socket socket = null;
			serverRunning = true;
			// Keep listening until exception occurs or a socket is returned
			while (serverRunning) {
				Log.d(TAG, "Wifi server waiting for incoming connections");
				try {
					socket = mServerSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				// If a connection was accepted
				if (socket != null && socket.isConnected()) {
					// Do work to manage the connection (in a separate thread)
					Log.d(TAG, "Connected as a server");//manageConnectedSocket(socket);
					// start a new thread to handling data exchange
					connected(socket, null, false);
				}
			}
			if(serverRunning){
				try {
					mServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				serverRunning = false;
			}			
			return;
		}
        
        /** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				Log.d(TAG, "server thread is stopped");
				mServerSocket.close();
				serverRunning = false;
			} catch (IOException e) {
				e.printStackTrace(); 
			}
		}
	}
	
	/**
	 * Client thread to handle issued connection command
	 * @author
	 */
	private class ClientThread extends Thread {
		private final Socket mClientSocket;
		private final WifiP2pDevice mWifiDevice;
		private boolean clientConnected = false;
		private StringBuffer sb;

		public ClientThread(WifiP2pDevice device, StringBuffer sb) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mSocket is final
			Socket tmp = null;
			mWifiDevice = device;
			this.sb=sb;

			// Get a Socket to connect with the given Device
			try {
				tmp = new Socket();
			} catch (Exception e) { }
			mClientSocket = tmp;
		}

		public void run() {
			// timestamp before connection
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				// stop the connection after 5 seconds
				synchronized (sb){
					timeoutHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG, "post delay " + String.valueOf(clientConnected));
							if(!clientConnected){
								cancel();
								if(mMessenger != null){
									try {
										Message msg=Message.obtain();
										msg.what = WIFI_CLIENT_CONNECT_FAILED;
										// send necessary info to the handler
										Bundle b = new Bundle();
										b.putString(WIFI_DEVICE_MAC, mWifiDevice.deviceAddress);
										msg.setData(b);
										mMessenger.send(msg);
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}, mTimeout);
					
					if((mWifiP2pInfo == null) || (mWifiP2pInfo.groupOwnerAddress == null)){
						return;
					}
					String host = mWifiP2pInfo.groupOwnerAddress.getHostAddress();
					mClientSocket.connect(new InetSocketAddress(host, serverPort));
					
					clientConnected = true;
					
					Log.d(TAG, "Connected as a client");
					// Do work to manage the connection (in a separate thread)
					// start a new thread to handling data exchange
					connected(mClientSocket, host, true);
				}
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mClientSocket.close();
				} catch (IOException closeException) { 
					// TODO Auto-generated catch block
					closeException.printStackTrace();
				}
			}

			// Do work to manage the connection (in a separate thread)
			return;
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				Log.d(TAG, "client thread is stopped");
				mClientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Start the ConnectedThread to begin managing a wifi connection
	 * @param socket
	 * @param remote address(such as "127.0.0.1")
	 * @param iAmClient
	 */
	private synchronized void connected(Socket socket, String rAddress, boolean iAmClient) {
		// Start the thread to manage the connection and perform transmissions
		ConnectedThread newConn = new ConnectedThread(socket);
		newConn.start();
		connections.add(newConn);
			
		// Send the info of the connected device back to the UI Activity
		Message msg=Message.obtain();
		if(iAmClient){
			msg.what = WIFI_CLIENT_CONNECTED;
		}else{
			msg.what = WIFI_SERVER_CONNECTED;
		}
		// send necessary info to the handler
		Bundle b = new Bundle();
		b.putString(WIFI_DEVICE_MAC, rAddress);//In wifi, cannot get the MAC of connected devi5ce in a simple way. 
		msg.setData(b);
		try {
			if(mMessenger != null)
				mMessenger.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class ConnectedThread extends Thread {
		private final Socket mConnectedSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(Socket socket) {
			mConnectedSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = mConnectedSocket.getInputStream();
				tmpOut = mConnectedSocket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public String getMac(){
			if((mWifiP2pInfo == null) || (mWifiP2pInfo.groupOwnerAddress == null)){
				return null;
			}
			return mWifiP2pInfo.groupOwnerAddress.getHostAddress();
		}

		public void run() {
			Object buffer;

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					ObjectInputStream in = new ObjectInputStream(mmInStream);
					buffer = in.readObject();
					
					Log.d(TAG, "Received msg:" + buffer.toString());
					
					// Send the obtained bytes to the UI activity
					if(mMessenger != null)
					{
						try {
							// Send the obtained bytes to the UI Activity
							Message msg=Message.obtain();
							Bundle b = new Bundle();
							b.putString(WIFI_DATA_CONTENT, buffer.toString());
							b.putString(WIFI_DEVICE_MAC, this.getMac());
							msg.what = WIFI_DATA;
							msg.setData(b);
							mMessenger.send(msg);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					stopConnection(getMac());
					break;
					// stop the connected thread
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					stopConnection(getMac());
					break;
				}
			}
		}

		public void writeObject(JSONObject json) {
			try {
				Log.d(TAG, String.valueOf(mmOutStream));
				ObjectOutputStream out = new ObjectOutputStream(mmOutStream);
				out.writeObject(json.toString());
				
				Log.d(TAG, "Write msg:" + json.toString());
				
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "output stream error");
				e.printStackTrace();
				stopConnection(getMac());
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				Log.d(TAG, "connected thread " + getMac() + " is stopped");
				mmInStream.close();
				mmOutStream.close();
				mConnectedSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * WifiP2pConnectionInfoListener to get the WifiP2pInfo while the connection is established.
	 */
	private class onConnectionInfoListener implements ConnectionInfoListener {

		public onConnectionInfoListener(){
			Log.d(TAG, "onConnectionInfoListener Constructor");
		}
		
		@Override
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			// TODO Auto-generated method stub
			Log.d(TAG, "ConnectionInfoAvailable");
			mWifiP2pInfo = info;
			if(info.isGroupOwner){
				Log.d(TAG, "ConnectionInfoAvailable, is GO!");
			}
		}
	}
	
	public onConnectionInfoListener getConnectionInfoListener(){
		if(wifiP2pConnectionInfoListener == null){
			wifiP2pConnectionInfoListener = new onConnectionInfoListener();
		}
		return wifiP2pConnectionInfoListener;
	}
	
	public synchronized void sendData(String mac, JSONObject data) {
		// TODO Auto-generated method stub
		for(ConnectedThread connection : connections){
			if(connection.getMac().equals(mac)){
				connection.writeObject(data);
			}
		}
	}
	
	public synchronized void stopConnection(String mac){
		for(ConnectedThread connection : connections){
			if(connection.getMac().equals(mac)){
				connection.cancel();
				Log.d(TAG, "remove connection" + connection.getMac());
				connections.remove(connection);
				break;
			}
		}
		Log.d(TAG, String.valueOf(connections.size()));
	}
}
