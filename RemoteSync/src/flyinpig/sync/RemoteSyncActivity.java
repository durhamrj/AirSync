package flyinpig.sync;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	TextView lastClickedItem = null;
	boolean lastClickedIsLocal = true; 
	ArrayAdapter<String> adapter = null;
	
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
		_singleton.runOnUiThread(new Runnable() {
			
	    public void run() {
	    	_singleton.setContentView(R.layout.main);
	            }
	    });
		
	}

	public static void initializeLocalFileView() {
		_singleton.setContentView(R.layout.browse);
		Button disconnectButton = (Button)_singleton.findViewById(R.id.btnDisconnect);
	    disconnectButton.setOnClickListener(_singleton);
	    Button viewServerButton = (Button)_singleton.findViewById(R.id.btnViewServ);
	    viewServerButton.setOnClickListener(_singleton);
		_singleton.localDirectoryView = (ListView)_singleton.findViewById(R.id.directoryView);
		_singleton.localPathLabel = (TextView)_singleton.findViewById(R.id.pathLabel);
		_singleton.localPathLabel.setOnClickListener(_singleton);
		_singleton.populateLDV("/");
	}
	
	public static void initializeRemoteFileView() {
		_singleton.setContentView(R.layout.remotebrowse);
		Button disconnectButton = (Button)_singleton.findViewById(R.id.btnDisconnect);
	    disconnectButton.setOnClickListener(_singleton);
	    Button viewLocalButton = (Button)_singleton.findViewById(R.id.btnViewLocal);
	    viewLocalButton.setOnClickListener(_singleton);
		_singleton.remoteDirectoryView = (ListView)_singleton.findViewById(R.id.remotedirectoryView);
		_singleton.remotePathLabel = (TextView)_singleton.findViewById(R.id.remotepathLabel);
		_singleton.remotePathLabel.setOnClickListener(_singleton);
		_singleton.remotePathLabel.setText("/");
		ACommandExecutor.requestDirectoryListing("/");
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
		
		if( listing != null )
		{
			localPathLabel.setText(path);
			localDirectoryView.setAdapter(new ArrayAdapter<File>(this,R.layout.simple,listing));
		}
	}
	
	protected void populateRDV(String[] listing)
	{
		if( listing != null )
		{
			adapter = new ArrayAdapter<String>(this,R.layout.remotesimple,listing);
			runOnUiThread(new Runnable(){
				public void run(){
					remoteDirectoryView.setAdapter(adapter);
				}
			});
		}		
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
	        		   initializeLocalFileView();
	        	   }
	        	   catch( NumberFormatException e )
	        	   {
	        		   //PopUpWindow
	        		   showErrorMessage(e.getMessage());
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
        }else if( v.getId() == R.id.btnViewLocal ){
        	initializeLocalFileView();
        }else if( v.getId() == R.id.btnViewServ ){
        	initializeRemoteFileView();
        }
	}
    
	public void onClickLocalBrowser(View v)
	{
		TextView selectedText = (TextView)v;
		lastClickedItem = selectedText;
		lastClickedIsLocal = true;
    	String pathstr = selectedText.getText().toString();
    	File path = new File(pathstr);
    	if( path.isDirectory() ){
    		populateLDV(pathstr);
    	}else{
    		Log.v(tag,"caught click on directory listing");
    		registerForContextMenu(v);
    		v.showContextMenu();
    	}
	}
	
	public void onClickRemoteBrowser(View v)
	{
		TextView selectedText = (TextView)v;
		lastClickedItem = selectedText;
		lastClickedIsLocal = false;
    	String pathstr = selectedText.getText().toString();
    	if( pathstr.endsWith("/") ){
    		populateRDV(new String[0]);
    		ACommandExecutor.requestDirectoryListing(pathstr);
    		_singleton.remotePathLabel.setText(pathstr);
    	}else{
    		Log.v(tag,"caught click on directory listing");
    		registerForContextMenu(v);
    		v.showContextMenu();
    	}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if( item.getItemId() == R.id.upload )
		{
			ACommandExecutor.uploadFile(lastClickedItem.getText().toString());
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    if( lastClickedIsLocal )
	    {
	    	inflater.inflate(R.menu.browser_contextmenu, menu);
	    }else{
	    	inflater.inflate(R.menu.remotebrowser_contextmenu, menu);
	    }
	}
}
