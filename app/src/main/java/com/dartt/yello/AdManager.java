package com.dartt.yello;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.HashMap;

public class AdManager {

    static interface Listener {
        void onFail();

        void onSuccess();
    }

    ;

    static final String TAG = "AdManager";
    static final HashMap<String, RewardedAd> rewardedAds = new HashMap<>();


    public static void requestRewardedAd(Context context, String adUnit) {
        if (hasAd(adUnit)) {
            return;
        }
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAdLoadCallback callback = new RewardedAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, loadAdError.toString());
                }

                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    rewardedAds.put(adUnit, rewardedAd);
                }
            };
            RewardedAd.load(context, adUnit, adRequest, callback);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static RewardedAd getAd(String adUnit) {
        return rewardedAds.get(adUnit);
    }

    public static boolean hasAd(String adUnit) {
        return rewardedAds.containsKey(adUnit);
    }

    public static void showAd(Activity activity, String adUnit, Listener listener) {
        if (!hasAd(adUnit)) {
            listener.onFail();
            return;
        }
        RewardedAd ad = AdManager.getAd(adUnit);
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {

                // listener.onFail();
                rewardedAds.remove(adUnit);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                listener.onFail();
                rewardedAds.remove(adUnit);
            }

        });

        ad.show(activity, rewardItem -> {
            listener.onSuccess();
            rewardedAds.remove(adUnit);
        });
    }

    public static void preloadAds(Context context) {
        requestRewardedAd(context, context.getString(R.string.ad_unit_remove_watermark));
    }

}