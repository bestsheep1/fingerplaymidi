package com.flat20.fingerplay.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Transmitter;

import com.flat20.fingerplay.FingerPlayServer;
import com.flat20.fingerplay.Midi;
import com.flat20.fingerplay.MidiReceiver;
import com.flat20.fingerplay.MidiReceiver.IMidiListener;
import com.flat20.fingerplay.socket.commands.FingerReader;
import com.flat20.fingerplay.socket.commands.FingerWriter;
import com.flat20.fingerplay.socket.commands.FingerReader.IReceiver;
import com.flat20.fingerplay.socket.commands.midi.MidiControlChange;
import com.flat20.fingerplay.socket.commands.midi.MidiNoteOff;
import com.flat20.fingerplay.socket.commands.midi.MidiSocketCommand;
import com.flat20.fingerplay.socket.commands.misc.DeviceList;
import com.flat20.fingerplay.socket.commands.misc.RequestMidiDeviceList;
import com.flat20.fingerplay.socket.commands.misc.SetMidiDevice;
import com.flat20.fingerplay.socket.commands.misc.Version;
import com.flat20.fingerplay.view.IView;

public class ClientSocketThread implements Runnable, IReceiver, IMidiListener {

	final private Socket client;
	final private Midi mMidiOut; // In from FP client and OUT to DAW.
	final private Midi mMidiIn; // In from DAW and OUT to FP client.

	final private IView mView;

	final private DataInputStream in;
	final private DataOutputStream out;
	final private FingerWriter mWriter;

	final private MidiReceiver mMidiReceiver;

	public ClientSocketThread(Socket client, IView view) throws IOException {
		this.client = client;
		mView = view;
		
		mMidiOut = new Midi();
		mMidiIn = new Midi();

		in = new DataInputStream(client.getInputStream());
		out = new DataOutputStream(client.getOutputStream());
		mWriter = new FingerWriter(out);

		mMidiReceiver = new MidiReceiver(this);

		//mBuffer = new byte[ 0xFFFF ]; // absolute maximum length is 65535
		mView.print("hello");
	}

	public void run() {
		try {

			final FingerReader reader = new FingerReader(in, this);

			while (client.isConnected()) {
				try {
					// Reads one command
					reader.readCommand();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			mView.print("Client message too long for buffer.");
			e.printStackTrace();
		} catch(Exception e) {
			mView.print("S: Error");
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException io) {
			}
		}
		mView.print("Phone disconnected.");
		return;
	}

	public void onVersion(Version clientVersion) throws Exception {//byte command, DataInputStream in, DataOutputStream out) throws Exception {
		mView.print("Client version: " + clientVersion.getVersion());

		Version version = new Version(FingerPlayServer.VERSION);

		mWriter.write(version);
	}

	public void onMidiSocketCommand(MidiSocketCommand socketCommand) throws Exception {
		mView.print("midiCommand = " + socketCommand.midiCommand + " channel = " + socketCommand.channel + ", data1 = " + socketCommand.data1 + " data2 = " + socketCommand.data2);
		synchronized (mMidiOut) {
			mMidiOut.sendShortMessage(socketCommand.midiCommand, socketCommand.channel, socketCommand.data1, socketCommand.data2);						
		}
	}

	public void onRequestMidiDeviceList(RequestMidiDeviceList request) throws Exception {

		String[] deviceNames;
		if (request.getType() == DeviceList.TYPE_OUT)
			deviceNames = Midi.getDeviceNames(false, true);
		else
			deviceNames = Midi.getDeviceNames(true, false);

		String allDevices = "";
		for (int i=0; i<deviceNames.length; i++) {
			allDevices += deviceNames[i] + "%";
		}
		if (deviceNames.length > 0)
			allDevices = allDevices.substring(0, allDevices.length()-1);

		DeviceList deviceList = new DeviceList( request.getType(), allDevices );

		mWriter.write(deviceList);
	}

	public void onSetMidiDevice(SetMidiDevice ssm) throws Exception {


		int type = ssm.getType();
		String device = ssm.getDevice();

		Midi midi = (type==DeviceList.TYPE_OUT) ? mMidiOut : mMidiIn;

		mView.print("Set MIDI Device: " + device);
		synchronized (midi) {
			midi.close();
			MidiDevice midiDevice = midi.open(device, (type==DeviceList.TYPE_OUT) ? false : true);

			mView.print("midiDevice = " + midiDevice);

			if (midiDevice != null) {
				Transmitter	t = midiDevice.getTransmitter();
				if (t != null)
					t.setReceiver(mMidiReceiver);
			}

			// Out device doesn't necessarily have the same name as the input device.
			// And it does matter so we need to send two separate commands.
			/*
			MidiDevice midiDeviceOUT = midi.open(device, true); // true = bForOutput
			mView.print("midiDeviceOUT = " + midiDeviceOUT);

			if (midiDeviceOUT != null) {
				Transmitter	t = midiDeviceOUT.getTransmitter();
				if (t != null)
					t.setReceiver(mMidiReceiver);
			}*/

		}
	}



	public void onDeviceList(DeviceList deviceList) throws Exception {
		
	}


	// IMidiListener - Incoming MIDI from DAW is sent back to the FP Client.

	public void onControlChange(int channel, int control2, int value) {
		MidiControlChange mcc = new MidiControlChange(0xB0, channel, control2, value);
		mView.print("IMidiListener.onControlChange channel: " + channel + ", control2: " + control2 + ", value: " + value);
		try {
			mWriter.write( mcc );
		} catch (Exception e) {
			
		}
	}

	public void onNoteOff(int channel, int key, int velocity) {
		MidiNoteOff mno = new MidiNoteOff(channel, key, velocity);
		try {
			mWriter.write( mno );
		} catch (Exception e) {
			
		}
	}

	public void onNoteOn(int channel, int key, int velocity) {
		MidiNoteOff mno = new MidiNoteOff(channel, key, velocity);
		try {
			mWriter.write( mno );
		} catch (Exception e) {
			
		}
	}

}