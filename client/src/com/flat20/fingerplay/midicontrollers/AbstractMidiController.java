package com.flat20.fingerplay.midicontrollers;

import java.util.HashMap;

import android.util.Log;

import com.flat20.fingerplay.config.IConfigItemView;
import com.flat20.fingerplay.config.dto.ConfigItemParameters;
import com.flat20.gui.widgets.MidiWidget;

public abstract class AbstractMidiController implements IMidiController {

	private String mName = null;
	private Parameter[] mParameters = null;

	private IOnControlChangeListener mListener = null;
	
	// The MidiWidget this controller belongs to.
	private MidiWidget mView = null;
	
	

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void setName(String name) {
		mName = name;
	}

	@Override
	public Parameter[] getParameters() {
		return mParameters;
	}

	/**
	 * Sends the parameter using the value (0x00-0x7FF)
	 * 
	 * @param parameterId
	 * @param value
	 */
	@Override
	public void sendParameter(int parameterId, int value) {

		if (mListener == null)
			return;

		try {
			final Parameter p = mParameters[parameterId];

			final int type = p.type;
			switch(type) {
				case Parameter.TYPE_CONTROL_CHANGE:
					mListener.onControlChange(this, p, value);
					break;
				case Parameter.TYPE_NOTE_ON:
					mListener.onNoteOn(this, p, value);
					break;
				case Parameter.TYPE_NOTE_OFF:
					mListener.onNoteOff(this, p, value);
					break;
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			Log.w("AbstractMidiController", "Controller tried to send a parameter which wasn't defined in the XML. " + e);
		}

	}

	@Override
	public void updateParameter(Parameter parameter, int value) {
		// mView should receive an update event and it can check the model
		// for any changed variables.
		// Or it can share the Parameter list with the controller and that will be
		// the model.
		if (mView != null)
			mView.onParameterUpdated(parameter, value);
	}

	@Override
    public void setOnControlChangeListener(IOnControlChangeListener l) {
    	mListener = l;
    }

	@Override
	public void setView(IConfigItemView view) throws Exception {
		if (view instanceof MidiWidget)
			mView = (MidiWidget) view;
		else
			throw new Exception("Illegal view assigned to AbstractMidiController; Must be of class MidiWidget.");
	}

	@Override
	public MidiWidget getView() {
		return mView;
	}


	// IConfigurable

	@Override
	public void setParameters(ConfigItemParameters parameters) {

		mParameters = new Parameter[parameters.data.size()];

		for (HashMap<String, Object> map : parameters.data) {

			int id = Integer.parseInt((String)map.get("id"));
			int channel = Integer.parseInt((String)map.get("channel"));
			int controllerNumber = Integer.parseInt( (String)map.get("controllerNumber") );
			String name = (String)map.get("name");
			boolean visible = Boolean.parseBoolean( (String)map.get("visible") );
			boolean usePressure = Boolean.parseBoolean( (String)map.get("usePressure") );
			String stringType = (String)map.get("type");
			int type = Parameter.TYPE_CONTROL_CHANGE;

			if ("controlChange".equals(stringType)) {
				type = Parameter.TYPE_CONTROL_CHANGE;
			} else if ("note".equals(stringType)) {
				type = Parameter.TYPE_NOTE_ON;
			} else if ("noteOn".equals(stringType)) {
				type = Parameter.TYPE_NOTE_ON;
			} else if ("noteOff".equals(stringType)) {
				type = Parameter.TYPE_NOTE_OFF;
			}

			Parameter param = new Parameter(id, channel, controllerNumber, name, type, visible, usePressure);
			mParameters[id] = param;
		}

	}

	public String toString() {
		String result = "AbstractMidiController name: " + this.mName + " Parameters:\n";
		if (mParameters != null) {
			for (int i=0; i<mParameters.length; i++) {
				result += mParameters[i] + "\n";
			}
		}
		return result;
	}
}
