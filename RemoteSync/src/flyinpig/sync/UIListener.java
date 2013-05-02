package flyinpig.sync;

import java.io.File;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import flyinpig.sync.io.ClientThread;

public class UIListener implements OnClickListener {

	public void onClick(View v) {
		
		if( v == null )
        {
     	   return;
        }
        
		Log.v(RemoteSyncActivity.tag,"Onclick called with " + v.getId());
		
        if( v.getId() == R.id.btnConnect )
        {
        	Button but = (Button)v;
        	Log.v("RemoteSync", but.getText().toString());
     	   if( ((Button)v).getText().toString().equals("Establish Connection") )
     	   {
	        	   // Try to connect to remote server
	        	   String ip_host = ((EditText)RemoteSyncActivity._singleton.findViewById(R.id.editIPText)).getText().toString();
	        	   String sPort = ((EditText)RemoteSyncActivity._singleton.findViewById(R.id.editPortText)).getText().toString();
	        	   
	        	   try
	        	   {
	        		   int nPort = Integer.parseInt(sPort);
	        		   if ( RemoteSyncActivity._singleton.mClientThread == null )
	        		   {
	        			   RemoteSyncActivity._singleton.mClientThread = new ClientThread();
	        		   }
	        		   RemoteSyncActivity._singleton.mClientThread.connect(ip_host,nPort);
	        		   RemoteSyncActivity._singleton.mClientThread.start();
	        		   RemoteSyncActivity.initializeLocalFileView();
	        		   
	        		   
	        	   }
	        	   catch( NumberFormatException e )
	        	   {
	        		   //PopUpWindow
	        		   Log.e(RemoteSyncActivity.tag,e.getMessage());
	        	   }
     	   }
     	   else if( ((Button)v).getText().equals("Disconnect") )
     	   {
     		   if( RemoteSyncActivity._singleton.mClientThread != null )
     		   {
     			   RemoteSyncActivity._singleton.mClientThread.disconnect();
     		   }
     		   RemoteSyncActivity.connectionLost(null);
     	   }
        }else if( v.getId() == R.id.btnDisconnect ){
        	
        	   if( RemoteSyncActivity._singleton.mClientThread != null )
	  		   {
	  			   RemoteSyncActivity._singleton.mClientThread.disconnect();
	  		   }
	  		   RemoteSyncActivity.connectionLost(null);
        }else if( v.getId() == R.id.directoryView ){
        	TextView selectedText = (TextView)v;
        	String pathstr = selectedText.getText().toString();
        	File path = new File(pathstr);
        	if( path.isDirectory() ){
        		RemoteSyncActivity._singleton.populateLDV(pathstr);
        	}else{
        		// TODO Add context menus
        	}
        		
        }else if( v.getId() == R.id.pathLabel ){
        	TextView path = (TextView)v;
        	String parent = new File(path.getText().toString()).getParent();
        	if( parent != null ){
        		RemoteSyncActivity._singleton.populateLDV(parent);
        	}
        }

	}

}
