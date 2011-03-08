package com.flat20.gui.widgets;

import android.hardware.Sensor;
import android.os.SystemClock;

// TODO Register sensor listener inside this class?
// Is one listener for each SensorSlider efficient or will it slow down Android?
public class SensorSlider extends Slider {

	private final long SEND_DELAY = 75;	//milliseconds	

	// Timestamp for last send.
	private long mLastSendTime = SystemClock.uptimeMillis();

	public SensorSlider() {
		super();
	}

	@Override
	public boolean onTouchMove(int touchX, int touchY, float pressure, int pointerId) {
		return true;
	}

	@Override
	public boolean onTouchUp(int touchX, int touchY, float pressure, int pointerId) {
		return true;
	}

	@Override
	public boolean onTouchUpOutside(int touchX, int touchY, float pressure, int pointerId) {
		return true;
	}

	// Consider having one class per Sensor type so we don't need the switch statement. 
	public void onSensorChanged(Sensor sensor, float[] sensorValues) {
		//Stop sensor update flooding
		if (SystemClock.uptimeMillis() > mLastSendTime + SEND_DELAY) {
			switch (sensor.getType()) {		//Single-value sensors
				case Sensor.TYPE_LIGHT:	
				case Sensor.TYPE_PRESSURE:
				case Sensor.TYPE_PROXIMITY:
				case Sensor.TYPE_TEMPERATURE:						
					final float max = sensor.getMaximumRange();
					//scale to 0..1 range:
					final float val = (sensorValues[0]+max)/(2*max);
					setMeterHeight( (int)(val*height) );
					getMidiController().sendParameter(CC_VALUE, (int)(val*0x7F));
					mLastSendTime = SystemClock.uptimeMillis();
					break;		
			}
		}
	}
	
}
