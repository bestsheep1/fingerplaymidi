package com.flat20.fingerplay.midicontrollers;

public interface IOnControlChangeListener {
	public void onControlChange(IMidiController midiController, Parameter p, int value);
	public void onNoteOn(IMidiController midiController, Parameter p, int velocity);
	public void onNoteOff(IMidiController midiController, Parameter p, int velocity);
}
