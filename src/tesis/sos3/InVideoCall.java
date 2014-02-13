package tesis.sos3;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.R;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceView;
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
	private LayoutInflater mInflater;
	public NgnAVSession avSession;
	
	private INgnConfigurationService mConfigurationService;
	private INgnSipService mSipService;
	private NgnEngine mEngine;
	private INgnSipService sipService;
	public String mid;
	public String remoteUri;
	public String validUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(tesis.sos3.R.layout.activity_in_video_call);
		
		mEngine = NgnEngine.getInstance();
		mSipService = mEngine.getSipService();
		mConfigurationService = mEngine.getConfigurationService();
		
		mInflater = LayoutInflater.from(this);
		
		remoteUri = "6001";
		validUri = NgnUriUtils.makeValidSipUri(remoteUri);
		avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.AudioVideo);
		avSession.setRemotePartyUri(validUri); // HACK
		
		if(avSession == null){
			Log.e(TAG, String.format("SESSION ID if null: %d", avSession.getId()));
			Toast.makeText(this, "Session == NULL", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "Session != NULL", Toast.LENGTH_SHORT).show();
			//Log.e(TAG, String.format("SESSION ID: %d", avSession.getId()));
			avSession.incRef();
			Log.e(TAG, String.format("Context: %s",getBaseContext().toString()));
			avSession.setContext(getBaseContext());
			
			mMainLayout = (RelativeLayout)findViewById(tesis.sos3.R.id.invideo_call_relativeLayout); //agarro el layout actual base
			if(mMainLayout == null){
				Log.e(TAG, String.format("mMainLayout == null"));	
			}else{
				mViewInCallVideo = mInflater.inflate(tesis.sos3.R.layout.view_call_incall_video, null); 
				
				mViewLocalVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_FrameLayout_local_video);
				mViewRemoteVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_FrameLayout_remote_video);
				
				mMainLayout.removeAllViews();//nullpointer exception
				mMainLayout.addView(mViewInCallVideo);
				
				avSession.setState(InviteState.INPROGRESS);
				
				// Video Consumer
				loadVideoPreview();
				
				// Video Producer
				startStopVideo(avSession.isSendingVideo());
				
				//startStopVideo(true);
			}
			
		}
		
		
        //final View remotePreview = mAVSession.startVideoConsumerPreview();
		/*if(remotePreview != null){
            final ViewParent viewParent = remotePreview.getParent();
            if(viewParent != null && viewParent instanceof ViewGroup){
                  ((ViewGroup)(viewParent)).removeView(remotePreview);
            }
            mViewRemoteVideoPreview.addView(remotePreview);
        }*/
		avSession.makeCall(validUri);
	}

	private void startStopVideo(boolean bStart) {

		//Log.e(TAG, "startStopVideo("+bStart+")");
/*		if(!mIsVideoCall){
			return;
		}*/
		
		avSession.setSendingVideo(bStart);
		
		if(mViewLocalVideoPreview != null){
			mViewLocalVideoPreview.removeAllViews();
			if(bStart){
				//cancelBlankPacket();
				Log.e(TAG, String.format("startstopvideo producer Context: %s", getBaseContext().toString()));
				final View localPreview = avSession.startVideoProducerPreview(); //ojo esto esta retornando null
				if(localPreview != null){
					final ViewParent viewParent = localPreview.getParent();
					if(viewParent != null && viewParent instanceof ViewGroup){
						((ViewGroup)(viewParent)).removeView(localPreview);
					}
					if(localPreview instanceof SurfaceView){
						((SurfaceView)localPreview).setZOrderOnTop(true);
					}
					mViewLocalVideoPreview.addView(localPreview);
					mViewLocalVideoPreview.bringChildToFront(localPreview);
				}else{
					Log.e(TAG, "localpreview == null");
				}
			}
			mViewLocalVideoPreview.setVisibility(bStart ? View.VISIBLE : View.GONE);
			mViewLocalVideoPreview.bringToFront();
		}
	}

	private void loadVideoPreview() {
		mViewRemoteVideoPreview.removeAllViews();
		Log.e(TAG, String.format("loadview consumer Context: %s", getBaseContext().toString()));
        final View remotePreview = avSession.startVideoConsumerPreview(); //ojo retorna null tambien
        if(remotePreview == null){
        	Log.e(TAG, "remotePreview == null");
        }
		if(remotePreview != null){
            final ViewParent viewParent = remotePreview.getParent();
            if(viewParent != null && viewParent instanceof ViewGroup){
                  ((ViewGroup)(viewParent)).removeView(remotePreview);
            }
            mViewRemoteVideoPreview.addView(remotePreview);
        }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(tesis.sos3.R.menu.in_video_call, menu);
		return true;
	}

}
