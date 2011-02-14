package com.flat20.fingerplay;

import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.flat20.fingerplay.socket.ClientSocketThread;
import com.flat20.fingerplay.socket.MulticastServer;
import com.flat20.fingerplay.socket.commands.SocketCommand;
import com.flat20.fingerplay.view.ConsoleView;
import com.flat20.fingerplay.view.IView;

public class FingerPlayServer implements Runnable{

	public static final String VERSION = "0.8.0";
	public static final int SERVERPORT = 4444;

	public static final String MULTICAST_SERVERIP = "230.0.0.1";
	public static final int MULTICAST_SERVERPORT = 9013;

	//private Midi midi;

	private static String mLocalAddress = null;
	private static int mLocalPort = -1;
	//private static InetAddress mPreferrededAddress = null;

	public void run() {
		try {
			System.out.println("\nFingerPlayServer v" + VERSION + "\n");

			// Check if there's a new version of the server online
			// Also tries to grab the localIP from the socket.

			//boolean result = 
			Updater.update(VERSION);
			/*
			if (result) {
				System.out.println("FingerPlayServer updated. Please restart.");
				System.exit(0);
			}*/

			// 
			//SocketStringCommand sm = new SetMidiDeviceCommand("apa");
			// lame..
			new SocketCommand();


 			// Open MIDI Device.

			//midi = new Midi();

			//Midi.listDevices(true, true, true);

			//MainWindow window = new MainWindow();
			ConsoleView console = new ConsoleView();

			IView view = console; // Set the view for this session.


			// Show list of MIDI devices on this computer

			String deviceNames[] = Midi.getDeviceNames(true, true);
			view.print("");
			view.print("MIDI Devices:");
			for (int i=0; i<deviceNames.length; i++) {
				view.print( deviceNames[i] );
			}
			view.print("");


			// Show network interfaces if one hasn't been selected

			// If update function didn't get the local IP we'll try
			// a cheaper alternative here. Might give us 127.0.0.1 though.

			//InetAddress localAddress = mPreferrededAddress;
			String localIP = mLocalAddress;
			int port = mLocalPort;

			//NetworkInterface ni NetworkInterface.getByName("asd");
			


			// Wait for client connection
			
			ServerSocket serverSocket = null;
			for (int i=0; i<10; i++) {
				try {
					serverSocket = new ServerSocket(port);
					break;
				} catch (BindException e) {
					e.printStackTrace();
					port++;
				}
			}

			if (serverSocket == null) {
				view.print("Couldn't find any available port to listen to.");
				return;
			}


			// Start multicast server

			String multicastOutputMessage = localIP + ":" + port;

			Thread multicastServerThread = new Thread( new MulticastServer(MULTICAST_SERVERIP, MULTICAST_SERVERPORT, multicastOutputMessage) );
			multicastServerThread.start();


			view.print("Listening on " + multicastOutputMessage);

			view.print("Waiting for connection from phone..");
			while (true) {

				Socket client = serverSocket.accept();
				ClientSocketThread st = new ClientSocketThread(client, view);
				Thread thread = new Thread( st );
				thread.start();

				view.print("Phone connected.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static void main (String[] args) {
/*
		if (args.length > 0) {

			try {
				int port = Integer.parseInt(args[0]);
				mPort = port;
			} catch (NumberFormatException e) {
				System.out.println("Couldn't set server port to " + args[0]);
			}
*/
			// Get the interface
			if (args.length > 0) {

					String validAddress = null;
					int validPort = -1;

					try {
						String address = (args[0].split(":")[0]);
						InetAddress inet = InetAddress.getByName(address);
						NetworkInterface ni = NetworkInterface.getByInetAddress(inet);
						if (ni != null)
							validAddress = inet.getHostAddress();

					} catch (UnknownHostException e) {
						System.out.println(e);
					} catch (SocketException e) {
						System.out.println(e);
					}

					try {
						validPort = Integer.parseInt( (args[0].split(":")[1]) );
					} catch (Exception e) {
					}

					if (validPort == -1) {
						try {
							validPort = Integer.parseInt( (args[0].split(":")[0]) );
						} catch (Exception e) {
						}
					}

					mLocalAddress = validAddress;
					mLocalPort = validPort;


						//NetworkInterface ni = NetworkInterface.getByInetAddress(mPreferrededAddress);
						//System.out.println(ni);
/*
					System.out.println(mPreferrededAddress);

					if (mPreferrededAddress != null) {
						NetworkInterface ni = NetworkInterface.getByInetAddress(mPreferrededAddress);
						System.out.println(ni);
					}
*/
				
				/*} catch (SocketException e) {
					System.out.println("IP address not found on any network interfaces: " + args[1]);
					System.out.println(e);
				}*/
					/*
				} catch (SocketException e) {
					System.out.println(e);
				}*/

/*
				if (mPreferrededAddress == null) {
					try {
						NetworkInterface ni = NetworkInterface.getByName(args[1]);
						System.out.println(ni);
						Enumeration addresses = ni.getInetAddresses();
						while ( addresses.hasMoreElements() ) {
							Object address = addresses.nextElement();
							System.out.println( address.toString() );
						} 
					} catch (SocketException e) {
							System.out.println(e);
					}
				}

				if (mPreferrededAddress == null) {
					System.out.println("Argument was not a valid network interface name or IP address.");
				}
*/
//			}

			} else {
			}

			// Set hostname to 127.0.0.1 if all else fails.
			if (mLocalAddress == null) {
				try {
					InetAddress inetAddress = InetAddress.getLocalHost();
					mLocalAddress = inetAddress.getHostAddress();
				} catch (UnknownHostException e) {
				}
			}

			// If all else fails set to default port.
			if (mLocalPort == -1) {
				mLocalPort = SERVERPORT;
			}


			Thread desktopServerThread = new Thread(new FingerPlayServer());
			desktopServerThread.start();
	}
/*
	public void showNetworkInterfaces(IView view) {
		try {
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
			while ( interfaces.hasMoreElements() ) {
				NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
				Enumeration addresses = ni.getInetAddresses();
				view.print( ni.toString() );
				//System.out.println(ni);
			}
		} catch (SocketException e) {
			System.out.println(e);
		}
	}
*/
}
