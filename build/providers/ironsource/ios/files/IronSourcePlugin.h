#import "PluginManager.h"

@interface IronSourcePlugin : GCPlugin <ISRewardedVideoDelegate, ISOfferwallDelegate, ISInterstitialDelegate>

@property (nonatomic, strong, readonly) NSString *ironSourceAppKey;
@property (nonatomic, strong) ISPlacementInfo *placementInfo;
@property (retain, nonatomic) UIViewController *viewController;

@end