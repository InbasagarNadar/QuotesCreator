package com.dartt.yello;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.rewarded.RewardedAd;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

//import com.google.android.gms.ads.rewarded.RewardedAdCallback;

public class SaveActivity extends AppCompatActivity {


    public static Bitmap bitmap;
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
    String savedPath = null;
    RewardedAd watermarkRewardedAd;
    boolean watermarkRemoved = false;

    final int REQUEST_CODE_SAVE = 0;
    final int REQUEST_CODE_SHARE = 1;
    Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
    String fileExtension = "png";

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private String mimeType = "image/png";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        originalImageView = findViewById(R.id.image_view);
        filterImageView = findViewById(R.id.filter_image_view);
        watermarkImageButton = findViewById(R.id.watermark_image_button);
        filterStrengthSeekBar = findViewById(R.id.seek_bar_filter_strength);
        filterStrengthParent = findViewById(R.id.filter_strength_parent);

        originalImage = bitmap;
        originalThumbnail = getResizedBitmap(originalImage, 256);
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        filterImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getPointerCount() == 1) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        filterImageView.setVisibility(View.VISIBLE);
                    } else {
                        filterImageView.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });
        originalImageView.setImageBitmap(originalImage);
        RecyclerView filterListView = findViewById(R.id.filter_recycler_view);
        filterListView.setAdapter(new FilterThumbnailAdapter(filterPack, originalThumbnail));
        filterListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        clearFilter();

        final ViewTreeObserver viewTreeObserver = originalImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                originalImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setWatermarkButtonDimension();
            }
        });
        initWatermarkRewardedAd();

        fileExtension = PreferenceManager.getDefaultSharedPreferences(this).getString("export_format", "png");
        if (fileExtension.equals("png")) {
            compressFormat = Bitmap.CompressFormat.PNG;
            mimeType = "image/png";
        } else {
            compressFormat = Bitmap.CompressFormat.JPEG;
            mimeType = "image/jpeg";
        }
    }

    public void clearFilter() {
        filterImageView.setAlpha(0f);
    }

    public void onBackClick(View view) {
        finish();
    }

    public void onSaveClick(View view) {
        if (hasStoragePermission()) {
            try {
                if (savedPath == null) {
                    savedPath = saveBitmap();
                    Toast.makeText(this, "Saved at " + savedPath, Toast.LENGTH_LONG).show();
                } else {
                    if (!new File(savedPath).exists()) {
                        savedPath = saveBitmap();
                    } else {
                        Toast.makeText(this, "Already saved at " + savedPath, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (IOException e) {
                Toast.makeText(this, "Something went wrong. " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            requestStoragePermission(REQUEST_CODE_SAVE);
        }
    }

    public void onShareClick(View view) {
        if (hasStoragePermission()) {
            try {
                File file;
                if (savedPath != null) {
                    file = new File(savedPath);
                    if (!file.exists()) {
                        savedPath = saveBitmap();
                    }
                } else {
                    savedPath = saveBitmap();
                }
                file = new File(savedPath);

                Uri myImageFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".FileProvider", file);

                //create a intent
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri);
                intent.putExtra(Intent.EXTRA_TEXT, "Created using Yello Quotes Creator");
                intent.setType(mimeType);
                Intent chooser = Intent.createChooser(intent, "Share with");
                List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    this.grantUriPermission(packageName, myImageFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            requestStoragePermission(REQUEST_CODE_SHARE);
        }
    }

    public void onRemoveWatermarkClick(View view) {
        if (!watermarkRemoved) {
            /*if (!watermarkRewardedAd.isLoaded()) {
                //initWatermarkRewardedAd();
            }*/
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remove Watermark")
                    .setMessage("We'll now show a short ad. Please watch the full ad to remove watermark.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            /*try {
                                if (watermarkRewardedAd.isLoaded()) {
                                    watermarkRewardedAd.show(SaveActivity.this, new RewardedAdCallback() {
                                        @Override
                                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                            Toast.makeText(SaveActivity.this, "Watermark Removed", Toast.LENGTH_LONG).show();
                                            watermarkRemoved = true;
                                            watermarkImageButton.setVisibility(View.GONE);
                                            savedPath = null;
                                        }

                                        @Override
                                        public void onRewardedAdClosed() {
                                            if (!watermarkRemoved) {
                                                initWatermarkRewardedAd();
                                            }
                                            watermarkRemoved = true;
                                            watermarkImageButton.setVisibility(View.GONE);
                                            savedPath = null;
                                        }
                                    });
                                } else {
                                    Toast.makeText(SaveActivity.this, "Failed to Load Ad. Please Try again later.", Toast.LENGTH_SHORT).show();
                                    if (!watermarkRewardedAd.isLoaded()) {
                                        initWatermarkRewardedAd();
                                    }
                                    watermarkRemoved = true;
                                    watermarkImageButton.setVisibility(View.GONE);
                                    savedPath = null;

                                }
                            } catch (Exception e) {
                                Log.e("ADMOB", e.toString());*/
                                watermarkRemoved = true;
                                watermarkImageButton.setVisibility(View.GONE);
                                savedPath = null;
                            //}
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You have already removed watermark.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    void initWatermarkRewardedAd() {
        /*try {
            watermarkRewardedAd = new RewardedAd(this, getString(R.string.ad_unit_remove_watermark));
            RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    Log.e("AD", "Watermark Ad Loaded.");
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                    Log.e("AD", "Watermark Ad failed to load");
                }
            };
            watermarkRewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        } catch (Exception e) {
            Log.e("ADMOB", e.toString());
        }*/
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

    private String saveBitmap() throws IOException {
        Bitmap resultBitmap = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBitmap);

        if (currentFilterIndex != 0) {
            Paint alphaPaint = new Paint();
            alphaPaint.setAlpha((int) ((float) filterStrengths[currentFilterIndex] / (float) filterStrengthSeekBar.getMax() * 255));
            Bitmap filterBitmap = filterPack.get(currentFilterIndex).processFilter(bitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvas.drawBitmap(filterBitmap, 0, 0, alphaPaint);
        }
        if (!watermarkRemoved) {
            Drawable watermark = getWatermarkDrawable(originalImage);
            watermark.draw(canvas);
        }
        String fileName = "quote_" + System.currentTimeMillis();
        String path;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
            path = getRealPathFromURI(this, imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;
            File fileDirectory = new File(imagesDir);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
            File image = new File(imagesDir, fileName + "." + fileExtension);
            fos = new FileOutputStream(image);
            path = image.getAbsolutePath();
        }
        resultBitmap.compress(compressFormat, 100, fos);
        if (fos != null) {
            fos.flush();
            fos.close();
        }
        MediaScannerConnection.scanFile(this, new String[]{path}, null, null);
        return path;

    }

    boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    void requestStoragePermission(int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CODE_SAVE) {
                onSaveClick(null);
            }
            if (requestCode == REQUEST_CODE_SHARE) {
                onShareClick(null);
            }
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("SaveActivity", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
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
        public void onBindViewHolder(@NonNull FilterThumbnailViewHolder holder, final int position) {
            final int filterIndex = position;
            if (position == 0) {
                holder.title.setText("Default");
                holder.imageButton.setImageBitmap(thumbnail);
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentFilterIndex != 0) {
                            savedPath = null;
                        }
                        notifyItemChanged(currentFilterIndex);
                        notifyItemChanged(0);
                        clearFilter();
                        currentFilterIndex = 0;
                        filterStrengthParent.setVisibility(View.INVISIBLE);
                    }
                });
            }
            else {
                holder.title.setText(filters.get(position).getName());
                final ImageButton button = holder.imageButton.findViewById(R.id.filter_thumb);
                button.setImageBitmap(filters.get(position).processFilter(thumbnail.copy(Bitmap.Config.ARGB_8888, true)));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentFilterIndex == filterIndex) {
                            //showFilterStrengthPanel(filterStrengths[filterIndex]);
                        }
                        else {
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
                            savedPath = null;
                            //setSelectedFilterButton(index + 1);
                        }
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
