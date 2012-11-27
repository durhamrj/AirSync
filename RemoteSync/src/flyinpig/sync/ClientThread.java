package flyinpig.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class ClientThread implements Runnable {

	private Socket mClientSocket;
	
	public ClientThread(Socket clientSocket)
	{
		super();
		
		mClientSocket = clientSocket;
	}
	
	public void run() {
		if( mClientSocket == null )  // Sanity check
		{
			return;
		}
		
		// Dump Some Device Configuration Info
		try {
			BufferedOutputStream bos = new BufferedOutputStream(mClientSocket.getOutputStream());
			bos.write("DEVICE INFO:\n".getBytes());
			bos.write(android.os.Build.BRAND.getBytes());
			bos.write(0x0D);
			bos.write(android.os.Build.DEVICE.getBytes());
			bos.write(0x0D);
			bos.write(android.os.Build.DISPLAY.getBytes());
			bos.write(0x0D);
			bos.write(android.os.Build.MANUFACTURER.getBytes());
			bos.write(0x0D);
			bos.write(android.os.Build.MODEL.getBytes());
			bos.write(0x0D);
			bos.write(android.os.Build.PRODUCT.getBytes());
			bos.write(0x0D);
			bos.write(android.os.Build.USER.getBytes());
			bos.write(0x0D);
			
			bos.write("NETWORK INFO:\n".getBytes());
			Enumeration<NetworkInterface> netI =  NetworkInterface.getNetworkInterfaces();
			if( !netI.hasMoreElements())
			{
				bos.write("0 Network Interfaces\n".getBytes());
			}
			while( netI.hasMoreElements() )
			{
				bos.write(netI.toString().getBytes());
				bos.write(0x0D);
			}
			bos.write(0x0A0D0A0D);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*  Will maintain communication thread running with server as long as
		 *   connection is open.
		 */
		BufferedInputStream bis;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		if( mClientSocket != null ){
			try {
				mClientSocket.close();
				
			} catch (IOException e) {
				mClientSocket = null;
				return;
			}
		}
		
	}

}
