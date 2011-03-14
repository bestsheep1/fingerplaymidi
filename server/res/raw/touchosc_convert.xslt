<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
<xsl:attribute-set name="widget_parameters">
﻿  <xsl:attribute name="x"><xsl:value-of select="@x" /></xsl:attribute>
﻿  <xsl:attribute name="y"><xsl:value-of select="@y" /></xsl:attribute>
﻿  <xsl:attribute name="width"><xsl:value-of select="@w" /></xsl:attribute>
﻿  <xsl:attribute name="height"><xsl:value-of select="@h" /></xsl:attribute>
<!--﻿  
﻿  <xsl:attribute name="channel" />
﻿  <xsl:attribute name="controllerNumber" />
-->
</xsl:attribute-set>
<xsl:attribute-set name="widget_parameters_inverted">
﻿  <xsl:attribute name="x"><xsl:value-of select="@y" /></xsl:attribute>
﻿  <xsl:attribute name="y"><xsl:value-of select="@x" /></xsl:attribute>
﻿  <xsl:attribute name="width"><xsl:value-of select="@h" /></xsl:attribute>
﻿  <xsl:attribute name="height"><xsl:value-of select="@w" /></xsl:attribute>
<!--﻿  
﻿  <xsl:attribute name="channel" />
﻿  <xsl:attribute name="controllerNumber" />
-->
</xsl:attribute-set>
<xsl:template match="/">

<layouts version="2">
<!--
		Defaults section

		Specifies the default value for all controllers in the layout file. All values
		can be, and should be, overriden.
		
		A controller's parameters don't have to be specified in the defaults section. 

		<id> An identifier for the controller parameter. 0 = Press for example.
		<channel> Midi channel.
		<controllerNumber> controller index? .. ???
		<name> Name to show in the settings page.
		<visible> Specifies if the parameter is visible and sendable from the settings page.  

 -->
<defaults>
	<xypad controller="com.flat20.fingerplay.midicontrollers.MidiController" view="com.flat20.gui.widgets.XYPad">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="controlChange" visible="true" />
		<parameter id="1" channel="0" controllerNumber="0" name="Horizontal" type="controlChange" visible="true" />
		<parameter id="2" channel="0" controllerNumber="0" name="Vertical" type="controlChange" visible="true" />
	</xypad>
	<slider controller="com.flat20.fingerplay.midicontrollers.MidiController" view="com.flat20.gui.widgets.Slider">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="controlChange" visible="true" />
		<parameter id="1" channel="0" controllerNumber="0" name="Vertical" type="controlChange" visible="true" />
	</slider>
	<pad controller="com.flat20.fingerplay.midicontrollers.MidiController" view="com.flat20.gui.widgets.Pad">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="note" visible="false" usePressure="true" />
	</pad>
	<rotaryencoderabsolute controller="com.flat20.fingerplay.midicontrollers.MidiController" view="com.flat20.gui.widgets.RotaryEncoderAbsolute">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="controlChange" visible="true" usePressure="true" />
		<parameter id="1" channel="0" controllerNumber="0" name="Vertical" type="controlChange" visible="true" />
	</rotaryencoderabsolute>
	<rotaryencoderrelative controller="com.flat20.fingerplay.midicontrollers.MidiController" view="com.flat20.gui.widgets.RotaryEncoderRelative">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="controlChange" visible="true" />
		<parameter id="1" channel="0" controllerNumber="0" name="Vertical" type="controlChange" visible="true" />
	</rotaryencoderrelative>

	<!-- Sensor controllers are "under construction" -->
	<orientation view="com.flat20.gui.widgets.SensorXYPad">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="controlChange" visible="true" />
		<parameter id="1" channel="0" controllerNumber="0" name="Horizontal" type="controlChange" visible="true" />
		<parameter id="2" channel="0" controllerNumber="0" name="Vertical" type="controlChange" visible="true" />
	</orientation>
	<proximity view="com.flat20.gui.widgets.SensorSlider">
		<parameter id="0" channel="0" controllerNumber="0" name="Press" type="controlChange" visible="true" />
		<parameter id="1" channel="0" controllerNumber="0" name="Vertical" type="controlChange" visible="true" />
	</proximity>
</defaults>

<layout screenWidth="480" screenHeight="320">
<xsl:for-each select="/layout/tabpage">
<screen>
  <xsl:for-each select="control">
﻿  <xsl:if test="@type = 'faderh'">
﻿  ﻿  <xsl:element name="slider" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
﻿  <xsl:if test="@type = 'faderv'">
﻿  ﻿  <xsl:element name="slider" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
﻿  <xsl:if test="@type = 'toggle'">
﻿  ﻿  <xsl:element name="pad" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
﻿  <xsl:if test="@type = 'push'">
﻿  ﻿  <xsl:element name="pad" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
﻿  <xsl:if test="@type = 'xy'">
﻿  ﻿  <xsl:element name="xypad" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
﻿  <xsl:if test="@type = 'rotaryv'">
﻿  ﻿  <xsl:element name="rotaryencoderabsolute" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
﻿  <xsl:if test="@type = 'rotaryh'">
﻿  ﻿  <xsl:element name="rotaryencoderabsolute" use-attribute-sets="widget_parameters_inverted" />
﻿  </xsl:if >
  </xsl:for-each>
</screen>
</xsl:for-each>
</layout>
</layouts>
</xsl:template>
</xsl:stylesheet>
