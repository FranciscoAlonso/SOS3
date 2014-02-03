package tesis.sos3;

import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnStringUtils;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class InVideoCall extends Activity {
	private static String TAG = InVideoCall.class.getCanonicalName();
	
	private RelativeLayout mMainLayout;
	private View mViewInCallVideo;
	private FrameLayout mViewLocalVideoPreview;
	private FrameLayout mViewRemoteVideoPreview;
	
	private NgnAVSession mAVSession;
	public String mid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_in_video_call);
		mid = getIntent().getStringExtra("id");
		Log.e(TAG, String.format("SESSION ID: %d", NgnStringUtils.parseLong(mid, -1)));
		mAVSession = NgnAVSession.getSession(NgnStringUtils.parseLong(mid, -1));
		if(mAVSession == null){
			Log.e(TAG, String.format("SESSION ID if null: %d", NgnStringUtils.parseLong(mid, -1)));
			Toast.makeText(this, "Session == NULL", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "Session != NULL", Toast.LENGTH_SHORT).show();
			mAVSession.incRef();
			mAVSession.setContext(this);
		}
        //final View remotePreview = mAVSession.startVideoConsumerPreview();
		/*if(remotePreview != null){
            final ViewParent viewParent = remotePreview.getParent();
            if(viewParent != null && viewParent instanceof ViewGroup){
                  ((ViewGroup)(viewParent)).removeView(remotePreview);
            }
            mViewRemoteVideoPreview.addView(remotePreview);
        }*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.in_video_call, menu);
		return true;
	}

}
