package tesis.sos3;

import java.util.Date;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private ViewType mCurrentView;
	private BroadcastReceiver mBroadCastRecv;
	
	private INgnConfigurationService mConfigurationService;
	private INgnSipService mSipService;
	private NgnEngine mEngine;
	private INgnSipService sipService;
	public String mid;
	public String remoteUri;
	public String validUri;
	
	private static enum ViewType{
		ViewNone,
		ViewTrying,
		ViewInCall,
		ViewProxSensor,
		ViewTermwait
	}
	
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
				
				mBroadCastRecv = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(intent.getAction())){
							handleSipEvent(intent);
							Log.e(TAG, ">>> 1");
						}
						else if(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(intent.getAction())){
							Log.e(TAG, ">>> 2");
							handleMediaEvent(intent);
						}
					}
				};
				
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
				intentFilter.addAction(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT);
			    registerReceiver(mBroadCastRecv, intentFilter);/**/
				/*mViewLocalVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_FrameLayout_local_video);
				mViewRemoteVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_FrameLayout_remote_video);
				
				mMainLayout.removeAllViews();//nullpointer exception
				mMainLayout.addView(mViewInCallVideo);
				
				avSession.setState(InviteState.INPROGRESS);
				
				// Video Consumer
				loadVideoPreview();
				
				// Video Producer
				startStopVideo(avSession.isSendingVideo());
				
				//startStopVideo(true);*/
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

	private void loadInCallVideoView(){
		Log.d(TAG, "loadInCallVideoView()");
		//if(mViewInCallVideo == null){
			mViewInCallVideo = mInflater.inflate(tesis.sos3.R.layout.view_call_incall_video, null);
			mViewLocalVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_FrameLayout_local_video);
			mViewRemoteVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_FrameLayout_remote_video);
		//}
		/*if(mTvDuration != null){
			synchronized(mTvDuration){
		        mTvDuration = null;
			}
		}*/
		//mTvInfo = null;
		mMainLayout.removeAllViews();
		mMainLayout.addView(mViewInCallVideo);
		
		final View viewSecure = mViewInCallVideo.findViewById(tesis.sos3.R.id.view_call_incall_video_imageView_secure);
		if(viewSecure != null){
			viewSecure.setVisibility(avSession.isSecure() ? View.VISIBLE : View.INVISIBLE);
		}
		
		// Video Consumer
		loadVideoPreview();
		
		// Video Producer
		startStopVideo(avSession.isSendingVideo());
		
		mCurrentView = ViewType.ViewInCall;
	}
	
	private void loadVideoPreview() {
		if(mViewRemoteVideoPreview != null){
			mViewRemoteVideoPreview.removeAllViews();	
		}else{
			Log.e(TAG, String.format("mViewRemoteVideoPreview es NULL"));
		}		
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

	private void handleSipEvent(Intent intent){
		@SuppressWarnings("unused")
		InviteState state;
		if(avSession == null){
			Log.e(TAG, "Invalid session object");
			return;
		}
		final String action = intent.getAction();
		if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
			NgnInviteEventArgs args = intent.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			if(args.getSessionId() != avSession.getId()){
				return;
			}
			
			switch((state = avSession.getState())){
				case NONE:
				default:
					break;
					
				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					//loadTryingView();
					Log.e(TAG, ">>> 1 - TRYINGview ringing");
					break;
					
				case EARLY_MEDIA:
				case INCALL:
					Log.e(TAG, ">>> 1 - INCALLview call in course");
					//if(state == InviteState.INCALL){
						// stop using the speaker (also done in ServiceManager())
						mEngine.getSoundService().stopRingTone();
						avSession.setSpeakerphoneOn(false);
					//}
					//if(state == InviteState.INCALL){
						loadInCallView();
					//}
					// Send blank packets to open NAT pinhole
					/*if(avSession != null){
						applyCamRotation(avSession.compensCamRotation(true));
						mTimerBlankPacket.schedule(mTimerTaskBlankPacket, 0, 250);
						if(!mIsVideoCall){
							mTimerInCall.schedule(mTimerTaskInCall, 0, 1000);
						}
					}*/
					
					// release power lock if not video call
					/*if(!mIsVideoCall && mWakeLock != null && mWakeLock.isHeld()){
						mWakeLock.release();
			        }*/
					
					switch(args.getEventType()){
						case REMOTE_DEVICE_INFO_CHANGED:
							{
								Log.d(TAG, String.format("Remote device info changed: orientation: %s", avSession.getRemoteDeviceInfo().getOrientation()));
								break;
							}
						case MEDIA_UPDATED:
							{
								if(true){
									loadInCallVideoView();
									Log.e(TAG, ">>> 1 - INCALLview is video+audio");
								}
								else{
									//loadInCallAudioView();
									Log.e(TAG, ">>> 1 - INCALLview is audio");
								}
								break;
							}
						default:
							{
								break;
							}
					}					
					break;
					
				case TERMINATING:
				case TERMINATED:
					//mTimerSuicide.schedule(mTimerTaskSuicide, new Date(new Date().getTime() + 1500)); //muestra la pantalla de home cuando se está en la de terminated-call
					//mTimerTaskInCall.cancel();
					//mTimerBlankPacket.cancel();
					//loadTermView(SHOW_SIP_PHRASE ? args.getPhrase() : null);
					
					// release power lock
					/*if(mWakeLock != null && mWakeLock.isHeld()){
						mWakeLock.release();
			        }*/
					Log.e(TAG, ">>> 1 - TERMINATED");
					break;
			}
		}
	}
	
	private void loadInCallView(){
		if(mCurrentView == ViewType.ViewInCall){
			return;
		}
		Log.d(TAG, "loadInCallView()");
		
		if(true){
			loadInCallVideoView();
		}
		else{
			//loadInCallAudioView();
		}
	}
	
	private void handleMediaEvent(Intent intent){
		final String action = intent.getAction();
	
		if(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT.equals(action)){
			NgnMediaPluginEventArgs args = intent.getParcelableExtra(NgnMediaPluginEventArgs.EXTRA_EMBEDDED);
			if(args == null){
				Log.e(TAG, "Invalid event args");
				return;
			}
			
			switch(args.getEventType()){
				case STARTED_OK: //started or restarted (e.g. reINVITE)
				{
					//mIsVideoCall = (avSession.getMediaType() == NgnMediaType.AudioVideo || avSession.getMediaType() == NgnMediaType.Video);
					loadView();
					Log.e(TAG, ">>> 2 - loading view");
					break;
				}
				case PREPARED_OK:
				case PREPARED_NOK:
				case STARTED_NOK:
				case STOPPED_OK:
				case STOPPED_NOK:
				case PAUSED_OK:
				case PAUSED_NOK:
				{
					break;
				}
			}
		}
	}
	
	private void loadView(){
		switch(avSession.getState()){
	        case INCOMING:
	        case INPROGRESS:
	        case REMOTE_RINGING:
	        	//loadTryingView();
	        	Log.e(TAG, ">>> 2 - loading TRYING view");
	        	break;
	        	
	        case INCALL:
	        case EARLY_MEDIA:
	        	loadInCallView();
	        	Log.e(TAG, ">>> 2 - loading IN CALL view");
	        	break;
	        	
	        case NONE:
	        case TERMINATING:
	        case TERMINATED:
	        default:
	        	//loadTermView();
	        	Log.e(TAG, ">>> 2 - loading TERMINATED view");
	        	break;
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(tesis.sos3.R.menu.in_video_call, menu);
		return true;
	}

}
