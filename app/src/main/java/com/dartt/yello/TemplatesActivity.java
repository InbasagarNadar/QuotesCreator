package com.dartt.yello;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.dartt.yello.databinding.ActivityTemplatesBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TemplatesActivity extends AppCompatActivity {

    private final String TAG = "TemplatesActivity";
    ActivityResultLauncher<Intent> launchCreateBackground;
    TemplateAdapter adapter;
    private ActivityTemplatesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTemplatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_ham_vector);
        initTemplateRecycler();
        binding.toolbar.setNavigationOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            adapter.refreshDataset();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
        launchCreateBackground = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> adapter.refreshDataset()
        );

        boolean adsSettingExist = PreferenceManager.getDefaultSharedPreferences(this).contains("ad_enabled");
        if (!adsSettingExist) {
            showAdsConsentDialog();
        }

    }

    void showAdsConsentDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Hi there! We've decided to display advertisements to generate some revenue. Don't worry, the ads are optional. You can disable them in settings. But it would be awesome if you keep it on :)");
        builder1.setPositiveButton("Okay", (dialogInterface, i) -> {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("ad_enabled", true).apply();
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    void initTemplateRecycler() {
        SharedPreferences sharedPref = getSharedPreferences("drafts", Context.MODE_PRIVATE);
        adapter = new TemplateAdapter(this, sharedPref);
        binding.recyclerTemplate.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerTemplate.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit?")
                .setPositiveButton("Yes", (dialog, id) -> finish())
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
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
            // FirebaseAnalytics.getInstance(this).logEvent("support_us_clicked", );
            showSupportUsAdDialog();
        }
        else if (id == R.id.button_navigation_privacy_policy) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://yelloquotes.com/privacy.html")));
        }
        else if (id == R.id.button_navigation_facebook) {
            showFacebookPage();
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

    void showFacebookPage() {
        String url = "https://www.facebook.com/YelloQuotesCreator";
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We'll now show an ad which will help us generate revenue.")
                .setCancelable(false)
                .setPositiveButton("Okay", (dialog, id) -> Toast.makeText(this, "Not Implemented", Toast.LENGTH_LONG).show())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }


    static class TemplateImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public TemplateImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            imageView.setClipToOutline(true);
        }
    }

    static class DefaultViewHolder extends RecyclerView.ViewHolder {
        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class TemplateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        Context context;
        JSONArray draftKeys;
        SharedPreferences sharedPref;

        private final int VIEW_TYPE_NEW = 0;
        private final int VIEW_TYPE_IMAGE = 1;

        public TemplateAdapter(Context context, SharedPreferences sharedPref) {
            this.context = context;
            this.sharedPref = sharedPref;
            refreshDataset();
        }

        @SuppressLint("NotifyDataSetChanged")
        public void refreshDataset() {
            try {
                this.draftKeys = new JSONArray(sharedPref.getString("draft_keys", "[]"));
                notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("TemplateAdapter", "Failed to read JSONArray");
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_NEW) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_add_template, parent, false);
                return new DefaultViewHolder(view);
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_open_template, parent, false);
            return new TemplateImageViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_NEW;
            }
            return VIEW_TYPE_IMAGE;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position == 0) {
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(TemplatesActivity.this, CreateBackgroundActivity.class);
                    launchCreateBackground.launch(intent);
                });
                return;
            }
            try {
                TemplateImageViewHolder viewHolder = (TemplateImageViewHolder) holder;
                final String draftKey = draftKeys.getString(position - 1);
                String draft = sharedPref.getString(draftKey, "{}");
                JSONObject draftJson = new JSONObject(draft);
                String thumbnail = draftJson.getString("thumbnail");
                Glide.with(context).load(thumbnail).error(R.drawable.ic_error).signature(new ObjectKey(draftJson.getLong("last_modified"))).into(viewHolder.imageView);
                viewHolder.imageView.setOnClickListener(view -> {
                    Intent intent = new Intent(TemplatesActivity.this, CreateBackgroundActivity.class);
                    intent.putExtra("draft_key", draftKey);
                    launchCreateBackground.launch(intent);

                });
                viewHolder.imageView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.add("Make a copy").setOnMenuItemClickListener(item -> {
                        duplicateDraft(TemplatesActivity.this, draftKey, "draft_" + System.currentTimeMillis(), () -> {
                            Toast.makeText(viewHolder.imageView.getContext(), "Duplicate Successful", Toast.LENGTH_SHORT).show();
                            refreshDataset();
                        });
                        return true;
                    });
                    menu.add("Delete").setOnMenuItemClickListener(item -> {
                        deleteDraft(TemplatesActivity.this, draftKey, () -> {
                            Toast.makeText(viewHolder.imageView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            refreshDataset();
                        });
                        return true;
                    });
                });

            } catch (JSONException e) {
                Toast.makeText(context, "Something went wrong while reading templates.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onBindViewHolder JSON Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return draftKeys.length() + 1;
        }
    }

    static void duplicateDraft(Context context, String sourceDraftKey, String destinationDraftKey, FunctionParameter callback) {
        SharedPreferences sharedPref = context.getSharedPreferences("drafts", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        try {
            JSONArray draftKeys = new JSONArray(sharedPref.getString("draft_keys", "[]"));
            Utilities.addToPos(0, destinationDraftKey, draftKeys);
            File sourceDirectory = new File(context.getFilesDir() + "/" + sourceDraftKey);
            File destinationDirectory = new File(context.getFilesDir() + "/" + destinationDraftKey);
            destinationDirectory.mkdir();
            copyFile(new File(sourceDirectory + "/thumb.png"), new File(destinationDirectory + "/thumb.png"));
            JSONObject draftJSON = new JSONObject(sharedPref.getString(sourceDraftKey, "{}"));
            draftJSON.put("thumbnail", destinationDirectory + "/thumb.png");
            draftJSON.put("last_modified", System.currentTimeMillis());
            draftJSON.put("name", new java.util.Date(System.currentTimeMillis()));
            JSONObject editableJSON = draftJSON.getJSONObject("editable_json");
            if (editableJSON.getJSONObject("background").getInt("type") == ImageViewContainer.BACKGROUND_TYPE_URL) {
                editableJSON.getJSONObject("background").put("background", destinationDirectory + "/background.png");
                copyFile(new File(sourceDirectory + "/background.png"), new File(destinationDirectory + "/background.png"));
            }
            editor.putString("draft_keys", draftKeys.toString());
            editor.putString(destinationDraftKey, draftJSON.toString());
            editor.apply();
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(callback::execute);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            Log.e("TAG", e.toString());
        }

    }

    static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    static void deleteDraft(Context context, String draftKey, FunctionParameter callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            SharedPreferences sharedPref = context.getSharedPreferences("drafts", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            try {
                JSONArray draftKeys = new JSONArray(sharedPref.getString("draft_keys", "[]"));
                for (int i = 0; i < draftKeys.length(); i++) {
                    if (draftKeys.getString(i).equals(draftKey)) {
                        draftKeys.remove(i);
                        break;
                    }
                }
                File directory = new File(context.getFilesDir() + "/" + draftKey);
                File[] children = directory.listFiles();
                if (children != null) {
                    for (File child : children) {
                        child.delete();
                    }
                }
                directory.delete();

                editor.putString("draft_keys", draftKeys.toString());
                editor.remove(draftKey);
                editor.apply();
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(callback::execute);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("TAG", e.toString());
            }
        });
    }

}