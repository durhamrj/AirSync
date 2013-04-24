package flyinpig.sync.service;

import java.util.LinkedList;

public class Main {
	
	public static LinkedList<CommandHandler> devices = new LinkedList<CommandHandler>();
	
	public static void main(String[] args)
	{
		
		return;
	}
	
	public static void register(CommandHandler commandHandler)
	{
		if( commandHandler != null )
		{
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
			// TODO update list in UI
		}
	}
}
