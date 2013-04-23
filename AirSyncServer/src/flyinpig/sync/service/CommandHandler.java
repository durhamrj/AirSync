package flyinpig.sync.service;

import java.net.Socket;

public class CommandHandler extends Thread {

	Socket sock = null;
	
	public CommandHandler(Socket sock)
	{
		this.sock = sock;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
