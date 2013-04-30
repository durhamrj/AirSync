package flyinpig.sync;

import java.io.File;
import java.util.List;

import android.util.Log;
import flyinpig.sync.io.CommandResponse;

public class ACommandExecutor {
	
	CommandResponse lastCommand = null;

	public static CommandResponse execute(CommandResponse commandResponse ) {
		boolean failure = false;
		boolean response = false;		
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
		
		switch(type)
		{
		case CommandResponse.COMMAND_TYPE_QUIT:
			CommandResponse msg = new CommandResponse(CommandResponse.COMMAND_TYPE_QUIT + CommandResponse.COMMAND_TYPE_RESPONSE);
			msg.setCommandid(commandResponse.getCommandid());
			RemoteSyncActivity.connectionLost("");
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
				for( String result : commandResponse.getParameters() )
				{
					// TODO
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
				}
			}
		case CommandResponse.COMMAND_TYPE_REQUEST_FILE:
			// TODO
			break;
		case CommandResponse.COMMAND_TYPE_PUSH_FILE:
			// TODO
			break;
		case CommandResponse.COMMAND_TYPE_FILE_PIECE:
			// TODO
			break;
		default:
			CommandResponse failmsg = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
			failmsg.addParameter("Invalid command!");
			return failmsg;
		}
		
		return null;  // return null if no response is required
	}

}
