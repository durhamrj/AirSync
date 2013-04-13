package flyinpig.sync.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

import flyinpig.sync.RemoteSyncActivity;

public class ClientThread extends Thread {

	private Socket mClientSocket = null;
	boolean stayConnected = false;
	String iphostname = null;
	Integer port = null;
	BufferedOutputStream bos = null;
	BufferedInputStream bis = null;
	
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
					if( bos != null )
						bos.close();
					bos = new BufferedOutputStream(mClientSocket.getOutputStream());
					initInfo(bos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				
				// check for commands
				try {
					bis = new BufferedInputStream(mClientSocket.getInputStream());
				
					int numread = 0;
					byte[] buf = new byte[4096];
					while( mClientSocket.isConnected() ) 
					{
						numread = bis.read(buf);
						System.out.print(new String(buf,0,numread));
					}
				} catch (IOException e) {
					stayConnected = false;
					RemoteSyncActivity.showErrorMessage(e.getMessage());
				}
			}
		}
		
		
		
				
		

		
	}
	
	private void initInfo(OutputStream out) throws IOException
	{
		// Dump Some Device Configuration Info
		byte[] endline = "\n".getBytes();
		out.write("DEVICE INFO:\n".getBytes());
		out.write(android.os.Build.BRAND.getBytes());
		out.write(endline);
		out.write(android.os.Build.DEVICE.getBytes());
		out.write(endline);
		out.write(android.os.Build.DISPLAY.getBytes());
		out.write(endline);
		out.write(android.os.Build.MANUFACTURER.getBytes());
		out.write(endline);
		out.write(android.os.Build.MODEL.getBytes());
		out.write(endline);
		out.write(android.os.Build.PRODUCT.getBytes());
		out.write(endline);
		out.write(android.os.Build.USER.getBytes());
		out.write(endline);
		
		out.write("NETWORK INFO:\n".getBytes());
		Enumeration<NetworkInterface> netI =  NetworkInterface.getNetworkInterfaces();
		if( !netI.hasMoreElements())
		{
			out.write("0 Network Interfaces\n".getBytes());
		}
		while( netI.hasMoreElements() )
		{
			NetworkInterface iface = netI.nextElement();
			if( !iface.isLoopback() && iface.isUp() )
			{
				out.write(endline);
				out.write(iface.getDisplayName().getBytes());
				out.write(endline);
				Enumeration<InetAddress> addrs = iface.getInetAddresses();
				while( addrs.hasMoreElements() )
				{
					out.write(addrs.nextElement().getHostAddress().getBytes());
				}
				out.write(endline);
			}						
		}
		out.write(endline);
		out.flush();
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
		if( mClientSocket != null )
		{
			try {
				mClientSocket.close();
				
			} catch (IOException e) {
				mClientSocket = null;
				return;
			}
		}
		
	}

}
