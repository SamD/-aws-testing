<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Change this to specify the number of elements per output file -->
    <xsl:param name="elementsPerFile" select="3"/>

    <xsl:template match="/">
        <xsl:variable name="totalElements" select="count(//*[not(self::text())])"/>
        <xsl:for-each select="//*[position() mod $elementsPerFile = 1]">
            <!-- Create a new document for every N elements -->
            <xsl:result-document href="output_{position()}.xml">
                <xsl:for-each select=". | following-sibling::*[position() &lt; $elementsPerFile]">
                    <xsl:copy-of select="."/>
                </xsl:for-each>
            </xsl:result-document>
        </xsl:for-each>
        <!-- Handle the remaining elements in a separate file -->
        <xsl:result-document href="output_{ceiling($totalElements div $elementsPerFile)}.xml">
            <xsl:for-each select="//*[position() > $totalElements - $totalElements mod $elementsPerFile]">
                <xsl:copy-of select="."/>
            </xsl:for-each>
        </xsl:result-document>
    </xsl:template>

</xsl:stylesheet>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output omit-xml-declaration="yes" indent="yes"/>
<xsl:param name="elementCount" select="3"/>

<xsl:template match="/*">
    <xsl:for-each-group select="*" group-by="(position() -1) idiv $elementCount">
        <group>
            <xsl:sequence select="current-group()"/>
        </group>
    </xsl:for-each-group>
</xsl:template>
</xsl:stylesheet>

        <!-- split.xsl -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Change this to specify the number of elements per output file -->
<xsl:param name="elementsPerFile" select="3"/>

<xsl:template match="/">
    <xsl:variable name="totalElements" select="count(//*[not(self::text())])"/>
    <xsl:variable name="numFiles" select="$totalElements idiv $elementsPerFile"/>

    <xsl:for-each select="1 to $numFiles">
        <xsl:variable name="startIndex" select="(position() - 1) * $elementsPerFile + 1"/>
        <xsl:variable name="endIndex" select="position() * $elementsPerFile"/>

        <!-- Check if we're creating the last file -->
        <xsl:variable name="isLastFile" select="position() = $numFiles"/>

        <xsl:result-document href="output_{position()}.xml">
            <xsl:apply-templates select="(//*[not(self::text())])[position() &gt;= $startIndex and position() &lt;= $endIndex]"/>

            <!-- If it's the last file, ensure it only contains remaining elements -->
            <xsl:if test="$isLastFile">
                <xsl:apply-templates select="(//*[not(self::text())])[position() > $totalElements - $totalElements mod $elementsPerFile]"/>
            </xsl:if>
        </xsl:result-document>
    </xsl:for-each>
</xsl:template>

<!-- Identity transform to copy elements -->
<xsl:template match="*">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>
</xsl:stylesheet>
