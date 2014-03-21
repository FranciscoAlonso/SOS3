package tesis.sos3;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;

import tesis.sos3.R;
import tesis.sos3.RegistrationActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends Activity {

	private final NgnEngine mEngine;
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	private Context mContext;
	
	private Button logInButton; 
	private Button listButton; 
	
	public LogInActivity(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		
		logInButton = (Button)findViewById(R.id.audioCall);
		logInButton.setEnabled(false);
		
		if(!mEngine.isStarted()){
			if(mEngine.start()){
				logInButton.setEnabled(true);
			}else{
				logInButton.setEnabled(true);
			}
		}
		
		listButton = (Button)findViewById(R.id.list_button);
		listButton.setOnClickListener(listOnclickListener);
		mContext = this;
	}
	
	private OnClickListener listOnclickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext, RegistrationActivity.class);
		    startActivity(intent);
			
		}
	}; 
	
	public void onlogInButtonClick(View view){
		/*
		String userNameString = userNameEditText.getText().toString();
		EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		String passwordString = passwordEditText.getText().toString();
		Toast.makeText(this, "text: " + NgnApplication.getContext().toString(), Toast.LENGTH_SHORT).show();
		 */
		Intent intent = new Intent(this, RegistrationActivity.class);
	    startActivity(intent);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;
	}

}
