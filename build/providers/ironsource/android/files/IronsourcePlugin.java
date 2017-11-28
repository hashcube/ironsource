package com.tealeaf.plugin.plugins;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import com.tealeaf.EventQueue;
import com.tealeaf.TeaLeaf;
import com.tealeaf.logger;
import com.tealeaf.event.*;
import com.tealeaf.plugin.IPlugin;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;

import com.ironsource.adapters.supersonicads.SupersonicConfig;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.sdk.SSAFactory;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

public class IronsourcePlugin implements IPlugin {

  Context _ctx = null;
  Activity _activity = null;
  String appKey = "";
  private IronSource mIronsourceInstance;
  private IronsourceListener listener = null;

  private Placement mPlacement;
  private Integer rewardedCount = 0;

  public class IronsourceListener implements InterstitialListener, RewardedVideoListener, OfferwallListener {
    /************************************************************
     *         Ironsource Interstitial Listeners
     ************************************************************
     */

    /**
     * Invoked when Interstitial initialization process completes successfully.
     */
    public void onInterstitialInitSuccess() {
      logger.log("{ironsource} onInterstitialInitSuccess");
    }

    /**
     * Invoked when Interstitial initialization process is failed.
     * @param ironSourceError - An Object which represents the reason of initialization failure.
     */
    public void onInterstitialInitFailed(IronSourceError ironSourceError) {
      logger.log("{ironsource} onInterstitialInitFail", ironSourceError);
    }

    /*
     * Invoked when the interstial ad clicked
     */
    @Override
    public void onInterstitialAdClicked() {
        // called when the interstitial has been clicked
        logger.log("{ironsource} onInterstitialAdClicked");
    }

    /*
     * Invoked when the interstial ad showing failed
     */
    @Override
    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
        logger.log("{ironsource} onInterstitialAdShowFailed", ironSourceError);
        EventQueue.pushEvent(new IronsourceAdNotAvailable());
    }

    /*
     * Invoked when the interstial ad showing succeeded
     */
    @Override
    public void onInterstitialAdShowSucceeded() {
        logger.log("{ironsource} onInterstitialAdShowSucceeded");
    }

    /*
     * Invoked when the interstial ad close
     */
    @Override
    public void onInterstitialAdClosed() {
        logger.log("{ironsource} onInterstitialAdClosed");
    }

    /*
     * Invoked when the interstial ad is ready for showing
     */
    @Override
    public void onInterstitialAdReady() {
        logger.log("{ironsource} onInterstitialReady");
        EventQueue.pushEvent(new IronsourceAdAvailable());
    }

    /*
     * Invoked when the interstial ad failed.
     */
    @Override
    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
        logger.log("{ironsource} onInterstitialLoadFailed", ironSourceError);
        EventQueue.pushEvent(new IronsourceAdNotAvailable());
    }

    /*
     * Invoked when the ad was opened and shown successfully.
     */
    public void onInterstitialShowSuccess() {
      logger.log("{ironsource} onInterstitialShowSuccess");
    }

    /**
     * Invoked when Interstitial ad failed to show.
     * @param ironSourceError - An object which represents the reason of showInterstitial failure.
     */
    public void onInterstitialShowFailed(IronSourceError ironSourceError) {
      logger.log("{ironsource} onInterstitialShowFailed", ironSourceError);
    }

    @Override
    public void onInterstitialAdOpened() {
        logger.log("{ironsource} onInterstitialAdOpened");
    }

    /*
     * Invoked when the end user clicked on the interstitial ad.
     */
    public void onInterstitialClick() {
    }

    /*
     * Invoked when the ad is closed and the user is about to return to the application.
     */
    public void onInterstitialClose() {
      logger.log("{ironsource} onInterstitialClose");
      EventQueue.pushEvent(new IronsourceAdDismissed());
    }

    /**
      Invoked when the Interstitial Ad Unit is opened
     */
    public void onInterstitialOpen() {
    }

    /************************************************************
     *         Ironsource Offerwall Listeners
     ************************************************************
     */

    /*
     * Invoked when the Offerwal is open
     */
    @Override
    public void onOfferwallOpened() {
      logger.log("{ironsource} onOfferwallOpened");
    }

    /**
      * Invoked each time the user completes an Offer.
      * Award the user with the credit amount corresponding to the value of the ‘credits’
      * parameter.
      * @param credits - The number of credits the user has earned.
      * @param totalCredits - The total number of credits ever earned by the user.
      * @param totalCreditsFlag - In some cases, we won’t be able to provide the exact
      * amount of credits since the last event (specifically if the user clears
      * the app’s data). In this case the ‘credits’ will be equal to the ‘totalCredits’,
      * and this flag will be ‘true’.
      * @return boolean - true if you received the callback and rewarded the user,
      * otherwise false.
      */
    @Override
    public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
      logger.log("{ironsource} onOWAdCredited");
      EventQueue.pushEvent(new onOWAdCredited(credits));
      return true;
    }

     /*
     * Invoked when the Offerwal is available
     */
    @Override
    public void onOfferwallAvailable(boolean available) {
        logger.log("{ironsource} onOfferwallAvailable");
        EventQueue.pushEvent(new onOWAdAvailabilityChange(available));
    }

    /*
     * Invoked when the Offerwal show failed
     */
    @Override
    public void onOfferwallShowFailed(IronSourceError ironSourceError) {
        logger.log("{ironsource} onOfferwallShowFailed", ironSourceError);
    }

    /*
     * Invoked when the Offerwal credit failed
     */
    @Override
    public void onGetOfferwallCreditsFailed(IronSourceError ironSourceError) {
        logger.log("{ironsource} onGetOfferwallCreditsFailed", ironSourceError);
    }

    /**
      * Invoked when the user is about to return to the application after closing
      * the Offerwall.
      */
    @Override
    public void onOfferwallClosed() {
      logger.log("{ironsource} onOWAdClosed");
    }

    /*
     * Invoked when the reward video clicked
     */
    @Override
    public void onRewardedVideoAdClicked(Placement placement) {
      logger.log("{ironsource} onRewardedVideoAdClicked");
    }

    /*
     * Invoked when the reward video open
     */
    @Override
    public void onRewardedVideoAdOpened() {
      logger.log("{ironsource} onRewardedVideoAdOpened");
    }

    /*
     * Invoked when the reward video ad closed
     */
    @Override
    public void onRewardedVideoAdClosed() {
      logger.log("{ironsource} onRewardedVideoAdClosed");
      EventQueue.pushEvent(new onRVAdClosed(mPlacement, rewardedCount));
      mPlacement = null;
      rewardedCount = 0;
    }

    /*
     * Invoked when the reward video availablitiy change
     */
    @Override
    public void onRewardedVideoAvailabilityChanged(boolean available) {
        logger.log("{ironsource} onVideoAvailabilityChanged: ", available);
        EventQueue.pushEvent(new onRVAvailabilityChange(available));
    }

    /*
     * Invoked when the reward video ad started
     */
    @Override
    public void onRewardedVideoAdStarted() {
        logger.log("{ironsource} onVideoStart");
    }

    /*
     * Invoked when the reward video ad enabled
     */
    @Override
    public void onRewardedVideoAdEnded() {
        logger.log("{ironsource} onVideoEnd");
    }

    /*
     * Invoked when the reward video reward rewarded
     */
    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
      logger.log("{ironsource} onRewardedVideoAdRewarded");
      mPlacement = placement;
      rewardedCount++;
    }

    /*
     * Invoked when the reward video ad show failed
     */
    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
        logger.log("{ironsource} onRewardedVideoShowFail", ironSourceError);
    }
  }

  public class IronsourceAdNotAvailable extends Event {

    public IronsourceAdNotAvailable() {
      super("IronsourceAdNotAvailable");
    }
  }

  public class IronsourceAdAvailable extends Event {

    public IronsourceAdAvailable() {
      super("IronsourceAdAvailable");
    }
  }

  public class IronsourceAdDismissed extends Event {

    public IronsourceAdDismissed() {
      super("IronsourceAdDismissed");
    }
  }

  public class onOWAdCredited extends Event {
    int credits;

    public onOWAdCredited(int credits) {
      super("ironsourceOWCredited");
      logger.log("{ironsource} Credits received:", credits);
      this.credits = credits;
    }
  }

  public class onOWAdAvailabilityChange extends Event {
    boolean available;

    public onOWAdAvailabilityChange(boolean available) {
      super("ironsourceOnOWAvailabilityChange");
      logger.log("{ironsource} ironsourceOnOWAvailabilityChange:", available);
      this.available = available;
    }
  }

  public class onRVAdClosed extends Event {
    String placement = null;
    Integer rewardedCount = 0;

    public onRVAdClosed(Placement placement, Integer rewardedCount) {
      super("ironsourceRVAdClosed");
      if(placement != null) {
        this.placement = placement.getRewardName();
        this.rewardedCount = rewardedCount;
      }
      logger.log("{ironsource} RVAd rewarded received:", this.placement, this.rewardedCount);
    }
  }

  public class onRVAvailabilityChange extends Event {
    boolean available;

    public onRVAvailabilityChange(boolean available) {
      super("ironsourceOnRVAvailabilityChange");
      logger.log("{ironsource} ironsourceOnRVAvailabilityChange:", available);
      this.available = available;
    }
  }

  private String getUserId(String json) {
    String userId = "";

    try {
      JSONObject jsonObject = new JSONObject(json);
      userId = jsonObject.getString("user_id");
    } catch (Exception e) {
      logger.log("{ironsource} exception", e);
    }
    return userId;
  }

  public IronsourcePlugin() {
  }

  public void onCreateApplication(Context applicationContext) {
    _ctx = applicationContext;
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    PackageManager manager = activity.getBaseContext().getPackageManager();
    _activity = activity;

    try {
      Bundle meta = manager.getApplicationInfo(activity.getApplicationContext().getPackageName(),
          PackageManager.GET_META_DATA).metaData;
      appKey = meta.getString("ironsourceAppKey");
    } catch (Exception e) {
      logger.log("{ironsource} Exception while loading manifest keys:", e);
    }

    logger.log("{ironsource} Installing for appKey:", appKey);

    if(listener == null) {
      listener = new IronsourceListener();
    }
  }

  public void initInterstitial(String jsonData) {
    logger.log("{ironsource} Init Interstitial");

    SSAFactory.getAdvertiserInstance().reportAppStarted(_ctx);
    IronSource.setInterstitialListener(listener);
    SupersonicConfig.getConfigObj().setClientSideCallbacks(true);
    IronSource.setUserId(getUserId(jsonData));
    IronSource.init(_activity, appKey);
  }

  public void initVideoAd(String jsonData) {
    logger.log("{ironsource} Init video Ad");

    SSAFactory.getAdvertiserInstance().reportAppStarted(_activity);
    SupersonicConfig.getConfigObj().setClientSideCallbacks(true);
    IronSource.setRewardedVideoListener(listener);
    IronSource.setUserId(getUserId(jsonData));
    IronSource.init(_activity, appKey);
  }

  public void initOfferWallAd(String jsonData) {
    logger.log("{ironsource} Init offerwall Ad");

    SSAFactory.getAdvertiserInstance().reportAppStarted(_ctx);
    SupersonicConfig.getConfigObj().setClientSideCallbacks(true);
    IronSource.setOfferwallListener(listener);
    IronSource.setUserId(getUserId(jsonData));
    IronSource.init(_activity, appKey);
  }

  public void cacheInterstitial(String jsonData) {
    logger.log("{ironsource} loadInterstitial");
    _activity.runOnUiThread(new Runnable() {
      public void run() {
        IronSource.loadInterstitial();
      }
    });
  }

  public void showInterstitial(String jsonData) {
    logger.log("{ironsource} showInterstitial called");
    _activity.runOnUiThread(new Runnable() {
      public void run() {
        IronSource.showInterstitial();
      }
    });
  }

  public void showOffersForUserID(String jsonData) {
    logger.log("{ironsource} showOffers called");

    if(IronSource.isOfferwallAvailable()) {
      IronSource.showOfferwall();
    }
  }

  public void showRVAd(String jsonData) {
    if(IronSource.isRewardedVideoAvailable()) {
      IronSource.showRewardedVideo();
    }
  }

  public void onResume() {
    IronSource.onResume(_activity);
  }

  public void onRenderResume() {
  }

  public void onStart() {
  }

  public void onFirstRun() {
  }

  public void onPause() {
    IronSource.onPause(_activity);
  }

  public void onRenderPause() {
  }

  public void onStop() {
  }

  public void onDestroy() {
  }

  public void onNewIntent(Intent intent) {
  }

  public void setInstallReferrer(String referrer) {
  }

  public void onActivityResult(Integer request, Integer result, Intent data) {
  }

  public void logError(String error) {
  }

  public boolean consumeOnBackPressed() {
    return true;
  }

  public void onBackPressed() {
  }
}
