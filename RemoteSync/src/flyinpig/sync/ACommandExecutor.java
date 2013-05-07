package flyinpig.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.util.Log;
import flyinpig.sync.io.CommandResponse;

public class ACommandExecutor {
	
	public static final int CHUNKSIZE = 4096;
	static String uploadfile = null;

	static FileOutputStream pushFile = null;
	static int pushId = -1;
	static FileInputStream	requestFile = null;
	static int requestId = -1;
	
	public static CommandResponse execute(CommandResponse commandResponse ) {
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
			RemoteSyncActivity.connectionLost("Received remote quit command");
			RemoteSyncActivity._singleton.mClientThread.disconnect();
			return msg;
		case CommandResponse.COMMAND_TYPE_DIRLIST:
			
			if( failure )
			{
				RemoteSyncActivity.showErrorMessage(commandResponse.getParameters().get(0));
			}
			else if( response )
			{
				// parse results and display in UI
				RemoteSyncActivity._singleton.populateRDV(commandResponse.getParameters().toArray(new String[0]));
				for( String result : commandResponse.getParameters() )
				{
					Log.v(RemoteSyncActivity.tag,result);
				}
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
					return dirmsg;
				}
			}
			break;
		case CommandResponse.COMMAND_TYPE_REQUEST_FILE:
			if( response )
			{
				RemoteSyncActivity.showErrorMessage("Remote msg: " + commandResponse.getParameters().get(0));
				break;
			}
			
			File upFile = new File(uploadfile);
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
						RemoteSyncActivity._singleton.mClientThread.send(filepiece);
					}
					
					if( chunks != chunk_count )
					{
						RemoteSyncActivity.showErrorMessage("Error: Only " + chunks + " of " + chunk_count + " file pieces were sent");
					}
					
					fis.close();
					
				}catch( IOException e ){
					
					RemoteSyncActivity.showErrorMessage(e.getMessage());
					CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_REQUEST_FILE + CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
					fail.addParameter(e.getMessage());
					fail.setCommandid(commandResponse.getCommandid());
					RemoteSyncActivity._singleton.mClientThread.send(fail);
				}
			}
			break;
		case CommandResponse.COMMAND_TYPE_PUSH_FILE:
			if( response )
			{
				RemoteSyncActivity.showErrorMessage(commandResponse.getParameters().get(0));
				break;
			}
			
			pushId = commandResponse.getCommandid();
			try {
				pushFile = new FileOutputStream(commandResponse.getParameters().get(0));
			} catch (FileNotFoundException e) {
				RemoteSyncActivity.showErrorMessage(e.getMessage());
				CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_PUSH_FILE + CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
				fail.addParameter(e.getMessage());
				fail.setCommandid(commandResponse.getCommandid());
				RemoteSyncActivity._singleton.mClientThread.send(fail);
			}
			
			break;
		case CommandResponse.COMMAND_TYPE_FILE_PIECE:
			if( response )
			{
				RemoteSyncActivity.showErrorMessage("Remote msg: " + commandResponse.getParameters().get(0));
				break;
			}
			
			if( pushId == commandResponse.getCommandid() )
			{
				try {
					pushFile.write(commandResponse.getParameters().get(0).getBytes());
					if( endoffile )
					{
						pushFile.close();
						pushFile = null;
						pushId = -1;
					}
				} catch (Exception e) {
					RemoteSyncActivity.showErrorMessage(e.getMessage());
				}
			}else{
				RemoteSyncActivity.showErrorMessage("Unrecognized file piece received");
				CommandResponse fail = new CommandResponse(CommandResponse.COMMAND_TYPE_FILE_PIECE+ CommandResponse.COMMAND_TYPE_RESPONSE + CommandResponse.COMMAND_TYPE_FAILURE);
				fail.addParameter("File piece not associated with any known transfer");
				fail.setCommandid(commandResponse.getCommandid());
				RemoteSyncActivity._singleton.mClientThread.send(fail);
			}
			break;
		default:
			CommandResponse failmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
			failmsg.addParameter("Invalid command!");
			return failmsg;
		}
		
		return null;  // return null if no response is required
	}

	public static synchronized void uploadFile(String string) {
		
		uploadfile = string;
		
		new Thread()
		{
		    public void run() {
		    	File upFile = new File(uploadfile);
				int charindex = upFile.getName().lastIndexOf(File.separator);
				String shortname = upFile.getName().substring(charindex+1);
				if( upFile.exists() && upFile.isFile() )
				{
					try{
						FileInputStream fis = new FileInputStream(upFile);
						byte[] buffer = new byte[4096];
						CommandResponse command = new CommandResponse(CommandResponse.COMMAND_TYPE_PUSH_FILE,shortname);
						RemoteSyncActivity._singleton.mClientThread.send(command);
						
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
							RemoteSyncActivity._singleton.mClientThread.send(filepiece);
						}
						
						if( chunks != chunk_count )
						{
							RemoteSyncActivity.showErrorMessage("Error: Only " + chunks + " of " + chunk_count + " file pieces were sent");
						}
						
						fis.close();
						
					}catch( IOException e){
						RemoteSyncActivity.connectionLost(e.getMessage());
					}
					return;
				}else{
					RemoteSyncActivity.showErrorMessage("Unable to read file");
				}
		    }
		}.start();
	}

	public static void requestDirectoryListing(String path) {
		CommandResponse command = new CommandResponse(CommandResponse.COMMAND_TYPE_DIRLIST,path);
		RemoteSyncActivity._singleton.mClientThread.send(command);
	}

}
