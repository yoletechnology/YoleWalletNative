package com.game.MobileAdsSDK;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

//import com.applovin.mediation.adapter.MaxAdapter;
import com.game.YouleSdk.CallBackFunction;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MobileAdsMgr {

    private String TAG = "MobileAdsMgr";
    private static MobileAdsMgr _instance = null;
    /***************激励相关*****************/
    public String rewardedAdId = "";
    public static RewardedAd rewardedAd = null;
    public static boolean isRewarded = false;
    public CallBackFunction rewardedAdCallBack = null;
    public static Activity tempActivity = null;

    public static MobileAdsMgr getsInstance() {
        if(MobileAdsMgr._instance == null)
        {
            MobileAdsMgr._instance = new MobileAdsMgr();
        }
        return MobileAdsMgr._instance;
    }
    private MobileAdsMgr() {
        Log.e(TAG,"MobileAdsMgr");
    }


    public void initAd(Context var1)
    {
        MobileAds.initialize( var1, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
    }
    public void preloadAd(Activity var1,String adId)
    {
        tempActivity = var1;
        rewardedAdId = adId;
    }

    public void preloadRewardedAd(boolean isShow)
    {
        Log.d(TAG, "preloadRewardedAd:"+rewardedAdId);

        this.isRewarded = false;
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        RewardedAd.load(tempActivity, rewardedAdId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error.
                Log.d(TAG, "onAdFailedToLoad"+loadAdError.toString());
                rewardedAd = null;
                if(rewardedAdCallBack != null)
                {
                    rewardedAdCallBack.onCallBack(isRewarded);
                }

            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                Log.d(TAG, "Ad was loaded.");
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d(TAG, "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d(TAG, "Ad dismissed fullscreen content.");
                        rewardedAd = null;
                        isRewarded = true;
                        rewardedAdCallBack.onCallBack(isRewarded);
                        preloadRewardedAd(false);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e(TAG, "Ad failed to show fullscreen content.");
//                        rewardedAd = null;
//                        isRewarded = false;
//                        rewardedAdCallBack.onCallBack(isRewarded);
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d(TAG, "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "Ad showed fullscreen content.");
                    }
                });
                if(isShow == true) {
                    showRewardedAd(rewardedAdCallBack);
                }
            }
        });

    }
    public void showRewardedAd(CallBackFunction callBack)
    {
        rewardedAdCallBack = callBack;
        Log.d(TAG, "showRewardedAd:"+rewardedAdId);
        if(rewardedAd == null)
        {
            this.preloadRewardedAd(true);
            return;
        }
        rewardedAd.show(tempActivity, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                // Handle the reward.
                Log.d(TAG, "The user earned the reward.");
                isRewarded = true;
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();
            }
        });
    }

    public void createBanner(Activity var1, String var2)
    {

        createBanner(var1,var2,Gravity.BOTTOM);
    }
    public void createBanner(Activity var1, String var2,int var3)
    {

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity= var3;
        AdManagerAdView adView = new AdManagerAdView(var1);
        adView.setAdUnitId(var2);
        var1.addContentView(adView, params);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.d(TAG, "Banner onAdClicked");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.d(TAG, "Banner onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.d(TAG, "Banner onAdFailedToLoad");
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
                Log.d(TAG, "Banner onAdImpression");
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "Banner onAdLoaded");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d(TAG, "Banner onAdOpened");
            }
        });
        adView.setAdSizes(AdSize.FULL_BANNER);
        adView.setActivated(true);
        adView.setVisibility(View.VISIBLE);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    public void createNativeAd(Activity var1, String var2)
    {
//        AdLoader nativeAdLoader = new AdLoader.Builder(var1, var2)
//                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
//                    @Override
//                    public void onNativeAdLoaded(NativeAd nativeAd) {
//                        Log.d(TAG, "onNativeAdLoaded");
//                        FrameLayout  nativeAd_frameLayout = (FrameLayout) var1.getLayoutInflater() .inflate(R.layout.my_template, (ViewGroup) var1.getWindow().getDecorView(),
//                                    true);
//                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
//                        TemplateView nativeAd_template= nativeAd_frameLayout.findViewById(R.id.my_template);
//                        nativeAd_template.setStyles(styles);
//                        nativeAd_template.setVisibility(View.VISIBLE);
//                        nativeAd_template.setNativeAd(nativeAd);
//                    }
//                })
//                .build();
//
//        nativeAdLoader.loadAd(new AdManagerAdRequest.Builder().build());

    }
}
