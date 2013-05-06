package flyinpig.sync.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import com.thoughtworks.xstream.XStream;

import flyinpig.sync.service.structures.Config;

public class Main {
	
	public static LinkedList<CommandHandler> devices = new LinkedList<CommandHandler>();
	
	private static CommandHandler selectedDevice = null; 
	private static String currentPath = "/";
	private static Config config = null;
	
	public static void main(String[] args)
	{
		
		XStream xstream = new XStream();
		xstream.alias("config", Config.class);
		File f = new File("config.xml");
		System.out.println(f.getAbsolutePath());
		try{
			config = (Config)xstream.fromXML(f);
		}catch( Exception e ){
			System.err.println("Unable to find configuration file.");
			return;
		}
		
		File rootfolder = new File(config.rootpath);
		if( !rootfolder.exists() )
		{
			if( !rootfolder.mkdir() )
			{
				System.err.println("Unable to find/create root folder.");
				return;
			}
			
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
		System.out.print("Enter command: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 
		boolean deviceSelected = false;
		
		try {
			String input = in.readLine().trim().toLowerCase();
			while( !input.equals("quit") )
			{
				if( input.equals("help"))
				{
					StringBuffer sb = new StringBuffer();
					sb.append("QUIT\t\t\t\tShutdown server and exit\n");
					sb.append("DEVICELIST\t\t\tList connected devices\n");
					sb.append("SELECT [deviceid]\t\tSelect a device to browse files\n");
					sb.append("LLS\t\t\t\tList local directory contents\n");
					sb.append("LCD\t\t\t\tChange local directory\n");
					sb.append("AFTER SELECTING A DEVICE:\n");
					sb.append("RLS\t\t\t\tList remote directory contents\n");
					sb.append("RCD\t\t\t\tChange remote directory\n");
					sb.append("PULL [remote file]\t\tRequest file from device\n");
					sb.append("PUSH [local file]\t\tPush file to device\n");

					System.out.println(sb.toString());
				}else if(input.equals("devicelist") ){
					if( devices.size() == 0 )
					{
						System.out.println("No connected devices.\n");
					}else{
						StringBuffer sb = new StringBuffer();
						for( CommandHandler device : devices )
						{
							sb.append("[ ");
							sb.append(devices.indexOf(device));
							sb.append(" ] = ");
							sb.append(device.devinfo);
							sb.append("\n");
						}
						System.out.println(sb.toString());
					}
				}else if( input.startsWith("select ") ){
					try{
						int selection =  Integer.parseInt(input.substring(7));
						selectedDevice = devices.get(selection);
						deviceSelected = true;
					}catch(NumberFormatException e){
						System.out.println(e.getMessage());
					}
				}else if( input.equals("lls") ){
					File dir = new File(config.rootpath+currentPath);
					StringBuffer sb = new StringBuffer(dir.getAbsolutePath());
					sb.append("\n");
					File[] listing = dir.listFiles();
					sb.append(listing.length);
					sb.append(" files/directories\n\n");
					for( File f : dir.listFiles() )
					{
						sb.append(f.getName());
						if( f.isDirectory() )
						{
							sb.append(File.pathSeparator);
						}
						sb.append("\n");
					}
					System.out.println(sb.toString());
				}else if( input.startsWith("lcd ") ){
					String folder = input.substring(5);
					if( folder.contains("../") )
					{
						// TODO
					}
					File dir = new File(config.rootpath + folder);
					if( !dir.exists() ){
						dir = new File(config.rootpath + File.pathSeparator + folder);
					}
					if( !dir.exists() ){
						System.err.println("Folder does not exist.\n");
					}else{
						// dir exists and is our new folder
					}
				}else if( input.equals("rls") ){
					// TODO
				}else if( input.equals("rcd") ){
					// TODO
				}else{
					System.out.println("Unrecognized command.\n");
				}
				
				System.out.print("Enter command: ");
				input = in.readLine().trim().toLowerCase();
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
