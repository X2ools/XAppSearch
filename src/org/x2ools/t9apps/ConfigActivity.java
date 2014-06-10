package org.x2ools.t9apps;

import android.app.Activity;
import android.os.Bundle;

public class ConfigActivity extends Activity {
	public static final String TAG = "ConfigActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new ConfigFragment()).commit();  
	}

}
