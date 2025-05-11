<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xj="http://camel.apache.org/component/xj"
                exclude-result-prefixes="xj">

    <xsl:output omit-xml-declaration="no" encoding="UTF-8" method="xml" indent="yes"/>

    <!-- Parameters with defaults -->
    <xsl:param name="creationDateTime" select="'2023-01-01T00:00:00'"/> <!-- Will be overridden -->
    <xsl:param name="baseLanguage" select="'EN'"/>
    <xsl:param name="version" select="'1'"/>

    <xsl:template match="/">
        <Sync-Request creationDateTime="{$creationDateTime}" baseLanguage="{$baseLanguage}" version="{$version}">
            <RequestSet>
                <Request>
                    <Name>
                        <xsl:value-of select="//object[@xj:name='id']"/>
                    </Name>
                    <Description>
                        <xsl:value-of select="//object[@xj:name='description']"/>
                    </Description>
                    <Start-Date>
                        <xsl:value-of select="//object[@xj:name='start']"/>
                    </Start-Date>
                    <End-Date>
                        <xsl:value-of select="//object[@xj:name='end']"/>
                    </End-Date>

                    <Items>
                        <!-- Generate multiple <Value> for each item -->
                        <xsl:for-each select="//object[@xj:name='workOrders']/object">
                            <Value>
                                <xsl:value-of select="."/>
                            </Value>
                        </xsl:for-each>
                    </Items>

                </Request>
            </RequestSet>
        </Sync-Request>
    </xsl:template>

</xsl:stylesheet>
