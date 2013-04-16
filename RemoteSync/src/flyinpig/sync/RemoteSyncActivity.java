package flyinpig.sync;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import flyinpig.sync.io.ClientThread;

public class RemoteSyncActivity extends Activity {
	
	private UIListener mGetListener = null;
	public static String tag = "RemoteSync";
	ClientThread mClientThread;
	Handler mHandler;
	String alertMessage;
	
	static RemoteSyncActivity _singleton = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        _singleton = this;
        mGetListener = new UIListener();
        mHandler = new Handler();
      
        setContentView(R.layout.main);        
        
        Button connectButton = (Button)findViewById(R.id.btnConnect);
        
        connectButton.setOnClickListener(mGetListener);
        
        alertMessage = "Test Alert";
        
        // alert test
        AlertDialog.Builder alert = new AlertDialog.Builder(_singleton);
    	alert.setMessage(_singleton.alertMessage);
    	AlertDialog dialog = alert.create();
    	if( dialog != null ){
    		dialog.show();
    	}
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
		showErrorMessage(message);
		Button btnConnect = (Button)_singleton.findViewById(R.id.btnConnect);
		if( btnConnect != null )
		{
			btnConnect.setText("Establish\nConnection");
		}
	}
    
}
