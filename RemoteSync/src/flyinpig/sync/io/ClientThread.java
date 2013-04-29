package flyinpig.sync.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import flyinpig.sync.CommandExecutor;
import flyinpig.sync.RemoteSyncActivity;

public class ClientThread extends Thread {

	private Socket mClientSocket = null;
	boolean stayConnected = false;
	String iphostname = null;
	Integer port = null;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	
	public ClientThread()
	{
		super();
		
	}
	
	public void run() {
		while( true )  // Sanity check
		{
			if( stayConnected == false )
			{
				return;
			}
						
			if(mClientSocket == null ){
				// initiate connection
				InetAddress serverAddr[];
				try {
					serverAddr = InetAddress.getAllByName(iphostname);
					
					if( serverAddr != null && serverAddr.length != 0){
		    			mClientSocket = new Socket(serverAddr[0], port);
		    		}
					
				} catch (UnknownHostException e1) {
					stayConnected = false;
					RemoteSyncActivity.connectionLost(e1.getMessage());
					return;
				}catch (IOException e) {
					stayConnected = false;
					RemoteSyncActivity.connectionLost(e.getMessage());
					return;
				}
	    		
				try {
					if( dos != null )
						dos.close();
					dos = new DataOutputStream(mClientSocket.getOutputStream());
					initInfo();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				
				// check for commands
				try {
					
					dis = new DataInputStream(mClientSocket.getInputStream());
				
					int messageLength = dis.readInt();
					byte[] message = new byte[messageLength];
					if( dis.read(message) == messageLength) 
					{
						CommandResponse response = CommandExecutor.execute(new CommandResponse(message));
						send(response);
					}
					
				} catch (IOException e) {
					stayConnected = false;
					RemoteSyncActivity.showErrorMessage(e.getMessage());
				} catch (ParsingException e){
					CommandResponse parsefailure = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
					parsefailure.addParameter("Command parsing failure.");
					RemoteSyncActivity.showErrorMessage(e.getMessage());
				}
			}
		}
	}
	
	private void initInfo() throws IOException
	{
		// Dump Some Device Configuration Info
		byte[] endline = "\n".getBytes();
		dos.writeChars("DEVICE INFO:\n");
		dos.writeChars(android.os.Build.BRAND);
		dos.write(endline);
		dos.writeChars(android.os.Build.DEVICE);
		dos.write(endline);
		dos.writeChars(android.os.Build.DISPLAY);
		dos.write(endline);
		dos.writeChars(android.os.Build.MANUFACTURER);
		dos.write(endline);
		dos.writeChars(android.os.Build.MODEL);
		dos.write(endline);
		dos.writeChars(android.os.Build.PRODUCT);
		dos.write(endline);
		dos.writeChars(android.os.Build.USER);
		dos.write(endline);
		
		dos.writeChars("NETWORK INFO:\n");
		Enumeration<NetworkInterface> netI =  NetworkInterface.getNetworkInterfaces();
		if( !netI.hasMoreElements())
		{
			dos.writeChars("0 Network Interfaces\n");
		}
		while( netI.hasMoreElements() )
		{
			NetworkInterface iface = netI.nextElement();
			if( !iface.isLoopback() && iface.isUp() )
			{
				dos.write(endline);
				dos.writeChars(iface.getDisplayName());
				dos.write(endline);
				Enumeration<InetAddress> addrs = iface.getInetAddresses();
				while( addrs.hasMoreElements() )
				{
					dos.writeChars(addrs.nextElement().getHostAddress());
				}
				dos.write(endline);
			}						
		}
		dos.write(endline);
		dos.flush();
	}
	
	public void send( CommandResponse cr )
	{
		byte[] message = cr.toByteArray();
		
		try {
			dos.writeInt(message.length);
			dos.write(message);
		} catch (IOException e) {
			
			// check connection status
			if( mClientSocket.isClosed() )
			{
				stayConnected = false;
				RemoteSyncActivity.connectionLost(e.getMessage());
			}else{
				RemoteSyncActivity.showErrorMessage(e.getMessage());
			}
		}
	}
	
	public void connect( String ip_hostname, int port )
    {
    	if( mClientSocket != null )
    	{
    		try {
				mClientSocket.close();
			} catch (IOException e) {
				// do nothing
			}
    	}
    	stayConnected = true;
    	iphostname = ip_hostname;
    	this.port = port;
    }
	
	public void disconnect()
	{
		stayConnected = false;
		if( mClientSocket != null )
		{
			try {
				mClientSocket.close();
				
			} catch (IOException e) {
				mClientSocket = null;
				return;
			}
		}
		mClientSocket = null;
		
	}

}
