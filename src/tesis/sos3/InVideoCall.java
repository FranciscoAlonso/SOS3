package tesis.sos3;

import org.doubango.ngn.sip.NgnAVSession;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class InVideoCall extends Activity {

	private RelativeLayout mMainLayout;
	private View mViewInCallVideo;
	private FrameLayout mViewLocalVideoPreview;
	private FrameLayout mViewRemoteVideoPreview;
	
	private NgnAVSession mAVSession;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_in_video_call);
		//mViewLocalVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(R.id.view_call_incall_video_FrameLayout_local_video);
		//mViewRemoteVideoPreview = (FrameLayout)mViewInCallVideo.findViewById(R.id.view_call_incall_video_FrameLayout_remote_video);
		
		//mMainLayout.removeAllViews();
		//mMainLayout.addView(mViewInCallVideo);
		//loadvideopreview from screenAV imsdroid
		//mViewRemoteVideoPreview.removeAllViews();
        final View remotePreview = mAVSession.startVideoConsumerPreview();
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
		getMenuInflater().inflate(R.menu.in_video_call, menu);
		return true;
	}

}
