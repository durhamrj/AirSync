package flyinpig.sync.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import flyinpig.sync.service.structures.CommandResponse;

public class CommandExecutor {

	public static final int CHUNKSIZE = 4096;
	
	public static CommandResponse execute(CommandResponse commandResponse, CommandHandler device ) {
		boolean failure = false;
		boolean response = false;
		boolean endoffile = false;
		int type = commandResponse.getCommandType();
		
		if( type >= CommandResponse.COMMAND_TYPE_FAILURE ) // >= 1000
		{
			failure = true;
			type -= CommandResponse.COMMAND_TYPE_FAILURE;
		}
		
		if( type >= CommandResponse.COMMAND_TYPE_RESPONSE ) // >= 400
		{
			response = true;
			type -= CommandResponse.COMMAND_TYPE_RESPONSE;
		}
		
		if( type >= CommandResponse.COMMAND_TYPE_END_FILE ) // >= 50
		{
			endoffile = true;
			type -= CommandResponse.COMMAND_TYPE_END_FILE;
		}
		
		switch(type)
		{
		case CommandResponse.COMMAND_TYPE_QUIT:
			CommandResponse msg = new CommandResponse(CommandResponse.COMMAND_TYPE_QUIT + CommandResponse.COMMAND_TYPE_RESPONSE);
			msg.setCommandid(commandResponse.getCommandid());
			Main.connectionLost(device,"Received remote quit command");
			device.disconnect();
			return msg;
		case CommandResponse.COMMAND_TYPE_DIRLIST:
			
			if( failure )
			{
				System.out.println(commandResponse.getParameters().get(0));
			}
			else if( response )
			{
				// parse results and display in UI
				Main.remoteDirectoryListing(device,commandResponse);

			}else{
				List<String> params = commandResponse.getParameters();
				// PARAM [0] = root directory
				// PARAM [1] = directory specified
				if( params.size() == 0 )
				{
					CommandResponse rootdirmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_DIRLIST + CommandResponse.COMMAND_TYPE_RESPONSE);
					rootdirmsg.setCommandid(commandResponse.getCommandid());
					// get root directories
					File[] rootDirs = File.listRoots();
					for( File f : rootDirs )
					{
						if( f.isDirectory() ){
							rootdirmsg.addParameter(f.getPath() + File.pathSeparatorChar);
						}else{
							rootdirmsg.addParameter(f.getPath());
						}
					}
					return rootdirmsg;
				}else if( params.size() >= 1 ){ // ignore any parameters beyond 1
					CommandResponse dirmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_DIRLIST + CommandResponse.COMMAND_TYPE_RESPONSE);
					dirmsg.setCommandid(commandResponse.getCommandid());
					
					File dir = new File(params.get(0));
					if( !dir.exists() )
					{
						CommandResponse failmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
						failmsg.addParameter("Directory does not exist!");
						return failmsg;
					}
					
					if( !dir.isDirectory() )
					{
						CommandResponse failmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
						failmsg.addParameter("Not a directory!");
						return failmsg;
					}
					
					File [] listing = dir.listFiles();
					for( File f : listing )
					{
						if( f.isDirectory() ){
							dirmsg.addParameter(f.getPath() + File.pathSeparatorChar);
						}else{
							dirmsg.addParameter(f.getPath());
						}
					}
				}
			}
		case CommandResponse.COMMAND_TYPE_REQUEST_FILE:
			if( response )
			{
				System.err.println("Remote msg: " + commandResponse.getParameters().get(0));
				break;
			}
			
			String path = null;
			try{
				path = Main.getFullPath(commandResponse.getParameters().get(0));
			}catch(Exception e){
				CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_REQUEST_FILE + CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
				fail.addParameter(e.getMessage());
				fail.setCommandid(commandResponse.getCommandid());
				device.send(fail);
			}
			File upFile = new File(path);
			if( upFile.exists() && upFile.isFile() )
			{
				try{
					FileInputStream fis = new FileInputStream(upFile);
					byte[] buffer = new byte[4096];
					long size = upFile.length();
					long chunk_count = size / (long)CHUNKSIZE;
					long index = 0;
					long chunks = 0;
					
					
					while( index <= size )
					{
						int numread = fis.read(buffer);
						index += numread;
						chunks += 1;
						CommandResponse filepiece = new CommandResponse(CommandResponse.COMMAND_TYPE_FILE_PIECE,new String(buffer,0,numread));
						filepiece.setCommandid(commandResponse.getCommandid());
						if( index == size ) // if last piece of file
						{
							fis.close();
							filepiece.setCommandtype(CommandResponse.COMMAND_TYPE_FILE_PIECE + CommandResponse.COMMAND_TYPE_END_FILE);
						}
						device.send(filepiece);
					}
					
					if( chunks != chunk_count )
					{
						System.err.println("Error: Only " + chunks + " of " + chunk_count + " file pieces were sent");
					}
					
					fis.close();
					
				}catch( IOException e ){
					
					System.err.println(e.getMessage());
					CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_REQUEST_FILE + CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
					fail.addParameter(e.getMessage());
					fail.setCommandid(commandResponse.getCommandid());
					device.send(fail);
				}
			}
			break;
		case CommandResponse.COMMAND_TYPE_PUSH_FILE:
			if( response )
			{
				System.err.println(commandResponse.getParameters().get(0));
				break;
			}
			// validate request
			device.pushId = commandResponse.getCommandid();
			try {
				device.pushFile = Main.createNewFile(commandResponse.getParameters().get(0));
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_PUSH_FILE + CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
				fail.addParameter("Unable to create file.");
				fail.setCommandid(commandResponse.getCommandid());
				device.send(fail);
			}
			
			break;
		case CommandResponse.COMMAND_TYPE_FILE_PIECE:
			if( response )
			{
				System.err.println("Remote msg: " + commandResponse.getParameters().get(0));
				break;
			}
			
			if( device.pushId == commandResponse.getCommandid() )
			{
				try {
					device.pushFile.write(commandResponse.getParameters().get(0).getBytes());
					if( endoffile )
					{
						device.pushFile.close();
						device.pushFile = null;
						device.pushId = -1;
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
					CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_FILE_PIECE+ CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
					fail.addParameter("File piece not associated with any known transfer");
					fail.setCommandid(commandResponse.getCommandid());
					device.send(fail);
				}
				
			}else{
				System.err.println("Unrecognized file piece received");
				CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_FILE_PIECE+ CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
				fail.addParameter("File piece not associated with any known transfer");
				fail.setCommandid(commandResponse.getCommandid());
				device.send(fail);
			}
			break;
		default:
			CommandResponse failmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
			failmsg.addParameter("Invalid command!");
			return failmsg;
		}
		
		return null;  // return null if no response is required
	}

	public static synchronized void uploadFile(String uploadfile,CommandHandler device) {
		

    	File upFile = new File(uploadfile);
		int charindex = upFile.getName().lastIndexOf(File.separator);
		String shortname = upFile.getName().substring(charindex+1);
		if( upFile.exists() && upFile.isFile() )
		{
			try{
				FileInputStream fis = new FileInputStream(upFile);
				byte[] buffer = new byte[4096];
				CommandResponse command = new CommandResponse(CommandResponse.COMMAND_TYPE_PUSH_FILE,shortname);
				device.send(command);
				
				long size = upFile.length();
				
				long chunk_count = size / (long)CHUNKSIZE + 1;
				
				long index = 0;
				long chunks = 0;
				while( index <= size )
				{
					int numread = fis.read(buffer);
					index += numread;
					chunks += 1;
					CommandResponse filepiece = new CommandResponse(CommandResponse.COMMAND_TYPE_FILE_PIECE,new String(buffer,0,numread));
					filepiece.setCommandid(command.getCommandid());
					if( index == size ) // if last piece of file
					{
						fis.close();
						filepiece.setCommandtype(CommandResponse.COMMAND_TYPE_FILE_PIECE + CommandResponse.COMMAND_TYPE_END_FILE);
					}
					device.send(filepiece);
				}
				
				if( chunks != chunk_count )
				{
					System.err.println("Error: Only " + chunks + " of " + chunk_count + " file pieces were sent");
				}
				
				fis.close();
				
			}catch( IOException e){
				Main.connectionLost(device,e.getMessage());
			}
			return;
		}else{
			System.err.println("Unable to read file");
		}
		
	}

	public static void requestDirectoryListing(String path,CommandHandler device) {
		CommandResponse command = new CommandResponse(CommandResponse.COMMAND_TYPE_DIRLIST,path);
		device.send(command);
	}

}
