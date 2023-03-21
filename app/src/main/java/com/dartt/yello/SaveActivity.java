package com.dartt.yello;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.dartt.yello.databinding.ActivitySaveBinding;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

//import com.google.android.gms.ads.rewarded.RewardedAdCallback;

public class SaveActivity extends AppCompatActivity {


    ConstraintLayout filterStrengthParent;
    SparseArray<Bitmap> bitmapSparseArray = new SparseArray<>();
    int[] filterStrengths;
    Bitmap originalImage;
    Bitmap originalThumbnail;
    ImageView originalImageView;
    ImageView filterImageView;
    ImageButton watermarkImageButton;
    SeekBar filterStrengthSeekBar;

    List<Filter> filterPack;
    int currentFilterIndex = 0;

//    String savedPath = null;

    boolean isImageSaved;
    String savedImageFileName;
    Uri savedImageUri;

    boolean watermarkRemoved = false;


    static {
        System.loadLibrary("NativeImageProcessor");
    }


    ActivitySaveBinding binding;

    private String watermarkRewardAdUnit;
    private ActivityResultLauncher<String> writePermissionActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initWatermarkRewardedAd();

        // TODO show progress bar. Using signature method to invalidate cache set by previous save I guess
        Glide.with(this).load(getFilesDir() + "/export.png").signature(new ObjectKey(System.currentTimeMillis())).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Drawable> transition) {
                onImageLoaded(resource);
            }

            @Override
            public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {
                originalImageView.setImageDrawable(null);
            }
        });

        writePermissionActivityResultLauncher = StorageHelper.registerImagePermissionResultLauncher(this, (permission, granted) -> {
            if (granted) {
                Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please grant storage permission to save image to gallery");
                builder.setPositiveButton("Okay", (dialog, id1) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permission)) {
                            StorageHelper.requestImageReadPermission(writePermissionActivityResultLauncher);
                        }
                        else {
                            StorageHelper.openAppInfoSystemSetting(this);
                        }
                    }
                });
                builder.create().show();
            }
        });
    }


    private void initWatermarkRewardedAd() {
        watermarkRewardAdUnit = getString(R.string.ad_unit_remove_watermark);
        if (!AdManager.hasAd(watermarkRewardAdUnit)) {
            AdManager.requestRewardedAd(this, watermarkRewardAdUnit);
        }
    }

    // Called whenever if filter is changed, filter opacity changed,
    private void onImageModified() {
        isImageSaved = false;
        savedImageUri = null;
        savedImageFileName = null;
    }


    public void onRemoveWatermarkClick(View view) {
        if (!watermarkRemoved) {
            boolean adsEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ad_enabled", true);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remove Watermark");
            if (adsEnabled) {
                builder.setMessage("We'll now show a short ad. Please watch the full ad to remove watermark. This supports us monetarily, Thank you :D");
                builder.setPositiveButton("Okay", (dialog, id) -> showWatermarkAd());
            }
            else {
                builder.setMessage("You've disabled ads, but it would be awesome if you watch this ad and support us monetarily. Would you like to watch an ad?");
                builder.setPositiveButton("Yes", (dialog, id) -> showWatermarkAd());
                builder.setNegativeButton("No", (dialog, id) -> {
                    Toast.makeText(this, "Watermark removed", Toast.LENGTH_LONG).show();
                    removeWatermark();
                });
            }

            builder.create().show();
        }
        else {
            Toast.makeText(this, "You have already removed watermark.", Toast.LENGTH_LONG).show();
        }
    }

    void removeWatermark() {
        watermarkRemoved = true;
        watermarkImageButton.setVisibility(View.GONE);
        onImageModified();
    }

    void showWatermarkAd() {
        AdManager.showAd(this, watermarkRewardAdUnit, new AdManager.Listener() {
            @Override
            public void onFail() {
                Toast.makeText(SaveActivity.this, "Failed to load ad. Watermark removed", Toast.LENGTH_LONG).show();
                removeWatermark();
            }

            @Override
            public void onSuccess() {
                removeWatermark();
            }
        });

    }


    private void initViews() {
        originalImageView = findViewById(R.id.image_view);
        filterImageView = findViewById(R.id.filter_image_view);
        watermarkImageButton = findViewById(R.id.watermark_image_button);
        filterStrengthSeekBar = findViewById(R.id.seek_bar_filter_strength);
        filterStrengthParent = findViewById(R.id.filter_strength_parent);

    }

    private void onImageLoaded(Drawable drawable) {
        originalImage = ((BitmapDrawable) drawable).getBitmap();
        originalThumbnail = getResizedBitmap(originalImage, 256);
        originalImageView.setImageBitmap(originalImage);
        initFilter();
        final ViewTreeObserver viewTreeObserver = originalImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                originalImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setWatermarkButtonDimension();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initFilter() {
        filterPack = FilterPack.getFilterPack(this);
        filterPack.add(0, null);
        filterStrengths = new int[filterPack.size()];
        for (int i = 0; i < filterPack.size(); i++) {
            filterStrengths[i] = filterStrengthSeekBar.getMax();
        }
        filterStrengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                filterImageView.setAlpha((float) i / (float) seekBar.getMax());
                filterStrengths[currentFilterIndex] = i;
                onImageModified();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Hide filter when pressing on image
        filterImageView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getPointerCount() == 1) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    filterImageView.setVisibility(View.VISIBLE);
                }
                else {
                    filterImageView.setVisibility(View.GONE);
                }
            }
            return true;
        });
        RecyclerView filterListView = findViewById(R.id.filter_recycler_view);
        filterListView.setAdapter(new FilterThumbnailAdapter(filterPack, originalThumbnail));
        filterListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        clearFilter();
    }

    public void clearFilter() {
        filterImageView.setAlpha(0f);
    }

    public void onBackClick(View view) {
        finish();
    }

    public void onSaveClick(View view) {
        if (StorageHelper.hasWritePermission(this)) {
            try {
                saveImageToDisk();
                Toast.makeText(this, "Saved as " + savedImageFileName + " in DCIM/Yello", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Something went wrong.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        else {
            StorageHelper.requestWritePermission(writePermissionActivityResultLauncher);
        }
    }

    public void onShareClick(View view) {
        if (StorageHelper.hasWritePermission(this)) {
            try {
                saveAndShareImage();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong while sharing the file.\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            StorageHelper.requestWritePermission(writePermissionActivityResultLauncher);
        }
    }

    final String IMAGES_FOLDER_NAME = "Yello";

    final float WATERMARK_REFERENCE_CANVAS_WIDTH = 1080;
    final float WATERMARK_WIDTH = 130;
    final float WATERMARK_HEIGHT = 65;
    final float WATERMARK_PADDING_BOTTOM = 15;
    final float WATERMARK_PADDING_RIGHT = 15;

    private Drawable getWatermarkDrawable(Bitmap targetBitmap) {
        Drawable watermark = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_yello_padded, null);
        final float scale = (targetBitmap.getWidth() / WATERMARK_REFERENCE_CANVAS_WIDTH);
        final int watermark_width = (int) (WATERMARK_WIDTH * scale);
        final int watermark_height = (int) (WATERMARK_HEIGHT * scale);
        final int watermark_padding_right = (int) (WATERMARK_PADDING_RIGHT * scale);
        final int watermark_padding_bottom = (int) (WATERMARK_PADDING_BOTTOM * scale);
        Rect bounds = new Rect();
        bounds.left = targetBitmap.getWidth() - watermark_width;
        bounds.top = targetBitmap.getHeight() - watermark_height;
        bounds.right = targetBitmap.getWidth();
        bounds.bottom = targetBitmap.getHeight();
        bounds.offset(-watermark_padding_right, -watermark_padding_bottom);
        watermark.setBounds(bounds);
        return watermark;
    }

    private void setWatermarkButtonDimension() {
        float scale = originalImageView.getWidth() / WATERMARK_REFERENCE_CANVAS_WIDTH;
        final int watermark_width = (int) (WATERMARK_WIDTH * scale);
        final int watermark_height = (int) (WATERMARK_HEIGHT * scale);
        final int watermark_padding_right = (int) (WATERMARK_PADDING_RIGHT * scale);
        final int watermark_padding_bottom = (int) (WATERMARK_PADDING_BOTTOM * scale);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) watermarkImageButton.getLayoutParams();
        params.width = watermark_width;
        params.height = watermark_height;
        params.rightMargin = watermark_padding_right;
        params.bottomMargin = watermark_padding_bottom;
        watermarkImageButton.setLayoutParams(params);
    }


    Bitmap.CompressFormat getCompressFormat(String filetype) {
        if (filetype.equals("png")) {
            return Bitmap.CompressFormat.PNG;
        }
        else if (filetype.equals("jpeg")) {
            return Bitmap.CompressFormat.JPEG;
        }
        return Bitmap.CompressFormat.PNG;
    }

    String compressFormatToMimeType(Bitmap.CompressFormat compressFormat) {
        switch (compressFormat) {
            case PNG:
                return "image/png";
            case JPEG:
                return "image/jpeg";
        }
        return "image/png";
    }

    String compressFormatToExtension(Bitmap.CompressFormat compressFormat) {
        switch (compressFormat) {
            case PNG:
                return "png";
            case JPEG:
                return "jpeg";
        }
        return "png";
    }


    private void saveAndShareImage() throws IOException {
        Bitmap.CompressFormat compressFormat = getCompressFormat(PreferenceManager.getDefaultSharedPreferences(this).getString("export_format", "png"));
        Uri imageFileUri = saveImageToDisk();
        Intent intent = new Intent(Intent.ACTION_SEND);
        // intent.setClipData(ClipData.newRawUri("", imageFileUri));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, imageFileUri);
        intent.putExtra(Intent.EXTRA_TEXT, "Created using Yello Quotes Creator\nDownload from https://play.google.com/store/apps/details?id=com.dartt.yello");
        intent.setType(compressFormatToMimeType(compressFormat));
        Intent chooser = Intent.createChooser(intent, "Share with");
        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, imageFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
        else {
            Toast.makeText(this, "No capable app found to share image", Toast.LENGTH_LONG).show();
        }
    }

    private Uri saveImageToDisk() throws IOException {
        if (isImageSaved) {
            return savedImageUri;
        }
        String fileName = "yello_quote_" + System.currentTimeMillis();
        Bitmap.CompressFormat compressFormat = getCompressFormat(PreferenceManager.getDefaultSharedPreferences(this).getString("export_format", "png"));
        Bitmap exportBitmap = generateBitmap();
        savedImageUri = saveBitmapToDCIM(exportBitmap, fileName, compressFormat);
        isImageSaved = true;
        savedImageFileName = fileName;
        return savedImageUri;
    }

    private Bitmap generateBitmap() {
        Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBitmap);

        // Check if filter is selected, then apply filter
        if (currentFilterIndex != 0) {
            // using copy so that it will work with alpha
            Bitmap filterBitmap = filterPack.get(currentFilterIndex).processFilter(resultBitmap.copy(Bitmap.Config.ARGB_8888, true));
            Paint alphaPaint = new Paint();
            alphaPaint.setAlpha((int) ((float) filterStrengths[currentFilterIndex] / (float) filterStrengthSeekBar.getMax() * 255));
            canvas.drawBitmap(filterBitmap, 0, 0, alphaPaint);
        }

        if (!watermarkRemoved) {
            Drawable watermark = getWatermarkDrawable(resultBitmap);
            watermark.draw(canvas);
        }
        return resultBitmap;
    }

    private Uri saveBitmapToDCIM(Bitmap bitmap, String fileName, Bitmap.CompressFormat compressFormat) throws IOException {
        Uri fileUri;
        String filePath;
        OutputStream outputStream;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, compressFormatToMimeType(compressFormat));
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + IMAGES_FOLDER_NAME);
            fileUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (fileUri == null) {
                return null;
            }
            outputStream = resolver.openOutputStream(fileUri);
            filePath = getRealPathFromURI(this, fileUri);
        }
        else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;
            File fileDirectory = new File(imagesDir);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
            File image = new File(imagesDir, fileName + "." + compressFormatToExtension(compressFormat));
            outputStream = new FileOutputStream(image);
            filePath = image.getAbsolutePath();
            fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".FileProvider", new File(filePath));
        }

        bitmap.compress(compressFormat, 100, outputStream);
        if (outputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
        MediaScannerConnection.scanFile(this, new String[]{filePath}, null, null);
        return fileUri;
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        String path;
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } catch (Exception e) {
            path = contentUri.getPath();
            e.printStackTrace();
            Log.d("PATH URI", "getRealPathFromURI: " + contentUri.getLastPathSegment());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        }
        else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    class FilterThumbnailAdapter extends RecyclerView.Adapter<FilterThumbnailAdapter.FilterThumbnailViewHolder> {
        List<Filter> filters;
        Bitmap thumbnail;

        class FilterThumbnailViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageButton imageButton;

            public FilterThumbnailViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.filter_name);
                imageButton = itemView.findViewById(R.id.filter_thumb);
            }
        }

        FilterThumbnailAdapter(List<Filter> filters, Bitmap thumbnail) {
            this.filters = filters;
            this.thumbnail = thumbnail;
        }

        @NonNull
        @Override
        public FilterThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FilterThumbnailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_thumbnail_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FilterThumbnailViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            final int filterIndex = position;
            if (position == 0) {
                holder.title.setText("Default");
                holder.imageButton.setImageBitmap(thumbnail);
                holder.imageButton.setOnClickListener(view -> {
                    if (currentFilterIndex != 0) {
                        onImageModified();
                    }
                    notifyItemChanged(currentFilterIndex);
                    notifyItemChanged(0);
                    clearFilter();
                    currentFilterIndex = 0;
                    filterStrengthParent.setVisibility(View.INVISIBLE);
                });
            }
            else {
                holder.title.setText(filters.get(position).getName());
                final ImageButton button = holder.imageButton.findViewById(R.id.filter_thumb);
                button.setImageBitmap(filters.get(position).processFilter(thumbnail.copy(Bitmap.Config.ARGB_8888, true)));
                button.setOnClickListener(view -> {
                    if (currentFilterIndex != filterIndex) {
                        onImageModified();
                        Bitmap filteredBitmap = bitmapSparseArray.get(filterIndex);
                        if (filteredBitmap == null) {
                            filteredBitmap = filterPack.get(filterIndex).processFilter(originalImage.copy(Bitmap.Config.ARGB_8888, true));
                            bitmapSparseArray.put(filterIndex, filteredBitmap);
                        }
                        filterImageView.setImageBitmap(filteredBitmap);
                        filterImageView.setAlpha(filterStrengths[filterIndex] / (float) filterStrengthSeekBar.getMax());
                        notifyItemChanged(currentFilterIndex);
                        notifyItemChanged(position);
                        currentFilterIndex = filterIndex;
                        filterStrengthSeekBar.setProgress(filterStrengths[filterIndex]);
                        filterStrengthParent.setVisibility(View.VISIBLE);
                    }
                });
            }
            if (currentFilterIndex == filterIndex) {
                holder.imageButton.setColorFilter(0x55FFFFFF);
                holder.title.setTextColor(getResources().getColor(R.color.colorAccent));
            }
            else {
                holder.imageButton.clearColorFilter();
                holder.title.setTextColor(0xFF444444);
            }
        }

        @Override
        public int getItemCount() {
            return filters.size();
        }
    }
}
