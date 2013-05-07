package flyinpig.sync.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import flyinpig.sync.service.structures.CommandResponse;
import flyinpig.sync.service.structures.Config;

public class Main {
	
	public static LinkedList<CommandHandler> devices = new LinkedList<CommandHandler>();
	
	private static CommandHandler selectedDevice = null; 
	private static String currentPath = "";
	private static Config config = null;
//	private static boolean waitforresponse = false;
	
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
		Listener socklistener = new Listener(config.port);
		if( socklistener.listen() )
		{
			System.out.println("Listening for connections on " + config.port);
		}else{
			System.out.println("Failed to open connection on " + config.port);
		}
		socklistener.start();
		
		commandlineInterface();
	}
	
	private static void commandlineInterface() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); 
		boolean deviceSelected = false;
		
		try {
			String input = "";
			while( !input.equals("quit") )
			{
				System.out.print("Enter command: ");
				input = in.readLine().trim().toLowerCase();
				
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
							sb.append(File.separatorChar);
						}
						sb.append("\n");
					}
					System.out.println(sb.toString());
				}else if( input.startsWith("lcd ") ){
					String folder = input.substring(4);
					if( folder.startsWith("../") )
					{
						int index = currentPath.length();
						boolean failed = false;
						while( folder.startsWith("../") ){
							
							index = currentPath.lastIndexOf(File.separatorChar, index);
							if( index == -1 )
							{
								System.out.println("Path is outside of permissable folder.");
								failed = true;
								break;
							}else{
								currentPath = currentPath.substring(0,index);
								folder = folder.substring(folder.indexOf('/')+1);
							}
						}
						if( failed )
						{
							continue;
						}
						// pealed off all '../' and still within context of currentPath							
					}
					File dir = new File(config.rootpath + currentPath + folder);
					if( !dir.exists() ){
						dir = new File(config.rootpath + currentPath + File.separatorChar + folder);
					}
					if( !dir.exists() ){
						System.err.println("Folder does not exist.\n");
					}else{
						// dir exists and is our new currentPath
						String newpath = dir.getPath();
						if( !newpath.startsWith(config.rootpath) )
						{
							System.out.println("Path is outside of permissable folder.");
						}else{
							currentPath = newpath.substring(config.rootpath.length());
						}
					}
				}else if( input.equals("rls") ){
					if( !deviceSelected ){
						System.out.println("Select a device before using this command.\n");
						continue;
					}
					CommandExecutor.requestDirectoryListing(selectedDevice.remoteWorkingDirectory,selectedDevice);
				}else if( input.startsWith("rcd ") ){
					
					String folder = input.substring(4);
					if( folder.startsWith("../") )
					{
						int index = selectedDevice.remoteWorkingDirectory.length();
						boolean failed = false;
						while( folder.startsWith("../") ){
							
							index = selectedDevice.remoteWorkingDirectory.lastIndexOf(File.separatorChar, index);
							if( index == -1 )
							{
								System.out.println("Path is outside of permissable folder.");
								failed = true;
								break;
							}else{
								selectedDevice.remoteWorkingDirectory = selectedDevice.remoteWorkingDirectory.substring(0,index);
								folder = folder.substring(folder.indexOf('/')+1);
							}
						}
						if( failed )
						{
							continue;
						}
						// pealed off all '../' and still within context of currentPath							
					}
					
					selectedDevice.remoteWorkingDirectory += folder;
					if( !folder.endsWith("/") )
					{
						selectedDevice.remoteWorkingDirectory += '/';
					}
										
				}else if( input.startsWith("pull ")){
					String file = input.substring(5);
					
					CommandExecutor.requestFile(file, selectedDevice);
				}else if( input.startsWith("push ")){
					String file = input.substring(5);
					System.out.println("Pushing file " + getFullPath(file) );
					CommandExecutor.uploadFile(getFullPath(file), selectedDevice);
				}else{
					if( !input.equals("quit")){
						System.out.println("Unrecognized command.\n");
					}
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
			System.out.println("Registered new device connection: " + commandHandler.devinfo);
			devices.add(commandHandler);
		}
	}

	public static void deregister(CommandHandler commandHandler) 
	{
		if( !devices.remove(commandHandler) )
		{
			System.err.println("Failed to remove " + commandHandler);
		}else{
			System.out.println("Deregistered device connection: " + commandHandler.devinfo);
		}
	}

	public static void connectionLost(CommandHandler device, String string) {
		System.err.println(string);
		deregister(device);
	}

	public static void remoteDirectoryListing(CommandHandler device,CommandResponse directoryListing) {
		
		List<String> listing = directoryListing.getParameters();
		StringBuffer sb = new StringBuffer(device.remoteWorkingDirectory);
		sb.append("\n");
		sb.append(listing.size());
		sb.append(" files/directories\n\n");
		for( String file : directoryListing.getParameters() )
		{
			sb.append(file);
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}

	public static String getFullPath(String path) throws FileNotFoundException {
		File dir = new File(config.rootpath + path);
		if( !dir.exists() ){
			dir = new File(config.rootpath + File.separatorChar + path);
		}
		
		// dir exists and is our new currentPath
		String newpath = dir.getPath();
		if( !newpath.startsWith(config.rootpath) )
		{
			throw new FileNotFoundException("File does not exist or is not accessible.");
		}else{
			return newpath;
		}
	}

	public static FileOutputStream createNewFile(String path) throws FileNotFoundException {
		File newfile;
		if( path.startsWith("/") )
		{
			newfile = new File(config.rootpath + path);
		}else{
			newfile = new File(config.rootpath + File.separatorChar + path);
		}
		
		try{
			if( !newfile.createNewFile() )
			{
				throw new FileNotFoundException("Unable to create file: " + path);
			}
			
			return new FileOutputStream(newfile);
			
		}catch(IOException e){
			throw new FileNotFoundException(e.getLocalizedMessage());
		}
	}
}
