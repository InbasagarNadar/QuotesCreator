package com.dartt.yello;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Arrays;
import java.util.HashMap;

public class AdManager {

    static interface Listener {
        void onFail();

        void onSuccess();
    }



    static final String TAG = "AdManager";
    public static final String TEST_DEVICE_ID = "CA65B52D8177D28B249A5F017E0EC63F";
    static final HashMap<String, RewardedAd> rewardedAds = new HashMap<>();

    public static void configureTestDevices() {
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(Arrays.asList(AdRequest.DEVICE_ID_EMULATOR, TEST_DEVICE_ID))
                .build();
        MobileAds.setRequestConfiguration(configuration);
    }

    public static void requestRewardedAd(Context context, String adUnit) {
        if (hasAd(adUnit) || context == null) {
            return;
        }
        try {
            configureTestDevices();
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
            Context appContext = context.getApplicationContext();
            RewardedAd.load(appContext != null ? appContext : context, adUnit, adRequest, callback);
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