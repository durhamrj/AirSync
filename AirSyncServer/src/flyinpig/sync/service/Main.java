package flyinpig.sync.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import com.thoughtworks.xstream.XStream;

import flyinpig.sync.service.structures.Config;

public class Main {
	
	public static LinkedList<CommandHandler> devices = new LinkedList<CommandHandler>();
	
	public static void main(String[] args)
	{
		
		XStream xstream = new XStream();
		xstream.alias("config", Config.class);
		Config config = null;
		try{
			config = (Config)xstream.fromXML("config.xml");
		}catch( Exception e ){
			System.err.println("Unable to find configuration file.");
			return;
		}
//		Listener socklistener = new Listener(config.port);
//		if( socklistener.listen() )
//		{
//			System.out.println("Listening for connections on " + config.port);
//		}else{
//			System.out.println("Failed to open connection on " + config.port);
//		}
//		socklistener.start();
		
		commandlineInterface();
	}
	
	private static void commandlineInterface() {
		// TODO Auto-generated method stub
		System.out.println("Enter command: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 
		boolean deviceSelected = false;
		
		try {
			String input = in.readLine().trim().toLowerCase();
			while( input != "quit")
			{
				switch(input)
				{
				case "help":
					StringBuffer sb = new StringBuffer();
					if( deviceSelected )
					{
						
					}else{
						
					}
					
					break;
				default:
					System.out.println("Unrecognized command.\n");
				}
			}
			
		} catch (IOException e) {
			// should never happen with System.in
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
