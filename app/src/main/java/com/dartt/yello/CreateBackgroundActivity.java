package com.dartt.yello;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.IOException;
import java.io.InputStream;

@SuppressLint("ClickableViewAccessibility")
public class CreateBackgroundActivity extends AppCompatActivity {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    DrawerLayout drawerLayout;
    Background Background;

    FrameLayout canvasFrameLayout;

    EditorFragment editorFragment;
    TextEditorFragment textEditorFragment;
    StickerEditorFragment stickerEditorFragment;

    FunctionParameter canvasLayoutChangeAction;
    boolean init = false;

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    RewardedAd supportUsAd;

    ImageButton btnUndo;
    ImageButton btnRedo;

    final Undo undo = new Undo() {
        @Override
        void changeButtonState(boolean undoEnabled, boolean redoEnabled) {
            btnUndo.setEnabled(undoEnabled);
            btnRedo.setEnabled(redoEnabled);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_background);
        /*
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .build());
        */

        Background = new Background();


        FragmentManager fragmentManager = getSupportFragmentManager();
        editorFragment = (EditorFragment) fragmentManager.findFragmentById(R.id.frame_editor_fragment);
        textEditorFragment = (TextEditorFragment) fragmentManager.findFragmentById(R.id.text_editor_fragment);
        stickerEditorFragment = (StickerEditorFragment) fragmentManager.findFragmentById(R.id.sticker_editor_fragment);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        canvasFrameLayout = findViewById(R.id.canvas);
        btnUndo = findViewById(R.id.btn_undo);
        btnRedo = findViewById(R.id.btn_redo);
        btnUndo.setEnabled(false);
        btnRedo.setEnabled(false);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(4);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        supportUsAd = createAndLoadRewardedAd(getString(R.string.ad_unit_support_us));

    }


    ///////////////////////////////////////////////////////////////////////////

    public void setEditorBackground(int resourceId) {
        findViewById(R.id.button_filter).setVisibility(View.VISIBLE);
        editorFragment.setBackground(resourceId);
    }

    public void setEditorBackground(String url) {
        findViewById(R.id.button_filter).setVisibility(View.VISIBLE);
        editorFragment.setBackground(url);
    }

    public void setEditorBackground(Uri imageUri) {
        findViewById(R.id.button_filter).setVisibility(View.VISIBLE);
        editorFragment.setBackground(imageUri);
    }

    public void onRemoveBackgroundClick(View view) {
        findViewById(R.id.button_filter).setVisibility(View.GONE);
        editorFragment.removeBackground();
    }

    public void setEditorArtBoardFill(int color) {
        editorFragment.setArtBoardFill(color);
    }

    public void setEditorArtBoardFill(int[] gradient, GradientDrawable.Orientation orientation) {
        editorFragment.setArtBoardFill(gradient, orientation);
    }

    public void beforeFilterChange() {
        undo.beforeEdit(editorFragment.backgroundViewContainer);
    }

    public void afterFilterChange() {
        undo.afterEdit(editorFragment.backgroundViewContainer);
    }

    public void setContrastBrightnessSaturation(float contrast, float brightness, float saturation) {
        editorFragment.setContrastBrightnessSaturation(contrast, brightness, saturation);
    }


    ///////////////////////////////////////////////////////////////////////////

    public void onShowStickerSheetClick(View view) {
        Background.setFragment(5);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("sticker_dialog");
        if (prev == null) {
            StickerBottomSheet dialogFragment = new StickerBottomSheet();
            dialogFragment.listener = new StickerBottomSheet.Listener() {
                @Override
                public void onStickerSelectListener(int index) {
                    editorFragment.addElement(Variables.stickerResourceId[index]);
                }
            };
            dialogFragment.show(ft, "sticker_dialog");
        }
    }


    ///////////////////////////////////////////////////////////////////////////


    public void onElementClicked(ViewContainer viewContainer) {
        if (viewContainer instanceof StickerViewContainer) {
            stickerEditorFragment.edit((StickerViewContainer) viewContainer);
        }
        if (viewContainer instanceof TextViewContainer) {
            textEditorFragment.edit((TextViewContainer) viewContainer);
        }
    }

    public void removeElement(ViewContainer viewContainer) {
        undo.beforeEdit(viewContainer);
        editorFragment.removeElement(viewContainer);
        undo.afterEdit(viewContainer);
    }

    //////////////////////////////////////////////////////////////////////////


    public void onAddTextClick(View view) {
        Background.setFragment(4);
        TextViewContainer viewContainer = new TextViewContainer(editorFragment);
        //editorFragment.addElement(viewContainer);
        textEditorFragment.edit(viewContainer);
        //undo.onElementAdded(view);
    }


    //////////////////////////////////////////////////////////////////////////


    public void onUndoClick(View view) {
        undo.onUndoClick();
        updateFragmentStates();
    }

    public void onRedoClick(View view) {
        undo.onRedoClick();
        updateFragmentStates();
    }

    void updateFragmentStates() {
        //Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 3);
        //((FilterFragment) fragment).resetFilter(editorFragment.backgroundViewContainer);
        viewPagerAdapter.filterFragment.resetFilter(editorFragment.backgroundViewContainer);
        viewPagerAdapter.backgroundImageFragment.resetState(editorFragment.backgroundViewContainer);
    }

    /////////////////////////////////////////////////////////////////////////


    public void onRatioClick(View view) {
        editorFragment.setRatio(((Button) view).getText().toString());
    }

    public void onShowBackgroundImagesFragmentClick(View view) {
        Background.setFragment(0);
    }

    public void onShowBackgroundColorFragmentClick(View view) {
        Background.setFragment(1);
    }

    public void onShowRatioFragmentClick(View view) {
        Background.setFragment(2);
    }

    public void onShowFilterFragmentClick(View view) {
        Background.setFragment(3);
    }

    public RewardedAd createAndLoadRewardedAd(String adUnitId) {
        RewardedAd rewardedAd = new RewardedAd(this, adUnitId);
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                Log.e("SupportAd", "Support Us Loaded");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                Log.e("SupportAd", "Support Us Ad failed to load. " + adError.toString());
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    public void onHamClick(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (textEditorFragment.isVisible()) {
            textEditorFragment.commit();
            return;
        }
        if (stickerEditorFragment.isVisible()) {
            stickerEditorFragment.commit();
            stickerEditorFragment.hide();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit? Unsaved progress will be discarded.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onSaveClick(View view) {
        SaveActivity.bitmap = editorFragment.getBitmap();
        Intent intent = new Intent(CreateBackgroundActivity.this, SaveActivity.class);
        startActivity(intent);
    }

    class Background {
        public final FrameLayout canvas = findViewById(R.id.canvas);

        public final ImageButton[] menuButtons = new ImageButton[]{
                findViewById(R.id.button_canvas_image),
                findViewById(R.id.button_canvas_fill),
                findViewById(R.id.button_canvas_ratio),
                findViewById(R.id.button_filter),
                findViewById(R.id.button_add_text),
                findViewById(R.id.button_add_sticker)
        };


        Background() {
            final ViewTreeObserver viewTreeObserver = canvas.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (canvasLayoutChangeAction != null) {
                        canvasLayoutChangeAction.execute();
                    }
                    if (init) {
                        return;
                    }
                    init = true;
                    View fixedParent = findViewById(R.id.fixedParent);
                    fixedParent.getLayoutParams().width = fixedParent.getWidth();
                    fixedParent.getLayoutParams().height = fixedParent.getHeight();
                }
            });
        }

        void setFragment(int index) {
            int accentColor = getResources().getColor(R.color.colorAccent);
            for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
                if (i == index) {
                    menuButtons[i].setColorFilter(accentColor);
                }
                else {
                    menuButtons[i].setColorFilter(0xFF000000);
                }
            }
            if (index < viewPager.getAdapter().getCount()) {
                if (index == viewPager.getCurrentItem() && viewPager.getVisibility() != View.INVISIBLE) {
                    viewPager.setVisibility(View.INVISIBLE);
                    menuButtons[index].setColorFilter(0xFF000000);
                }
                else {
                    viewPager.setCurrentItem(index, viewPager.getVisibility() == View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                }
            }
            else {
                viewPager.setVisibility(View.INVISIBLE);
            }
            if (viewPager.getVisibility() == View.VISIBLE && viewPager.getCurrentItem() == 1) {
                //Background.backgroundImageView.setAlpha(.5f);
            }
            else {
                //Background.backgroundImageView.setAlpha(1f);
            }
        }
    }

    public void OnNavigationItemClick(View view) {
        int id = view.getId();
        if (id == R.id.button_navigation_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (id == R.id.button_navigation_fonts) {
            startActivity(new Intent(this, ManageLanguageActivity.class));
        }
        else if (id == R.id.button_navigation_rate_app) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.getPackageName())));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }
        }
        else if (id == R.id.button_navigation_contact_us) {
            openMailApp();
        }
        else if (id == R.id.button_navigation_support_us) {
            showSupportUsAdDialog();
        }
        else if (id == R.id.button_navigation_facebook) {
            Toast.makeText(this, "Facebook page coming soon.", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.button_navigation_get_featured || id == R.id.button_navigation_instagram) {
            showInstagramPage();
        }
        else if (id == R.id.button_faq) {
            startActivity(new Intent(this, FaqActivity.class));
        }
    }

    void openMailApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Yello.Darttdevelopers@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hello Yello, ");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
        else {
            Toast.makeText(this, "Send an email to\nYello.DarttDevelopers@gmail.com\nto get in touch with us. ", Toast.LENGTH_SHORT).show();
        }
    }

    void showInstagramPage() {
        Uri uri = Uri.parse("http://instagram.com/_u/yelloquotes");
        Intent instagramIntent = new Intent(Intent.ACTION_VIEW, uri);
        instagramIntent.setPackage("com.instagram.android");
        try {
            startActivity(instagramIntent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/yelloquotes")));
        }
    }

    void showSupportUsAdDialog() {
        if (!supportUsAd.isLoaded()) {
            //supportUsAd = createAndLoadRewardedAd(getString(R.string.ad_unit_support_us));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We'll now show an ad which will help us generate revenue.")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (supportUsAd.isLoaded()) {
                            supportUsAd.show(CreateBackgroundActivity.this, new RewardedAdCallback() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    Toast.makeText(CreateBackgroundActivity.this, "Thanks for supporting us :D", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onRewardedAdClosed() {
                                    supportUsAd = createAndLoadRewardedAd(getResources().getString(R.string.ad_unit_support_us));
                                }
                            });
                        }
                        else {
                            Toast.makeText(CreateBackgroundActivity.this, "Failed to Load Ad. Please Try again later.", Toast.LENGTH_SHORT).show();
                            if (!supportUsAd.isLoaded()) {
                                // supportUsAd = createAndLoadRewardedAd(getString(R.string.ad_unit_support_us));
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        public BackgroundImageFragment backgroundImageFragment;
        public BackgroundColorFragment backgroundFillFragment;
        public RatioFragment ratioFragment;
        public FilterFragment filterFragment;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            backgroundImageFragment = (BackgroundImageFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 0);
            if (backgroundImageFragment == null) {
                backgroundImageFragment = new BackgroundImageFragment();
            }
            backgroundFillFragment = (BackgroundColorFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 1);
            if (backgroundFillFragment == null) {
                backgroundFillFragment = new BackgroundColorFragment();
            }
            ratioFragment = (RatioFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 2);
            if (ratioFragment == null) {
                ratioFragment = new RatioFragment();
            }
            filterFragment = (FilterFragment) fm.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 3);
            if (filterFragment == null) {
                filterFragment = new FilterFragment();
            }
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 0) {
                fragment = backgroundImageFragment;
            }
            else if (position == 1) {
                fragment = backgroundFillFragment;
            }
            else if (position == 2) {
                fragment = ratioFragment;
            }
            else {
                fragment = filterFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    int getImageRotationFromExif(Uri imageUri) {
        int rotation = 0;
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return rotation;
            }
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
            }
        } catch (IOException e) {
            Log.e("ExifException", "Something went wrong");
        }
        return rotation;
    }

    String getID(int resId) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(resId) + '/' + getResources().getResourceTypeName(resId) + '/' + getResources().getResourceEntryName(resId);
    }

}
