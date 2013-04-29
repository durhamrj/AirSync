package flyinpig.sync;

import flyinpig.sync.io.CommandResponse;

public class CommandExecutor {

	public static CommandResponse execute(CommandResponse commandResponse ) {
		RemoteSyncActivity.showErrorMessage("Received command: " + commandResponse);
		return null;
	}

}
