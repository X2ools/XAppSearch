package org.x2ools.t9apps;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ConfigFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.preference);
	}

}
