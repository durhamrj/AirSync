package flyinpig.sync.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CommandResponse {

	// internal variables
	int length = 0;
	int commandid = 0;
	int commandtype = 0;
	List<String> parameters = null;
	int chksum = 0;
	
	// COMMAND TYPES - RESPONSES to and command will be 
	public final static int COMMAND_TYPE_RESPONSE = 400;
	public final static int COMMAND_TYPE_FAILURE = 1000;
	public final static int COMMAND_TYPE_QUIT = 0;
	public final static int COMMAND_TYPE_DIRLIST = 1;
	public final static int COMMAND_TYPE_REQUEST_FILE = 2;
	public final static int COMMAND_TYPE_PUSH_FILE = 3;
	public final static int COMMAND_TYPE_FILE_PIECE = 4;
	public final static int COMMAND_TYPE_END_FILE = 50;
	
	public CommandResponse(int command_type)
	{
		commandid = (int)System.currentTimeMillis();
		commandtype = command_type;
		parameters = new ArrayList<String>();
	}
	
	public CommandResponse(int command_type,String parameter)
	{
		this(command_type);
		if( parameter != null )
		{
			parameters.add(parameter);
			length += parameter.length();
		}
	}
	
	public CommandResponse(byte[] raw_message) throws ParsingException
	{
		if( raw_message == null )
		{
			throw new ParsingException("Null message received.");
		}
		
		// check checksum
		for( int i = 0; i < raw_message.length-4; i++ )
		{
			chksum += raw_message[i]; 
		}
		System.out.println("Checksum value = " + chksum);
		
		ByteBuffer buf = ByteBuffer.wrap(raw_message);
		length = buf.getInt(0);
		commandid = buf.getInt(4);
		commandtype = buf.getInt(8);
		parameters = new ArrayList<String>();
	
		int paramslength = 0;
		int index = 12;
		while( paramslength < length )
		{
			int paramlength = buf.getInt(index);
			System.out.println("Start index = " + index);
			System.out.println("Paramlength = " + paramlength);
			index+= 4;
			String str = new String(raw_message, index, paramlength);
			parameters.add(str);
			System.out.println("[" + parameters.size() + "] - " + str );
			paramslength += paramlength;
			index += paramlength;
		}
		
		int read_chksum = buf.getInt(index);
		System.out.println(index);
		System.out.println("Read checksum value = " + read_chksum);
		if( read_chksum != chksum )
		{
			throw new ParsingException("Checksums did not match");
		}
	}
	
	public void addParameter(String parameter)
	{
		if(parameter != null)
		{
			parameters.add(parameter);
			length += parameter.length();
		}
	}

	
	public byte[] toByteArray()
	{
		// 24 is the combined size of the header values and checksum
		int command_size = 16 + length + (4 * parameters.size());
		System.out.println( "Allocating byte array of size: " + command_size );
		ByteBuffer buf = ByteBuffer.allocate(command_size);
		byte[] raw_message = null;
		
		// put header info in bytebuffer
		buf.putInt(length);
		buf.putInt(commandid);
		buf.putInt(commandtype);
		
		for( String param : parameters )
		{
			buf.putInt(param.length());
			buf.put(param.getBytes());
		}
		
		//put empty int place holder for checksum
		raw_message = buf.array();
		
		// calculate checksum and insert into raw_message
		for( int i = 0; i < raw_message.length-4; i++ )
		{
			chksum += raw_message[i]; 
		}
		System.out.println("Checksum value = " + chksum);
		buf.putInt(chksum);
		raw_message = buf.array();
		
		return raw_message;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof CommandResponse )
		{
		
			CommandResponse cr = (CommandResponse)obj;
			if( this.commandtype == cr.commandtype )
			{
				if( parameters.size() != cr.parameters.size() )
					return false;
				
				for ( int i = 0; i < parameters.size(); i++ )
				{
					if( !this.parameters.get(i).equals(cr.parameters.get(i)) )
					{
						return false;
					}
				}
			}else{
				return false;
			}
		}else{
			return false;
		}

		return true;
	}
	
	public int getCommandType() {
		return commandtype;
	}
	
	public int getCommandid() {
		return commandid;
	}

	public void setCommandid(int commandid) {
		this.commandid = commandid;
	}

	public List<String> getParameters() {
		return parameters;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer("ID: ");
		sb.append(commandid);
		sb.append("\nTYPE: ");
		sb.append(commandtype);
		sb.append("\n");
		for( int i = 0; i < parameters.size(); i++ )
		{
			sb.append("param[");
			sb.append(i);
			sb.append("] = ");
			sb.append(parameters.get(i));
			sb.append("\n");
		}
		return null;
	}


	public void setCommandtype(int type) {
		commandtype = type;
	}
	
	public static void main(String[] args)
	{
		String test1 = "Test1llllalksdfjlaj;sdflkjajsdlkfjlsajd;fljslkajdfjs;lkajfl;saj;jlfsdlkjasjdflksjlkfs";
		String test2 = "Test2a;lkdjf;lasjfdlkjsajfsld;jfalksjsdjflk;ajsdlkfjsa;klsfds;aljkflsajdlkjfdsjklsdjf";
		String test3 = "Test3This is a test from the emergency broadcast association";
/*		String test1 = "t";
		String test2 = "e";
		String test3 = "st";*/
		
		CommandResponse test = new CommandResponse(CommandResponse.COMMAND_TYPE_DIRLIST, test1);
		test.addParameter(test2);
		test.addParameter(test3);
		
		byte[] raw_message = test.toByteArray();
		CommandResponse result;
		try {
			result = new CommandResponse(raw_message);
			
			if( test.equals(result) )
			{
				System.out.println("SUCCESS!!!");
			}else{
				System.out.println("FAIL!!!!");
			}
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
