package com.flat20.fingerplay.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.flat20.fingerplay.FingerPlayActivity;
import com.flat20.fingerplay.midicontrollers.MidiControllerManager;
import com.flat20.fingerplay.network.ConnectionManager;

/**
 * SettingsModel is a singleton because it's used by both Settings
 * and FingerPlayActivity. Could do with a more suitable name.
 * 
 * @author andreas
 *
 */
public class SettingsModel {

	public static final int STATE_CONNECTED = 1;
	public static final int STATE_DISCONNECTED = 2;
	public static final int STATE_CONNECTING = 3;
	public static final int STATE_CONNECTING_FAIL = 4;
	public static final int STATE_CONNECTING_SUCCESS = 5;
	public static final int STATE_DISCONNECTING = 6;

	protected SharedPreferences mSharedPreferences;
	protected SettingsView mView = null;

	public MidiControllerManager midiControllerManager = null;
	public int state = 0;
	public int serverType = -1;
	public String serverAddress = null;
	public String midiDeviceOut = null;
	public String[] midiDevicesOut = null;
	public String midiDeviceIn = null;
	public String[] midiDevicesIn = null;
	public String layoutFile = null;
	public String[] layoutFiles = null;



	private static SettingsModel sSingleton = new SettingsModel();
	private Context activity = null;

	public static SettingsModel getInstance() {
		return sSingleton;
	}

	private SettingsModel() {
	}

	public void init(Context context) {
		midiControllerManager = MidiControllerManager.getInstance();
		activity = context;
		
		// Load saved data
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
		String type = mSharedPreferences.getString("settings_server_type", "-1");
		serverType = Integer.parseInt(type);
		serverAddress = mSharedPreferences.getString("settings_server_address", null);
		midiDeviceOut = mSharedPreferences.getString("settings_midi_out", null);
		midiDeviceIn = mSharedPreferences.getString("settings_midi_in", null);
		layoutFile = mSharedPreferences.getString("settings_layout_file", null);

		
        if (serverType != -1)
        	ConnectionManager.getInstance().setConnection( serverType );
	}

	public void setView(SettingsView view) {
		mView = view;
		updateView();
	}

	public void setState(int state) {
		if (this.state != state) {
			this.state = state;
			updateView();
		}
	}

	public void setServerType(int value) {
		serverType = value;
		updateView();
	}

	public void setServerAddress(String value) {
		serverAddress = value;
		updateView();
	}

	public void setMidiDevicesOut(String[] value) {
		midiDevicesOut = value;
		updateView();
	}

	public void setMidiDeviceOut(String value) {
		midiDeviceOut = value;
		updateView();
	}

	public void setMidiDevicesIn(String[] value) {
		midiDevicesIn = value;
		updateView();
	}

	public void setMidiDeviceIn(String value) {
		midiDeviceIn = value;
		updateView();
	}

	public void setLayoutFiles(String[] value) {
		layoutFiles = value;
		updateView();
	}

	public void setLayoutFile(String value) {
		layoutFile = value;
		updateView();
		((FingerPlayActivity)activity).parseConfiguration();
	}
	
	protected void updateView() {
		if (mView != null)
			mView.update();
	}
}
