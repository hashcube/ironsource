#import "IronSourcePlugin.h"

@implementation IronSourcePlugin

// The plugin must call super dealloc.
- (void) dealloc {
  [super dealloc];
}

/* The plugin must call super init.
  Initializing the properties
  _ironSourceAppKey with nil and _ssaPub with IronSourceAdsPublisher singleton
*/
- (id) init {
  
  if (self = [super init]) {
    _ironSourceAppKey = nil;
    return self;
  }
  return nil;
}

- (void) initializeWithManifest:(NSDictionary *)manifest
                    appDelegate:(TeaLeafAppDelegate *)appDelegate {
  @try {
    NSDictionary *ios = [manifest valueForKey:@"ios"];
    _ironSourceAppKey = [ios valueForKey:@"ironsourceAppKey"];
    NSLog(@"{ironSource} Initializing with manifest ironSourceAppKey: '%@'",
      self.ironSourceAppKey);
    self.viewController = appDelegate.tealeafViewController;

    NSString *userId = [IronSource advertiserId];
    
    if([userId length] == 0){
        //If we couldn't get the advertiser id, we will be blank.
        userId = @"";
    }
    
    // After setting the delegates you can go ahead and initialize the SDK.
    [IronSource setUserId:userId];
    
    [IronSource initWithAppKey:self.ironSourceAppKey];
  }
  @catch (NSException *exception) {
    NSLog(@"{ironSource} Failed during startup: %@", exception);
  }
}

- (void) initVideoAd:(NSDictionary *)jsonObject {
  NSLog(@"{ironSource} Init VideoAd");
  [IronSource setRewardedVideoDelegate:self];

  [IronSource initWithAppKey:_ironSourceAppKey adUnits:@[IS_REWARDED_VIDEO]];
}

- (void) initInterstitial:(NSDictionary *)jsonObject {
  NSLog(@"{ironSource} Init Interstitials");
  [IronSource setInterstitialDelegate:self];
   
  [IronSource initWithAppKey:_ironSourceAppKey adUnits:@[IS_INTERSTITIAL]];
}

- (void) cacheInterstitial:(NSDictionary *)jsonObject {
  NSLog(@"{ironSource} cacheInterstitial");
  [IronSource loadInterstitial];
}

- (void) showInterstitial:(NSDictionary *)jsonObject {
  [IronSource showInterstitialWithViewController:self.viewController];
}

- (void) initOfferWallAd:(NSDictionary *)jsonObject {
  [IronSource setOfferwallDelegate:self];

  [IronSource initWithAppKey:_ironSourceAppKey adUnits:@[IS_OFFERWALL]];
}

- (void) showRVAd:(NSDictionary *)jsonObject {
  [IronSource showRewardedVideoWithViewController:self.viewController];
}

- (void) showOffersForUserID:(NSDictionary *)jsonObject {
  [IronSource showOfferwallWithViewController:self.viewController];
}

#pragma mark IronSourceOWDelegate Functions

// This method gets invoked after the availability of the Offerwall changes.
- (void)offerwallHasChangedAvailability:(BOOL)hasAvailableAds {
    NSLog(@"%s", __PRETTY_FUNCTION__);
    NSString *available = @"";
    if(hasAvailableAds) {
        available = @"true";
    }
    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"onOWAdAvailabilityChange",@"name",
                                          available, @"available",
                                          nil]];
}

// This method gets invoked each time the Offerwall loaded successfully.
- (void)offerwallDidShow {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

// This method gets invoked after a failed attempt to load the Offerwall.
// If it does happen, check out 'error' for more information and consult our
// Knowledge center.
- (void)offerwallDidFailToShowWithError:(NSError *)error {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

// This method gets invoked after the user had clicked the little
// 'x' button at the top-right corner of the screen.
- (void)offerwallDidClose {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

// This method will be called each time the user has completed an offer.
// All relative information is stored in 'creditInfo' and it is
// specified in more detail in 'SupersonicOWDelegate.h'.
// If you return NO the credit for the last offer will be added to
// Everytime you return 'NO' we aggragate the credit and return it all
// at one time when you return 'YES'.
- (BOOL)didReceiveOfferwallCredits:(NSDictionary *)creditInfo {
    NSLog(@"%s",__PRETTY_FUNCTION__);
    return YES;
}

// This method get invoked when the ‘-getOWCredits’ fails to retrieve
// the user's credit balance info.
- (void)didFailToReceiveOfferwallCreditsWithError:(NSError *)error {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

#pragma mark IronSourceRVDelegate Functions

// This method lets you know whether or not there is a video
// ready to be presented. It is only after this method is invoked
// with 'hasAvailableAds' set to 'YES' that you can should 'showRV'.
- (void)rewardedVideoHasChangedAvailability:(BOOL)hasAvailableAds {
    NSLog(@"%s", __PRETTY_FUNCTION__);
    NSString *available = @"";
    if(hasAvailableAds) {
        available = @"true";
    }
    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"ironsourceOnRVAvailabilityChange",@"name",
                                          available, @"available",
                                          nil]];
}

// This method gets invoked after the user has been rewarded.
- (void)didReceiveRewardForPlacement:(ISPlacementInfo *)placementInfo {
    NSLog(@"%s", __PRETTY_FUNCTION__);
    self.placementInfo = placementInfo;
}

// This method gets invoked when there is a problem playing the video.
// If it does happen, check out 'error' for more information and consult
// our knowledge center for help.
- (void)rewardedVideoDidFailToShowWithError:(NSError *)error {
    NSLog(@"%s", __PRETTY_FUNCTION__);
}

// This method gets invoked when we take control, but before
// the video has started playing.
- (void)rewardedVideoDidOpen {
    NSLog(@"%s", __PRETTY_FUNCTION__);
}

// This method gets invoked when we return controlback to your hands.
// We chose to notify you about rewards here and not in 'didReceiveRewardForPlacement'.
// This is because reward can occur in the middle of the video.
- (void)rewardedVideoDidClose {
    NSLog(@"%s", __PRETTY_FUNCTION__);
    NSString *rewardName = nil;
    NSNumber *rewardedCount = 0;
    if (self.placementInfo) {
        rewardName = ((ISPlacementInfo *) self.placementInfo).rewardName;
        rewardedCount = ((ISPlacementInfo *) self.placementInfo).rewardAmount;
    }
    
    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"ironsourceRVAdClosed",@"name",
                                          rewardName, @"placement",
                                          rewardedCount, @"rewardedCount",
                                          nil]];
    
    _placementInfo = nil;
}

// This method gets invoked when the video has started playing.
- (void)rewardedVideoDidStart {
    NSLog(@"%s", __PRETTY_FUNCTION__);
}

// This method gets invoked when the video has stopped playing.
- (void)rewardedVideoDidEnd {
    NSLog(@"%s", __PRETTY_FUNCTION__);
}

#pragma mark - Interstitial Delegate Functions

- (void)interstitialDidLoad {
    NSLog(@"%s",__PRETTY_FUNCTION__);
    NSLog(@"{ironSource} onInterstitialReady");
    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"IronSourceAdAvailable",@"name",
                                          nil]];
}

- (void)interstitialDidFailToLoadWithError:(NSError *)error {
    NSLog(@"%s",__PRETTY_FUNCTION__);
    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"IronSourceAdNotAvailable",@"name",
                                          nil]];
}

- (void)interstitialDidOpen {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

// The method will be called each time the Interstitial windows has opened successfully.
- (void)interstitialDidShow {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

// This method gets invoked after a failed attempt to load Interstitial.
// If it does happen, check out 'error' for more information and consult our
// Knowledge center.
- (void)interstitialDidFailToShowWithError:(NSError *)error {
    NSLog(@"{ironSource} onInterstitialNotAvailable");
}

// This method will be called each time the user had clicked the Interstitial ad.
- (void)didClickInterstitial {
    NSLog(@"%s",__PRETTY_FUNCTION__);
}

// This method get invoked after the Interstitial window had closed and control
// returns to your application.
- (void)interstitialDidClose {
    NSLog(@"{ironSource} onInterstitialClose");
    [[PluginManager get] dispatchJSEvent:[NSDictionary dictionaryWithObjectsAndKeys:
                                          @"IronSourceAdDismissed",@"name",
                                          nil]];
}
@end
