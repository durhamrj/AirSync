package flyinpig.sync.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import flyinpig.sync.service.structures.CommandResponse;
import flyinpig.sync.service.structures.DeviceInfo;
import flyinpig.sync.service.structures.ParsingException;

public class CommandHandler extends Thread {

	private Socket sock = null;
	DeviceInfo devinfo = null;
	private DataInputStream dis = null;
	private DataOutputStream dos =  null;
	private boolean die = false;
	FileOutputStream pushFile = null;
	int pushId = -1;
	FileInputStream requestFile = null;
	int requestId = -1;
	String remoteWorkingDirectory = "/";
	
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
//				System.out.println("Reached normal read while loop");
				try{
					int messageLength = dis.readInt();
					byte[] message = new byte[messageLength];
					int numread;
					if( (numread = dis.read(message)) == messageLength) 
					{
						try{
							CommandResponse response = CommandExecutor.execute(new CommandResponse(message), this);
							if( response != null )
							{
								send(response);
							}
						}catch (ParsingException e){
							CommandResponse parsefailure = new CommandResponse(CommandResponse.COMMAND_TYPE_FAILURE);
							parsefailure.addParameter("Command parsing failure.");
							System.err.println(e.getMessage());
						}
					}else{
						System.out.println("Expected " + messageLength + " but only read " + numread);
					}
				}catch(EOFException e){
					Main.connectionLost(this,"Connection ended prematurely.");
					die = true;
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Main.deregister(this);
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
		if( dos == null )
		{
			try {
				dos = new DataOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				Main.connectionLost(this, "Connection lost.");
			}
		}
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

	public void disconnect() {
		die = true;
	}

}
