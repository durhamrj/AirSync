package flyinpig.sync.service;

import flyinpig.sync.service.structures.CommandResponse;

public class CommandExecutor {

	
	public static CommandResponse execute(CommandResponse commandResponse ) {
		System.out.println("Received command: " + commandResponse);
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
			// TODO
			break;
		case CommandResponse.COMMAND_TYPE_DIRLIST:
			// TODO
			break;
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
