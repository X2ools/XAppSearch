package org.x2ools.t9apps;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

public class ConfigActivity extends Activity {
	public static final String TAG = "ConfigActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fragment fragment = new ConfigFragment();
		getFragmentManager().beginTransaction().add(fragment, "").commit();
		Intent viewService = new Intent(this, ViewManagerService.class);
		viewService.putExtra(ViewManagerService.EXTRA_HIDE_VIEW, false);
		startService(viewService);
	}

}
