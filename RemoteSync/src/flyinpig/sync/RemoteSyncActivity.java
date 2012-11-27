package flyinpig.sync;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RemoteSyncActivity extends Activity {
	
	ClientThread mClientThread;
	
	static RemoteSyncActivity _singleton = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        _singleton = this;
        
        Button connectButton = (Button)findViewById(R.id.btnConnect);
        
        connectButton.setOnClickListener(mGetListener);
        
        setContentView(R.layout.main);        
    }
    
    protected void connect( String ip_hostname, int port )
    {
    	try {
			Socket mClientSocket = new Socket(ip_hostname, port);
			
			mClientThread = new ClientThread(mClientSocket);
			
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
    }
    
    private OnClickListener mGetListener = new OnClickListener() {
        public void onClick(View v) {
           if( v == null )
           {
        	   return;
           }
           
           if( v.getId() == R.id.btnConnect )
           {
        	   if( ((Button)v).getText().equals("Connect") )
        	   {
	        	   // Try to connect to remote server
	        	   String ip_host = ((EditText)findViewById(R.id.editIPText)).getText().toString();
	        	   String sPort = ((EditText)findViewById(R.id.editPortText)).getText().toString();
	        	   
	        	   try
	        	   {
	        		   int nPort = Integer.parseInt(sPort);
	        		   
	        		   RemoteSyncActivity._singleton.connect(ip_host,nPort);
	        		   
	        		   ((Button)v).setText("Disconnect");
	        	   }
	        	   catch( NumberFormatException e )
	        	   {
	        		   //PopUpWindow
	        		   System.err.println("Port value must be a number!");
	        	   }
        	   }
        	   else if( ((Button)v).getText().equals("Disconnect") )
        	   {
        		   
        	   }
           }
        }
    };
}
