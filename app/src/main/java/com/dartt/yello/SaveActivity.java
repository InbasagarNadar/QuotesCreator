package com.dartt.yello;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SaveActivity extends AppCompatActivity {


    ConstraintLayout filterStrengthParent;
    SparseArray<Bitmap> bitmapSparseArray = new SparseArray<>();
    int[] filterStrengths;
    Bitmap originalImage;
    Bitmap originalThumbnail;
    ImageView originalImageView;
    ImageView filterImageView;
    SeekBar filterStrengthSeekBar;
    String savedFilePath = "";
    List<Filter> filterPack;
    int currentFilterIndex = 0;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        originalImageView = findViewById(R.id.image_view);
        filterImageView = findViewById(R.id.filter_image_view);
        filterStrengthSeekBar = findViewById(R.id.seek_bar_filter_strength);
        filterStrengthParent = findViewById(R.id.filter_strength_parent);
        originalImage = CreateBackgroundActivity.bitmap;
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        originalImageView.setImageBitmap(originalImage);
        RecyclerView filterListView = findViewById(R.id.filter_recycler_view);
        filterListView.setAdapter(new FilterThumbnailAdapter(filterPack, originalThumbnail));
        filterListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        clearFilter();
    }


    public void onConfirmFilterStrengthClick(View view) {
        filterStrengths[currentFilterIndex] = filterStrengthSeekBar.getProgress();
        filterStrengthParent.setVisibility(View.GONE);
    }

    public void onCancelFilterStrengthClick(View view) {
        filterImageView.setAlpha(filterStrengths[currentFilterIndex] / (float) filterStrengthSeekBar.getMax());
        filterStrengthParent.setVisibility(View.GONE);
    }

    public void clearFilter() {
        filterImageView.setAlpha(0f);

    }

    void showFilterStrengthPanel(int seekBarProgress) {
        filterStrengthParent.setVisibility(View.VISIBLE);
        filterStrengthSeekBar.setProgress(seekBarProgress);
    }

    public void onSaveClick(View view) {
        try {
            if (savedFilePath.equals("")) {
                savedFilePath = saveImage(CreateBackgroundActivity.bitmap, "quote");
                Toast.makeText(this, "Saved at " + savedFilePath, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Already saved at " + savedFilePath, Toast.LENGTH_LONG).show();
            }
        }
        catch (IOException e) {
            Toast.makeText(this, "Something went wrong :/" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        /*File filesDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        assert filesDir != null;
        if (!filesDir.exists()) {
            if (filesDir.   mkdirs()) {
            }
        }
        File file = new File(filesDir, "quote.jpg");
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Cant able to create file");
                }
            }
            OutputStream os = new FileOutputStream(file);
            CreateBackgroundActivity.bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.close();
            Log.e("TAG", "File Path= " + file.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void onShareClick(View view) {

        File cachePath = new File(getExternalCacheDir(), "image/");
        cachePath.mkdirs();

        //create png file
        File file = new File(cachePath, "Yello.png");
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            CreateBackgroundActivity.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hmmmmm", Toast.LENGTH_SHORT).show();
        }

        try {
            Uri myImageFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

            //create a intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri);
            intent.setType("image/png");

            startActivity(Intent.createChooser(intent, "Share with"));
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    final String IMAGES_FOLDER_NAME = "Yello";

    private String saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        String path;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
            path = imageUri.getPath() + "/" + name + ".png";
        }
        else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);
            path = image.getAbsolutePath();
        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
        return path;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
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
        public void onBindViewHolder(@NonNull FilterThumbnailViewHolder holder, final int position) {
            final int filterIndex = position;
            if (position == 0) {
                holder.title.setText("Default");
                holder.imageButton.setImageBitmap(thumbnail);
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notifyItemChanged(currentFilterIndex);
                        notifyItemChanged(0);
                        clearFilter();
                        currentFilterIndex = 0;

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
                            showFilterStrengthPanel(filterStrengths[filterIndex]);
                        }
                        else {
                            Bitmap bitmap = bitmapSparseArray.get(filterIndex);
                            if (bitmap == null) {
                                bitmap = filterPack.get(filterIndex).processFilter(originalImage.copy(Bitmap.Config.ARGB_8888, true));
                                bitmapSparseArray.put(filterIndex, bitmap);
                            }
                            filterImageView.setImageBitmap(bitmap);
                            filterImageView.setAlpha(filterStrengths[filterIndex] / (float) filterStrengthSeekBar.getMax());
                            notifyItemChanged(currentFilterIndex);
                            notifyItemChanged(position);
                            currentFilterIndex = filterIndex;
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
