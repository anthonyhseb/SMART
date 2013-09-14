<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <root>
            <xsl:variable name="counter" select="0"/>
            <xsl:for-each select="//*[@label]">
                <xsl:element name="item">
                    <xsl:attribute name="id">
                        <xsl:value-of select ="./@id"/>
                    </xsl:attribute>
                    <xsl:attribute name="parent_id">
                        <xsl:value-of select ="../@id"/>
                    </xsl:attribute>
                    <xsl:element name="content">
                        <xsl:element name="name">
                            <xsl:value-of select ="@label"/>
                            <xsl:if test="./@type = 'datap'">: <b><xsl:value-of select="current()"/></b>
                            </xsl:if>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
                <xsl:if test="name(.) = 'Location'">
                    <xsl:copy>
                        <xsl:attribute name="parent_id">
                            <xsl:value-of select ="../../@id"/>
                        </xsl:attribute>
                        <xsl:attribute name="id">
                            <xsl:value-of select ="./@id"/>_<xsl:value-of select ="../../*[1]"/>
                        </xsl:attribute>
                        <xsl:element name="latitude">
                            <xsl:value-of select="./latitude"/>
                        </xsl:element>
                        <xsl:element name="longitude">
                            <xsl:value-of select="./longitude"/>
                        </xsl:element>
                        <xsl:element name="title">
                            <xsl:value-of select="../../*[1]"/>
                        </xsl:element>                        
                    </xsl:copy>
                </xsl:if>
                <xsl:if test="name(.) = 'GeoMeasurement'">
                    <xsl:copy>
                        <xsl:attribute name="parent_id">
                            <xsl:value-of select ="../../@id"/>
                        </xsl:attribute>
                        <xsl:attribute name="id">
                            <xsl:value-of select ="./@id"/>_<xsl:value-of select ="../../*[1]"/>
                        </xsl:attribute>
                        <xsl:element name="latitude">
                            <xsl:value-of select="./measurementLatitude"/>
                        </xsl:element>
                        <xsl:element name="longitude">
                            <xsl:value-of select="./measurementLongitude"/>
                        </xsl:element>
                        <xsl:element name="value">
                            <xsl:value-of select="./value"/>
                        </xsl:element>
                        <xsl:element name="unit">
                            <xsl:value-of select="./unit"/>
                        </xsl:element>
                        <xsl:element name="timestamp">
                            <xsl:value-of select="./timestamp"/>
                        </xsl:element>
                        <xsl:element name="provider">
                            <xsl:value-of select="../../../../*[1]"/>
                        </xsl:element>                          
                    </xsl:copy>
                </xsl:if>
            </xsl:for-each>
        </root>
    </xsl:template>

</xsl:stylesheet>