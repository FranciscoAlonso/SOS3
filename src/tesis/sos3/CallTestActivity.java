package tesis.sos3;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.services.impl.NgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.utils.NgnUriUtils;

import android.util.Log;
import android.nfc.Tag;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class CallTestActivity extends Activity {
	private static String TAG = CallTestActivity.class.getCanonicalName();
	public String remoteUri;
	public String validUri;
	
	private INgnConfigurationService mConfigurationService;
	private INgnSipService mSipService;
	private NgnEngine mEngine;
	private INgnSipService sipService;
	//final NgnSipStack sipStack = sipService.getSipStack(); //in case we need the e.164 number
	private NgnMediaType mediaType;
	//final NgnAVSession avSession;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_test);
		
		//oncreate
		mEngine = NgnEngine.getInstance();
		mSipService = mEngine.getSipService();
		mConfigurationService = mEngine.getConfigurationService();
	}
	
	public void audioCallTest(View view){
		remoteUri = "6001";
		validUri = NgnUriUtils.makeValidSipUri(remoteUri);
		NgnAVSession avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.Audio);
		if(avSession.makeCall(validUri)){
			Toast.makeText(this, "audio Call: OK", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "audio Call: NOT OK", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void videoCallTest(View view){
		remoteUri = "6001";
		validUri = NgnUriUtils.makeValidSipUri(remoteUri);
		final NgnAVSession avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.AudioVideo);
		avSession.setRemotePartyUri(validUri); // HACK
		Intent intent = new Intent(this, InVideoCall.class);
		Log.e(TAG, "SESSION ID: " + Long.toString(avSession.getId()));
		intent.putExtra("id", Long.toString(avSession.getId()));
	    startActivity(intent);

		if(avSession.makeCall(validUri)){
			Toast.makeText(this, "video Call: OK", Toast.LENGTH_SHORT).show();			
		}else{
			Toast.makeText(this, "video Call: NOT OK", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.call_test, menu);
		return true;
	}

}
