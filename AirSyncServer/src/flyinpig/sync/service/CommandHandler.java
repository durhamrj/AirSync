package flyinpig.sync.service;

import java.net.Socket;

import flyinpig.sync.service.structures.DeviceInfo;

public class CommandHandler extends Thread {

	Socket sock = null;
	DeviceInfo devinfo = null;
	
	public CommandHandler(Socket sock)
	{
		this.sock = sock;
		devinfo = new DeviceInfo();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if( sock == null )
		{
			Main.deregister(this);
			return;
		}
	}

}
