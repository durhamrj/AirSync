package flyinpig.sync.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Thread {
	int port = 0;
	ServerSocket servsock = null;
	boolean die = false;
	
	public Listener ( int port )
	{
		this.port = port;
	}
	
	public boolean listen()
	{
		if( servsock != null )
		{
			if( !servsock.isClosed() )
			{
				try{
					servsock.close();
				} catch (IOException e) {
					System.err.println("Listener:" + e.getMessage());
					return false;
				}
			}
		}
		try {
			servsock = new ServerSocket(port);
			
		} catch (IOException e) {
			System.err.println("Listener:" + e.getMessage());
			return false;
		}
		
		return true;
	}

	@Override
	public void run() {
		
		if( servsock == null && listen() == false )
		{
			return;
		}
		
		while( !die )
		{
			Socket sock;
			try {
				sock = servsock.accept();
				CommandHandler tmp = new CommandHandler(sock);
				Main.register(tmp);
				tmp.start();
				
			} catch (IOException e) {
				System.err.println("Listener: " + e.getMessage());
				continue;
			}
		}
		
		try {
			servsock.close();
		} catch (IOException e) {
			System.err.println("Listener: " + e.getMessage());
		}
	}
	
	public void kill()
	{
		die = true;
	}
}
