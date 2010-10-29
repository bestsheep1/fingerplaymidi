package com.flat20.fingerplay.settings;

import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;
import android.util.Log;

import com.flat20.fingerplay.midicontrollers.IMidiController;
import com.flat20.fingerplay.network.ConnectionManager;
import com.flat20.fingerplay.socket.commands.SocketCommand;
import com.flat20.fingerplay.socket.commands.misc.DeviceList;
import com.flat20.fingerplay.socket.commands.misc.RequestMidiDeviceList;
import com.flat20.fingerplay.socket.commands.misc.SetMidiDevice;
import com.flat20.fingerplay.socket.commands.misc.Version;

public class SettingsController {

	SettingsModel mModel;
	SettingsView mView;

	// Separate model
	protected ConnectionManager mConnectionManager = null;

	public SettingsController(SettingsView view) {
		mModel = SettingsModel.getInstance();
		mView = view;

		mConnectionManager = ConnectionManager.getInstance();
		mConnectionManager.addConnectionListener(mConnectionListener);

		mModel.setState( mConnectionManager.isConnected() ? SettingsModel.STATE_CONNECTING_SUCCESS : SettingsModel.STATE_DISCONNECTED );

		// A bit of a hack to get the view to update the preferences properly.
		// TODO: Just call requestMidiDeviceList() and set STATE_CONNECTED like it should be.
		if (mModel.state == SettingsModel.STATE_CONNECTING_SUCCESS) {
			mModel.setState(SettingsModel.STATE_CONNECTED);
			requestMidiDeviceList();
		}

		// Get the layout XML files from the sdcard. 
		updateLayoutsList();
	}

	public void destroy() {
		mConnectionManager.removeConnectionListener(mConnectionListener);
	}

    // Server updates listener

    private ConnectionManager.IConnectionListener mConnectionListener = new ConnectionManager.IConnectionListener() {

    	public void onConnect() {
    		if (mConnectionManager.isConnected()) {
    			mModel.setState(SettingsModel.STATE_CONNECTING_SUCCESS);
    			mModel.setState(SettingsModel.STATE_CONNECTED);
    			if (mModel.serverType == ConnectionManager.CONNECTION_TYPE_FINGERSERVER)
    				requestMidiDeviceList();
    		} else {
    			mModel.setState(SettingsModel.STATE_CONNECTING_FAIL);
    			mModel.setState(SettingsModel.STATE_DISCONNECTED);
    		}

    	}

    	public void onDisconnect() {
    		mModel.setState(SettingsModel.STATE_DISCONNECTED);
    	}

    	public void onError(String errorMessage) {
    		mView.displayError(errorMessage);
    	}

    	public void onSocketCommand(SocketCommand sm) {
    		if (sm.command == SocketCommand.COMMAND_MIDI_DEVICE_LIST) {
    			DeviceList ssm = (DeviceList) sm;
    			String[] deviceNames = ssm.getDeviceList().split("%");
    			if (ssm.getType() == DeviceList.TYPE_OUT) {
    				mModel.setMidiDevicesOut(deviceNames);
    				if (mModel.midiDeviceOut != null)
    					setMidiDevice(DeviceList.TYPE_OUT, mModel.midiDeviceOut);
    			} else {
    				mModel.setMidiDevicesIn(deviceNames);
    				if (mModel.midiDeviceIn != null)
    					setMidiDevice(DeviceList.TYPE_IN, mModel.midiDeviceIn);
    			}
    		} else if (sm.command == SocketCommand.COMMAND_VERSION) {
    			Version version = (Version) sm;
    			Log.i("Settings", "version = " + version.getVersion());
    		}
    	}

    };


    // Controller commands

    protected void setConnectionType(int connectionType) {
        mConnectionManager.setConnection(connectionType);
		mModel.setServerType( connectionType );
    }

    protected void requestMidiDeviceList() {
    	if (mConnectionManager.isConnected()) {
    		RequestMidiDeviceList sm = new RequestMidiDeviceList();
    		mConnectionManager.send(sm);
    	}
    }

    protected void setMidiDevice(int type, String deviceName) {
    	if (mConnectionManager.isConnected()) {
    		SetMidiDevice setDevice = new SetMidiDevice(DeviceList.TYPE_OUT, deviceName);
    		mConnectionManager.send(setDevice);
    	}
    	if (type == DeviceList.TYPE_OUT)
    		mModel.setMidiDeviceOut(deviceName);
    	else
    		mModel.setMidiDeviceIn(deviceName);
	}

    protected void serverConnect() {
		if ( mModel.serverType != -1 && !mConnectionManager.isConnected() ) {
			mConnectionManager.connect( mModel.serverAddress );
			mModel.setState(SettingsModel.STATE_CONNECTING);
		} else {
			mConnectionManager.disconnect();
			mModel.setState(SettingsModel.STATE_DISCONNECTING);
			//TODO connections should send onDisconnect
			mModel.setState(SettingsModel.STATE_DISCONNECTED);
		}
    }

    protected void sendControlChange(String controllerName, int parameterId) {
		IMidiController mc = mModel.midiControllerManager.getMidiControllerByName(controllerName);
		if (mc != null) {
			mc.sendParameter(parameterId, 0x7F);
			/*
			int controllerNumber = mc.getControllerNumber();

			Parameter p = mc.getParameterById(parameterId);
			if (p.type == Parameter.TYPE_CONTROL_CHANGE) {
				MidiControlChange socketCommand = new MidiControlChange(0xB0, 0, controllerNumber+p.id, 0x7F);
				mConnectionManager.send(socketCommand);
			} else if (p.type == Parameter.TYPE_NOTE) {
				int controllerIndex = (int) mModel.midiControllerManager.getIndex(mc);
				MidiNoteOn socketCommand = new MidiNoteOn(0, controllerIndex, 0x7F);
				mConnectionManager.send(socketCommand);
			}*/
		}

    }

	protected void setLayoutFile(String value) {
		if (!value.equals(mModel.layoutFile)) {
			mView.displayError("Restart FingerPlay MIDI to use the new layout file.");
		}
		mModel.setLayoutFile(value);
	}


	// Layout files listing

    protected void updateLayoutsList() {
        File home = new File(Environment.getExternalStorageDirectory() + "/FingerPlayMIDI/");

        XMLFilter filter = new XMLFilter();
        String[] files = home.list(filter);
        mModel.setLayoutFiles(files);
    }

    class XMLFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
        }
    }

}
