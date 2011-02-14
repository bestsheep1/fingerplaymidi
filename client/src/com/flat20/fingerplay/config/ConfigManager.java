package com.flat20.fingerplay.config;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import android.os.Environment;

import com.flat20.fingerplay.config.dto.ConfigItem;
import com.flat20.fingerplay.config.dto.ConfigLayout;
import com.flat20.fingerplay.config.dto.ConfigScreen;
import com.flat20.fingerplay.config.parsers.FingerPlayV2Parser;
import com.flat20.fingerplay.config.parsers.IParser;
import com.flat20.fingerplay.settings.SettingsModel;

/**
 * Will parse the config and notify listeners (view, MidiControllerManager).
 *  
 * @author andreas.reuterberg
 *
 */
public class ConfigManager {

	private SettingsModel mSettingsModel;

    private ArrayList<IConfigUpdateListener> mListeners;

    private InputStream mDefaultConfigXml = null;
    private int mWidth;
    private int mHeight;


	// Singleton
	private static ConfigManager mInstance = null;
	public static ConfigManager getInstance() {
		if (mInstance == null)
			mInstance = new ConfigManager();
		return mInstance;
	}

	private ConfigManager() {
		//mConnectionManager.addConnectionListener(mConnectionListener);
		mSettingsModel = SettingsModel.getInstance();
		mListeners = new ArrayList<IConfigUpdateListener>();
	}

	public void addListener(IConfigUpdateListener listener) {
		mListeners.add(listener);
	}

	public void setDefaultConfigXml(InputStream defaultConfigXml) {
		mDefaultConfigXml = defaultConfigXml; 
	}
	
	public void setScreenSize(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	// TODO Make parser settable from the outside.
	//public void setParser(IParser parser) {
	//}

	private IParser createParser() {
		File xmlFile;
        IParser parser = null;		
		if (!mSettingsModel.layoutFile.equals("Default")) {
			xmlFile = new File(Environment.getExternalStorageDirectory() + "/FingerPlayMIDI/" + mSettingsModel.layoutFile);
	
	        try {
	        	if (mSettingsModel.layoutFile != null && xmlFile != null) {
	    			parser = new FingerPlayV2Parser();
	    			parser.setInput(xmlFile);
	        	}
			} catch (Exception e) {
				// Tried loading and parsing file but failed. Most likely the file wasn't there.
				System.out.println(e);
			}
		}
		
		try {
			if (parser == null && mDefaultConfigXml != null) {
				mDefaultConfigXml.reset();
				parser = new FingerPlayV2Parser();
				parser.setInput( mDefaultConfigXml );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parser;
	}
	/*
	private ConfigReader getReader() {

		File xmlFile = new File(Environment.getExternalStorageDirectory() + "/FingerPlayMIDI/" + mSettingsModel.layoutFile);

        ConfigReader reader = null;
        try {
        	if (mSettingsModel.layoutFile != null && xmlFile != null) {
    			reader = new ConfigReader( xmlFile );
        	}
		} catch (Exception e) {
			// Tried loading and parsing file but failed. Most likely the file wasn't there.
			System.out.println(e);
		}

		try {
			if (reader == null && mDefaultConfigXml != null) {
				mDefaultConfigXml.reset();
				reader = new ConfigReader( mDefaultConfigXml );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reader;
	}*/

	public void updateConfig() throws Exception {

		IParser parser = createParser();
		if (parser == null)
			throw new Exception("No config file to parse!");

		try {

			// Fills ConfigLayout with all info and controllers from
			// the config file.

			ConfigLayout layout = parser.selectLayout(mWidth, mHeight);
			parser.parseLayout(layout); 


			// Instantiate controller and view classes.

	        for (ConfigScreen screen : layout.screens) {
	        	for (ConfigItem configItem : screen.items) {
	        		
	        		// Load the controller class.

					Class<?> ControllerClass = Class.forName( configItem.controllerClassName );
					Class<?>[] classParams = new Class<?>[] {};
					Object[] objectParams = new Object[] {};
					Constructor<?> ctor = ControllerClass.getConstructor( classParams );
					IConfigurable controller = (IConfigurable) ctor.newInstance( objectParams );

					configItem.itemController = controller;
					configItem.itemController.setParameters(configItem.parameters);


					// Load the view class if this controller has one.

					if (configItem.viewClassName != null) {

						Class<?> ViewClass = Class.forName( configItem.viewClassName );
						Class<?>[] viewClassParams = new Class<?>[] {};
						Object[] viewObjectParams = new Object[] {};
						Constructor<?> viewCtor = ViewClass.getConstructor( viewClassParams );
						IConfigItemView view = (IConfigItemView) viewCtor.newInstance( viewObjectParams );

						configItem.itemView = view;
						configItem.itemView.setController( configItem.itemController );

					}

	        	}
	        }


	        // Notify listeners that the config has been parsed and updated.

	        for (IConfigUpdateListener listener : mListeners) {
	        	listener.onConfigUpdated(layout);
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
