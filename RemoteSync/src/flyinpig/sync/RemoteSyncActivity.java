package flyinpig.sync;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import flyinpig.sync.io.ClientThread;

public class RemoteSyncActivity extends Activity implements OnClickListener {
	
	public static String tag = "RemoteSync";
	ClientThread mClientThread;
	Handler mHandler;
	String alertMessage;
	ListView localDirectoryView = null;
	TextView localPathLabel = null;
	ListView remoteDirectoryView = null;
	TextView remotePathLabel = null;
	
	static RemoteSyncActivity _singleton = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        _singleton = this;
        mHandler = new Handler();
      
        setContentView(R.layout.main);        
        
        Button connectButton = (Button)findViewById(R.id.btnConnect);
        connectButton.setOnClickListener(this);
        
        
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	Log.v(tag, "onSaveInstanceState Called...\n");
    }
    
    
    public static void showErrorMessage(String message){
    	Log.e(RemoteSyncActivity.tag,message);
    	_singleton.alertMessage = message;
    	_singleton.mHandler.post(new Runnable(){
    	    public void run(){
    	    	AlertDialog.Builder alert = new AlertDialog.Builder(_singleton);
    	    	alert.setMessage(_singleton.alertMessage);
    	    	AlertDialog dialog = alert.create();
    	    	if( dialog != null ){
    	    		dialog.show();
    	    	}
    	    }
    	});	
    }

	public static void connectionLost(String message) {
		if( message != null )
			showErrorMessage(message);
		Button btnConnect = (Button)_singleton.findViewById(R.id.btnConnect);
		if( btnConnect != null )
		{
			btnConnect.setText("Establish Connection");
		}
		_singleton.setContentView(R.layout.main);
	}

	public static void initializeLocalFileView() {
		_singleton.setContentView(R.layout.browse);
		Button disconnectButton = (Button)_singleton.findViewById(R.id.btnDisconnect);
	    disconnectButton.setOnClickListener(_singleton);
		_singleton.localDirectoryView = (ListView)_singleton.findViewById(R.id.directoryView);
		_singleton.localPathLabel = (TextView)_singleton.findViewById(R.id.pathLabel);
		_singleton.localPathLabel.setOnClickListener(_singleton);
		_singleton.populateLDV("/");
	}

	protected void populateLDV(String path) {
		
		File[] listing;
		if( path.equals("/") )
		{
			listing = File.listRoots();
			if( listing.length == 1 )
			{
				listing = listing[0].listFiles();
			}
			Log.v(tag, "Root Listing: " + listing.length);
		}else{
			listing = new File(path).listFiles();
		}
		localPathLabel.setText(path);
		localDirectoryView.setAdapter(new ArrayAdapter<File>(this,R.layout.simple,listing));
	}
	
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
	        	   String ip_host = ((EditText)findViewById(R.id.editIPText)).getText().toString();
	        	   String sPort = ((EditText)findViewById(R.id.editPortText)).getText().toString();
	        	   
	        	   try
	        	   {
	        		   int nPort = Integer.parseInt(sPort);
	        		   if ( mClientThread == null )
	        		   {
	        			   mClientThread = new ClientThread();
	        		   }
	        		   mClientThread.connect(ip_host,nPort);
	        		   mClientThread.start();
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
     		   if( mClientThread != null )
     		   {
     			   mClientThread.disconnect();
     		   }
     		   connectionLost(null);
     	   }
        }else if( v.getId() == R.id.btnDisconnect ){
        	
        	   if( mClientThread != null )
	  		   {
	  			   mClientThread.disconnect();
	  		   }
	  		   connectionLost(null);
        }else if( v.getId() == R.id.pathLabel ){
        	TextView path = (TextView)v;
        	String parent = new File(path.getText().toString()).getParent();
        	if( parent != null ){
        		populateLDV(parent);
        	}
        }else if( v instanceof TextView ){
        	TextView selectedText = (TextView)v;
        	String pathstr = selectedText.getText().toString();
        	File path = new File(pathstr);
        	if( path.isDirectory() ){
        		populateLDV(pathstr);
        	}else{
        		// TODO Add context menus
        		Log.v(tag,"caught click on directory listing");
        	}
        }

	}
    
}
