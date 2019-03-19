import device;
import event.Emitter as Emitter;
import util.setProperty as setProperty;

var is_online = navigator.onLine;

var is_rv_connected = false;

var rv_source = null;
/*
 * Flag for RewardVideo init status
 */
var is_rv_available = false;

/*
 * Flag for OfferWall init status
 */
var is_ow_available = false;

var IronSource = Class(Emitter, function (supr) {
  this.init = function() {
    supr(this, 'init', arguments);

    setProperty(this, "onAdDismissed", {
      set: function(f) {
        if (typeof f === "function") {
          onAdDismissed = f;
        } else {
          onAdDismissed = null;
        }
      },
      get: function() {
        return onOfferClose;
      }
    });

    setProperty(this, "onAdAvailable", {
      set: function(f) {
        if (typeof f === "function") {
          onAdAvailable = f;
        } else {
          onAdAvailable = null;
        }
      },
      get: function() {
        return onAdAvailable;
      }
    });

    setProperty(this, "onAdNotAvailable", {
      set: function(f) {
        if (typeof f === "function") {
          onAdNotAvailable = f;
        } else {
          onAdNotAvailable = null;
        }
      },
      get: function() {
        return onAdNotAvailable;
      }
    });

    setProperty(this, "onVideoClosed", {
      set: function(f) {
        // If a callback is being set,
        if (typeof f === "function") {
          onVideoClosed = f;
        } else {
          onVideoClosed = null;
        }
      },
      get: function() {
        return onVideoClosed;
      }
    });

    setProperty(this, "onOfferwallCredited", {
      set: function(f) {
        // If a callback is being set,
        if (typeof f === "function") {
          onOfferwallCredited = f;
        } else {
          onOfferwallCredited = null;
        }
      },
      get: function() {
        return onOfferwallCredited;
      }
    });

    NATIVE.events.registerHandler("IronsourceAdDismissed", function() {
      logger.log("{ironSource} ad dismissed ");
      if (typeof onAdDismissed === "function") {
        onAdDismissed();
      }
    });

    NATIVE.events.registerHandler("IronsourceAdAvailable", function() {
      logger.log("{ironSource} ad available");
      if (typeof onAdAvailable === "function") {
        onAdAvailable("ironSource");
      }
    });

    NATIVE.events.registerHandler("IronsourceAdNotAvailable", function() {
      logger.log("{ironSource} ad not available");
      if (typeof onAdNotAvailable === "function") {
        onAdNotAvailable();
      }
    });
    function onRWAvailabilityChange(is_rv_connected) {
      logger.log("Rewarded Video is now available before asigning");
      var available = is_rv_connected && is_online;

      if (available != is_rv_available) {
        is_rv_available = available;

        if (is_rv_available) {
          logger.log("Rewarded Video is now available");
        } else {
          logger.log("Rewarded Video is now unavailable");
        }
      }
    };

    NATIVE.events.registerHandler('ironsourceRVAdClosed', function(evt) {
      this.onVideoClosed(rv_source, evt.placement, evt.rewardedCount);
      rv_source = null;
    });

    NATIVE.events.registerHandler('ironsourceOnRVAvailabilityChange', function(evt) {
      onRWAvailabilityChange(evt.available);
    });

    window.addEventListener("online", bind(this, function() {
      is_online = true;

      if(this.user_id) {
        this.initVideoAd(this.user_id);
      }
    }));

    window.addEventListener("offline", function() {
      is_online = false;

      onRWAvailabilityChange(is_rv_connected);
    });
  };

  this.showInterstitial = function() {
    NATIVE.plugins.sendEvent("IronSourcePlugin", "showInterstitial", JSON.stringify({}));
  };

  this.cacheInterstitial = function() {
    NATIVE.plugins.sendEvent("IronSourcePlugin", "cacheInterstitial", JSON.stringify({}));
  }

  this.isOWAdAvailable = function() {
    return is_ow_available === true;
  };

  this.isVideoAdAvailable = function() {
    logger.log("{ironSource} isVideoAdAvailable plugin function ");
    return is_rv_available === true;
  };

  this.initInterstitial = function(user_id) {
    this.user_id = user_id;

    NATIVE.plugins.sendEvent("IronSourcePlugin", "initInterstitial", JSON.stringify({
      user_id: user_id
    }));
  };

  this.initVideoAd = function(user_id) {
    this.user_id = user_id;

    NATIVE.plugins.sendEvent("IronSourcePlugin", "initVideoAd", JSON.stringify({
      user_id: user_id
    }));
  };

  this.initOfferWallAd = function(user_id) {
    NATIVE.plugins.sendEvent("IronSourcePlugin", "initOfferWallAd", JSON.stringify({
      user_id: user_id
    }));
  };

  this.showVideoAd = function(placement_name) {
    rv_source = placement_name;

    NATIVE.plugins.sendEvent("IronSourcePlugin", "showRVAd", JSON.stringify({
      placementName: placement_name
    }));
  };

  this.showOWAd = function(userid) {
    NATIVE.plugins.sendEvent("IronSourcePlugin", "showOffersForUserID", JSON.stringify({
      userID: userid
    }));
  };
});

exports = new IronSource();
