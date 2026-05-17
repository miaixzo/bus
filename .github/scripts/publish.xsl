<?xml version="1.0" encoding="UTF-8"?>
<!--
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
-->
<xsl:stylesheet version="1.0"
        xmlns:m="http://maven.apache.org/POM/4.0.0"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        exclude-result-prefixes="m xalan">
    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" />
    <xsl:strip-space elements="*" />

    <xsl:template match="/">
        <xsl:text>&#10;</xsl:text>
        <xsl:for-each select="node()[not(self::m:project)]">
            <xsl:apply-templates select="." />
            <xsl:text>&#10;</xsl:text>
        </xsl:for-each>
        <xsl:apply-templates select="m:project" />
    </xsl:template>

    <xsl:template match="m:project">
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            <xsl:for-each select="node()[not(self::text()[normalize-space() = ''])]">
                <xsl:apply-templates select="." />
            </xsl:for-each>
        </project>
    </xsl:template>

    <xsl:template match="m:*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:copy />
    </xsl:template>

    <xsl:template match="comment() | processing-instruction()">
        <xsl:copy />
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>
</xsl:stylesheet>
