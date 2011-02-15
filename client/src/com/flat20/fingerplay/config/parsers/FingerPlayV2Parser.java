package com.flat20.fingerplay.config.parsers;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.flat20.fingerplay.config.XMLUtils;
import com.flat20.fingerplay.config.dto.ConfigItem;
import com.flat20.fingerplay.config.dto.ConfigItemParameters;
import com.flat20.fingerplay.config.dto.ConfigLayout;
import com.flat20.fingerplay.config.dto.ConfigScreen;

/**
 * Reads and parses layout XML files into ControllerInfo classes
 * @author andreas.reuterberg
 *
 */
public class FingerPlayV2Parser implements IParser {

	private Document mXmlDoc;

	// Version number found on the root <layouts> tag
	private int mConfigFileVersion;

	// HashMap with widget name and its default values. Gets cloned for each new Widget found.
	private HashMap<String, ConfigItem> mDefaultItems;

	// Where do we put these?
	int numTouchPads = 0;
	int numSliders = 0;
	int numButtons = 0;


	public FingerPlayV2Parser() {
		
	}

	public void setInput(InputStream xmlStream) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		init( db.parse(xmlStream) );
	}

	public void setInput(File xmlFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		init( db.parse( xmlFile ) );
	}

	private void init(Document xmlDoc) throws Exception {

		mXmlDoc = xmlDoc;
		mXmlDoc.getDocumentElement().normalize();

		Element root = mXmlDoc.getDocumentElement();
		if ( !root.getNodeName().equals("layouts") )
			throw new Exception("Root element of XML was not <layouts>");

		// Get the version number if it's set, default to 1 otherwise.
		mConfigFileVersion = XMLUtils.getIntegerAttribute(root, "version", 1);

		// Load all Widget definitions and their default settings.
		mDefaultItems = parseDefinitions();
	}

	/**
	 * Parses the 'defaults' tag, which we should rename to definitions.
	 * 
	 * @return
	 */
	private HashMap<String, ConfigItem> parseDefinitions() {
		// Get all controller definitions and their default values

		HashMap<String, ConfigItem> defaultParameters = new HashMap<String, ConfigItem>();

		NodeList defaults = mXmlDoc.getElementsByTagName("defaults");
		for (int l = 0; l < defaults.getLength(); l++) {
			if (defaults.item(l).getNodeType() == Node.ELEMENT_NODE) {
				Element tempDefault = (Element) defaults.item(l);

				// loop through defaults/controllers
				NodeList controllers = tempDefault.getChildNodes();
				for (int i = 0; i < controllers.getLength(); i++) {
					if (controllers.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element controller = (Element) controllers.item(i);

						ConfigItem defaultItem = new ConfigItem();
						defaultItem.tagName = controller.getNodeName();
						defaultItem.controllerClassName = XMLUtils.getStringAttribute(controller, "controller", null);
						defaultItem.viewClassName = XMLUtils.getStringAttribute(controller, "view", null);
						defaultItem.displayName = "Default";
						defaultItem.parameters = new ConfigItemParameters();

						// TODO Parse the default parameters and their values first from our hardcoded ones,
						// and then from the XML. But to get hardcoded values from the class we'd need to load
						// the class and loading isn't done in ConfigReader.

						parseParameters(defaultItem, controller);

						// Put it in a HashMap by tagName.
						defaultParameters.put(defaultItem.tagName, defaultItem);
					}
				}
			}
		}

		return defaultParameters;

	}


	/**
	 * Selects the layout from the XML Document which best fits the
	 * specified width and height.
	 * 
	 * Perhaps this function should be moved outside of ConfigReader? 
	 * 
	 * @param width		Ideal width
	 * @param height	Ideal height
	 */
	public ConfigLayout selectLayout(int width, int height) throws Exception {

		ConfigLayout configLayout = new ConfigLayout();
		configLayout.ID = -1;

		// Get the correct layout for the current resolution
		NodeList layouts = mXmlDoc.getElementsByTagName("layout");
		int bestDiff = 10000;

		for (int i = 0; i < layouts.getLength(); i++) {
			//if (layouts.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element tempLayout = (Element) layouts.item(i);
				int layoutWidth = XMLUtils.getIntegerAttribute(tempLayout, "screenWidth");
				int layoutHeight = XMLUtils.getIntegerAttribute(tempLayout, "screenHeight");
				int diff = Math.max(Math.abs(width - layoutWidth), Math.abs(height - layoutHeight));
				if (diff < bestDiff) {
					bestDiff = diff;
					configLayout.ID = i;
					configLayout.width = layoutWidth;
					configLayout.height = layoutHeight;
				}
			//}
		}

		// Couldn't find any layout
		if (configLayout.ID == -1)
			throw new Exception("Unable to find a suitable layout for w: " + width + " h: " + height);

		return configLayout;
	}

	/**
	 * Fills the ConfigLayout with information parsed from the XML file.
	 * 
	 * @param layout
	 * @return
	 * @throws Exception
	 */
	public void parseLayout(ConfigLayout configLayout) throws Exception {

		// Get all layout tags and select the one specified by ConfigLayout ID.
		NodeList layouts = mXmlDoc.getElementsByTagName("layout");
		Element layout = (Element) layouts.item(configLayout.ID);
		NodeList screenElements = layout.getElementsByTagName("screen");

		// Get width & height
		configLayout.width = XMLUtils.getIntegerAttribute(layout, "screenWidth");
		configLayout.height = XMLUtils.getIntegerAttribute(layout, "screenHeight");

		final int length = screenElements.getLength();
		for (int s = 0; s < length; s++) {

			Element screenElement = (Element) screenElements.item(s);

			ConfigScreen configScreen = new ConfigScreen();
			configScreen.x = XMLUtils.getIntegerAttribute(screenElement, "x");
			configScreen.y = XMLUtils.getIntegerAttribute(screenElement, "y");
			configScreen.width = XMLUtils.getIntegerAttribute(screenElement, "width");
			configScreen.height = XMLUtils.getIntegerAttribute(screenElement, "height");

			NodeList widgets = screenElement.getChildNodes();
			final int wlength =  widgets.getLength();
			for (int e = 0; e < wlength; e++) {

				if (widgets.item(e).getNodeType() == Node.ELEMENT_NODE) {

					Element widgetElement = (Element) widgets.item(e);
					ConfigItem configItem = parseWidget(widgetElement);
					configScreen.items.add(configItem);
					//System.out.println(configItem);
				}
			}

			configLayout.screens.add(configScreen);
		}

	}

	/**
	 * Parses an individual Widget XML tag in the layout and returns
	 * a ConfigItem.
	 * 
	 * TODO Move class creation outside of ConfigReader.
	 * 
	 * @param widgetElement
	 */
	private ConfigItem parseWidget(Element widgetElement) throws Exception {

		String elementName = widgetElement.getNodeName();

		ConfigItem configWidget = null;
		ConfigItem defaultItem = mDefaultItems.get(elementName);

		configWidget = defaultItem.clone();


		if (configWidget != null) {

			configWidget.tagName = elementName;
			configWidget.displayName = elementName + " " + (++numButtons);
			configWidget.x = XMLUtils.getIntegerAttribute(widgetElement, "x");
			configWidget.y = XMLUtils.getIntegerAttribute(widgetElement, "y");
			configWidget.width = XMLUtils.getIntegerAttribute(widgetElement, "width");
			configWidget.height = XMLUtils.getIntegerAttribute(widgetElement, "height");

			// Load any XML parameters
			parseParameters( configWidget, widgetElement );
		}

		// Returns null if widget was null
		return configWidget;
	}


	/**
	 * Loads all parameters defined inside the itemElement.
	 * It will override the parameters found in the defaults section.
	 *  
	 * 
	 * @param item
	 * @param itemElement
	 */
	private void parseParameters(ConfigItem item, Element itemElement) {

		NodeList parameterElements = itemElement.getElementsByTagName("parameter");
		final int length = parameterElements.getLength();

		for (int e = 0; e < length; e++) {
			Element parameterElement = (Element) parameterElements.item(e);
			NamedNodeMap attributes = parameterElement.getAttributes();

			int id = Integer.parseInt((String)attributes.getNamedItem("id").getNodeValue());

			// Load old parameter value if it's been set in the defaults section.
			HashMap<String, Object> parameters = item.parameters.getParameterById(id);

			if (parameters == null) {
				parameters = new HashMap<String, Object>();
				item.parameters.data.add(parameters);
			}

			// Update parameter with attributes from the XML tag.
			for (int a=0; a < attributes.getLength(); a++) {
				Node node = attributes.item(a);
				parameters.put(node.getNodeName(), node.getNodeValue());
			}

		}
	}

}
