package flyinpig.sync.service;

import java.util.LinkedList;

public class Main {
	
	public static LinkedList<CommandHandler> devices = new LinkedList<CommandHandler>();
	
	public static void main(String[] args)
	{
		int port = 2600;
		
		Listener socklistener = new Listener(port);
		if( socklistener.listen() )
		{
			System.out.println("Listening for connections on " + port);
		}else{
			System.out.println("Failed to open connection on " + port);
		}
		socklistener.start();
		
		while( socklistener.isAlive() )
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}
	
	public static void register(CommandHandler commandHandler)
	{
		if( commandHandler != null )
		{
			System.out.println("Registered new device connection.");
			devices.add(commandHandler);
			// TODO update list in UI
		}
	}

	public static void deregister(CommandHandler commandHandler) 
	{
		// TODO Auto-generated method stub
		if( !devices.remove(commandHandler) )
		{
			System.err.println("Failed to remove " + commandHandler);
		}else{
			System.out.println("Deregistered device connection.");
			// TODO update list in UI
		}
	}
}
