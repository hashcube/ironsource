  <xsl:template match="meta-data[@android:name='admobAppId']">
    <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="{$admobAppId}"/>
  </xsl:template>