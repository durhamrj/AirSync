package flyinpig.sync.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import flyinpig.sync.service.structures.CommandResponse;
import flyinpig.sync.service.structures.DeviceInfo;
import flyinpig.sync.service.structures.ParsingException;

public class CommandHandler extends Thread {

	Socket sock = null;
	DeviceInfo devinfo = null;
	DataInputStream dis = null;
	DataOutputStream dos =  null;
	boolean die = false;
	
	public CommandHandler(Socket sock)
	{
		this.sock = sock;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if( sock == null )
		{
			Main.deregister(this);
			return;
		}
		
		try {
			if( devinfo == null )
			{
				dis = new DataInputStream(sock.getInputStream());
				
				// get device info
				StringBuffer device_details = new StringBuffer();
				char charread;
				boolean endline_read = false;
				while( (charread = dis.readChar()) != '\n' || endline_read == false )
				{
					if( charread == '\n')
					{
						endline_read = true;
					}else{
						endline_read = false;
					}
					device_details.append(charread);
				}
				devinfo = new DeviceInfo(device_details.toString());
				System.out.println(devinfo.toString());
			}
			
			while( sock.isConnected() && die == false )
			{
				// continuously read new messages
				System.out.println("Reached normal read while loop");
				
				int messageLength = dis.readInt();
				byte[] message = new byte[messageLength];
				if( dis.read(message) == messageLength) 
				{
					try{
						CommandResponse response = CommandExecutor.execute(new CommandResponse(message));
						if( response != null )
						{
							send(response);
						}
					}catch (ParsingException e){
						CommandResponse parsefailure = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
						parsefailure.addParameter("Command parsing failure.");
						System.err.println(e.getMessage());
					}
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
			
		if( die == true )
		{
			if( sock != null )
			{
				try {
					sock.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}
	
	public void send( CommandResponse cr )
	{
		byte[] message = cr.toByteArray();
		
		try {
			dos.writeInt(message.length);
			dos.write(message);
		} catch (IOException e) {
			
			// check connection status
			if( sock.isClosed() )
			{
				die = true;
				System.err.println(e.getMessage());
				Main.deregister(this);
			}else{
				System.err.println(e.getMessage());
			}
		}
	}

}
