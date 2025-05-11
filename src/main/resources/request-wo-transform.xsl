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
        <Sync-WoRequest creationDateTime="{$creationDateTime}" baseLanguage="{$baseLanguage}" version="{$version}">
            <RequestSet>

                <!-- Add work orders -->
                <xsl:for-each select="/object/object[@xj:name='addedWorkOrders']/object">
                    <Request action="add">
                        <Name>
                            <xsl:value-of select="/object/object[@xj:name='id']"/>
                        </Name>
                        <WorkOrder>
                            <xsl:value-of select="."/>
                        </WorkOrder>
                    </Request>
                </xsl:for-each>

                <!-- Delete work orders -->
                <xsl:for-each select="/object/object[@xj:name='deletedWorkOrders']/object">
                    <Request action="delete">
                        <Name>
                            <xsl:value-of select="/object/object[@xj:name='id']"/>
                        </Name>
                        <WorkOrder>
                            <xsl:value-of select="."/>
                        </WorkOrder>
                    </Request>
                </xsl:for-each>

            </RequestSet>
        </Sync-WoRequest>
    </xsl:template>
</xsl:stylesheet>
