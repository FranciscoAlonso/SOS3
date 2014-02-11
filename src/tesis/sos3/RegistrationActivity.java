package tesis.sos3;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import tesis.sos3.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegistrationActivity extends Activity {
	private static String TAG = RegistrationActivity.class.getCanonicalName();

	private final NgnEngine mEngine;
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	
	private TextView registrationStatusText;
	private Button signInOutButton;
	private Button callButton;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	public RegistrationActivity(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		//oncreate
		registrationStatusText = (TextView)findViewById(R.id.registrationStatus);
		signInOutButton = (Button)findViewById(R.id.signInOut);
		callButton = (Button)findViewById(R.id.callTest);
		callButton.setEnabled(false);
		
		mSipBroadCastRecv = new BroadcastReceiver() { //recibir eventos de registro SIP
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				//registration event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_NOK:
							registrationStatusText.setText("Failed to register");
							break;
						case UNREGISTRATION_OK:
							registrationStatusText.setText("You are now unregistered");
							break;
						case REGISTRATION_OK:
							registrationStatusText.setText("You are now registered");
							break;
						case REGISTRATION_INPROGRESS:
							registrationStatusText.setText("Trying to register...");
							break;
						case UNREGISTRATION_INPROGRESS:
							registrationStatusText.setText("Trying to unregister...");
							break;
						case UNREGISTRATION_NOK:
							registrationStatusText.setText("Failed to unregister");
							break;
					}
					signInOutButton.setText(mSipService.isRegistered() ? "Sign Out" : "Sign In");
					callButton.setEnabled(mSipService.isRegistered());
				}
			}
        };
        final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter); //registrar el bcast receiver
        
	    signInOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mEngine.isStarted()){
					if(!mSipService.isRegistered()){
						// Set credentials (get them from SOS BD or sip server data)
						//192.168.1.120 home
						//192.168.2.18 tony's VM at work
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
								"6002");
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
								"sip:6002@192.168.2.18");
						mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
								"bob123");
						mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
								"192.168.2.18");
						mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
								5060);
						mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
								"192.168.2.18");
						// VERY IMPORTANT: Commit changes
						mConfigurationService.commit();
						// register (log in)
						mSipService.register(RegistrationActivity.this);
						
						
					}
					
					else{
						// unregister (log out)
						mSipService.unRegister();
					}
				}
				else{
					registrationStatusText.setText("Engine not started yet");
				}
				
			}
		});
	}

	public void onCallClick(View view){
		Intent intent = new Intent(this, CallTestActivity.class);
	    startActivity(intent);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}

}
