package flyinpig.sync.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import flyinpig.sync.service.structures.CommandResponse;
import flyinpig.sync.service.structures.DeviceInfo;

public class CommandHandler extends Thread {

	Socket sock = null;
	DeviceInfo devinfo = null;
	DataInputStream dis = null;
	DataOutputStream dos =  null;
	boolean die = false;
	
	public CommandHandler(Socket sock)
	{
		this.sock = sock;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if( sock == null )
		{
			Main.deregister(this);
			return;
		}
		
		try {
			if( devinfo == null )
			{
				dis = new DataInputStream(sock.getInputStream());
				
				// get device info
				String device_details = dis.readUTF();
				devinfo = new DeviceInfo(device_details);
				System.out.println(devinfo.toString());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		while( sock.isConnected() && die == false )
		{
			// 
		}
		
		if( die == true )
		{
			if( sock != null )
			{
				try {
					sock.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}
	
	public void send( CommandResponse cr )
	{
		byte[] message = cr.toByteArray();
		
		try {
			dos.writeInt(message.length);
			dos.write(message);
		} catch (IOException e) {
			
			// check connection status
			if( sock.isClosed() )
			{
				die = true;
				System.err.println(e.getMessage());
				Main.deregister(this);
			}else{
				System.err.println(e.getMessage());
			}
		}
	}

}
