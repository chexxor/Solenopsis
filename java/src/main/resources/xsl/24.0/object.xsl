<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : object.xsl
    Created on : March 31, 2012, 2:40 PM
    Author     : sfloess
    Description:
        Convert SFDC object XML into Java properties file.  Good for SFDC API 24.0.
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:sfdc="http://soap.sforce.com/2006/04/metadata">
    
    <!-- ====================================================================== -->
    
    <!--  Function to output key-value pair.  -->
    <xsl:template name="EmitProperty">
        <xsl:param name="pPROPERTY_NAME"/>
        <xsl:param name="pVALUE"/>
        
        <xsl:value-of select="$pPROPERTY_NAME"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="$pVALUE"/>
        <xsl:text>&#xa;</xsl:text><!--Newline-->
    </xsl:template>
    <!--  END Key-value function.  -->
    
    <!--  Function to output in Csv format. -->
    <xsl:template name="EmitCsv">
        <xsl:param name="pNODESET"/>
        
        <xsl:for-each select="$pNODESET">
            <xsl:if test="position() != last()">"<xsl:value-of select="normalize-space(.)"/>", </xsl:if>
            <xsl:if test="position()  = last()">"<xsl:value-of select="normalize-space(.)"/>"<xsl:text>&#xD;</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <!--  END Csv function. -->
    
    <!--  Function to output values we care about from a nodeset.  -->
    <xsl:template mode="Values" match="sfdc:actionName | sfdc:fullName">
        <xsl:for-each select=".">
            <xsl:if test="position() != last()">"<xsl:value-of select="normalize-space(.)"/>", </xsl:if>
            <xsl:if test="position()  = last()">"<xsl:value-of select="normalize-space(.)"/>"<xsl:text>&#xD;</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <!--  END Value function.  -->
    
    <!-- ====================================================================== -->

    <xsl:output method="text" version="1.0" indent="no"/>

    <xsl:template match="* | @* | text()"/>
    
    <!-- ====================================================================== -->

    <!--  Match root. For efficieny, all other templates in modes.  -->
    <xsl:template match="/">
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:actionOverrides   [sfdc:actionName != '']]" mode="ActionOverrideProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:businessProcesses [sfdc:fullName   != '']]" mode="BusinessProcessProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:fields            [sfdc:fullName   != '']]" mode="CustomFieldProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:listViews         [sfdc:fullName   != '']]" mode="ListViewProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:namedFilters      [sfdc:fullName   != '']]" mode="NamedFilterProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:recordTypes       [sfdc:fullName   != '']]" mode="RecordTypeProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:validationRules   [sfdc:fullName   != '']]" mode="ValidationRuleProperty"/>
        <xsl:apply-templates select="sfdc:CustomObject [sfdc:webLinks          [sfdc:fullName   != '']]" mode="WebLinkProperty"/> 
    </xsl:template>
    <!--  END Match root element.  -->
    
    <!-- ====================================================================== -->
    
    <!--  Elements or attributes in file to use as key.  -->
    <xsl:template mode="ActionOverrideProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'ActionOverride'"/>
            <xsl:with-param name="pVALUE">
<!--                <xsl:apply-templates mode="Values" select="sfdc:actionOverrides/sfdc:actionName"/>-->
<!--                <xsl:apply-templates mode="Values" select="sfdc:actionOverrides"/>-->
                <xsl:call-template name="EmitCsv">
                    <xsl:with-param name="pNODESET" select="sfdc:actionOverrides/sfdc:actionName"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template mode="BusinessProcessProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'BusinessProcess'"/>
            <xsl:with-param name="pVALUE">
                <xsl:apply-templates mode="Values" select="sfdc:businessProcesses/sfdc:fullName"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="CustomFieldProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'CustomField'"/>
            <xsl:with-param name="pVALUE">
<!--                <xsl:apply-templates mode="Values" select="sfdc:fields/sfdc:fullName"/>-->
                <xsl:call-template name="EmitCsv">
                    <xsl:with-param name="pNODESET" select="sfdc:fields/sfdc:fullName"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>   
    </xsl:template>
    
    <xsl:template mode="ListViewProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'ListView'"/>
            <xsl:with-param name="pVALUE">
                <xsl:apply-templates mode="Values" select="sfdc:listViews/sfdc:fullName"/>
            </xsl:with-param>
        </xsl:call-template>               
    </xsl:template>
    
    <xsl:template mode="NamedFilterProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'NamedFilter'"/>
            <xsl:with-param name="pVALUE">
                <xsl:apply-templates mode="Values" select="sfdc:namedFilters/sfdc:fullName"/>
            </xsl:with-param>
        </xsl:call-template>                
    </xsl:template>
    
    <xsl:template mode="RecordTypeProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'RecordType'"/>
            <xsl:with-param name="pVALUE">
                <xsl:apply-templates mode="Values" select="sfdc:recordTypes/sfdc:fullName"/>
            </xsl:with-param>
        </xsl:call-template>               
    </xsl:template>
    
    <xsl:template mode="ValidationRuleProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'ValidationRule'"/>
            <xsl:with-param name="pVALUE">
                <xsl:apply-templates mode="Values" select="sfdc:validationRules/sfdc:fullName"/>
            </xsl:with-param>
        </xsl:call-template>        
    </xsl:template>
    
    <xsl:template mode="WebLinkProperty" match="sfdc:CustomObject">
        <xsl:call-template name="EmitProperty">
            <xsl:with-param name="pPROPERTY_NAME" select="'WebLink'"/>
            <xsl:with-param name="pVALUE">
                <xsl:apply-templates mode="Values" select="sfdc:webLinks/sfdc:fullName"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!--  END Elements or attributes in file to use as key.  -->
    
</xsl:stylesheet>
