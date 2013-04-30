package flyinpig.sync;

import flyinpig.sync.io.ClientThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
	        		   
	        		   ((Button)v).setText("Disconnect");
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
     		   ((Button)v).setText("Establish Connection");
     	   }
        }

	}

}
