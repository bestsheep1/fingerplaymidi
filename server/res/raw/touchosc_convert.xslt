<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
<xsl:attribute-set
name="widget_parameters">
	<xsl:attribute name="x">
		<xsl:value-of select="@x" />
	</xsl:attribute>
	<xsl:attribute name="y">
		<xsl:value-of select="@y" />
	</xsl:attribute>
	<xsl:attribute name="width">
		<xsl:value-of select="@y" />
	</xsl:attribute>
	<xsl:attribute name="height">
		<xsl:value-of select="@h" />
	</xsl:attribute>
<!--	
	<xsl:attribute name="channel" />
	<xsl:attribute name="controllerNumber" />
-->
</xsl:attribute-set>
<xsl:template match="/">
<layouts version="2">
<layout screenWidth="480" screenHeight="320">
<xsl:for-each select="/layout/tabpage">
<screen>
  <xsl:for-each select="control">
	<xsl:if test="@type = 'faderh'">
		<xsl:element name="slider" 		use-attribute-sets="widget_parameters" />
	</xsl:if >
	<xsl:if test="@type = 'faderv'">
		<xsl:element name="slider" 				use-attribute-sets="widget_parameters" />
	</xsl:if >
	<xsl:if test="@type = 'toggle'">
		<xsl:element name="pad" 						use-attribute-sets="widget_parameters" />
	</xsl:if >
	<xsl:if test="@type = 'push'">
		<xsl:element name="slider" 						use-attribute-sets="widget_parameters" />
	</xsl:if >
	<xsl:if test="@type = 'xy'">
		<xsl:element name="xypad" 						use-attribute-sets="widget_parameters" />
	</xsl:if >
  </xsl:for-each>
</screen>
</xsl:for-each>
</layout>
</layouts>
</xsl:template>
</xsl:stylesheet>
