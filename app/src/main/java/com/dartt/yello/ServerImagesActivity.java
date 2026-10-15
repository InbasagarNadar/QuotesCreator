package com.dartt.yello;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServerImagesActivity extends AppCompatActivity {

    final String TAG = "ServerImageActivity";
    final String HOST = "https://www.inbasagar.com";
    final String CATEGORIES_URL = "https://www.inbasagar.com/projects/Yello/mobile_get_categories.php";
    final String IMAGES_URL = "https://www.inbasagar.com/projects/Yello/mobile_get_images.php";
    RecyclerView categoryRecyclerView;
    RecyclerView imageRecyclerView;
    StringRequest request;
    RequestQueue requestQueue;
    ServerImageCategoriesAdapter categoriesAdapter;
    ServerImageAdapter imageAdapter;
    ConstraintLayout loadingOverlay;
    ConstraintLayout categoryRetryPanel;
    //ConstraintLayout imageRetryPanel;
    CircularProgressDrawable loadingDrawable;
    int currentCategoryId = 0;
    private boolean showingImagesPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_images);
        loadingDrawable = new CircularProgressDrawable(this);
        loadingDrawable.setCenterRadius(Variables.densityToPixel(this, 50));
        loadingDrawable.start();
        loadingOverlay = findViewById(R.id.loading_panel);
        categoryRetryPanel = findViewById(R.id.retry_categories_panel);
        //imageRetryPanel = findViewById(R.id.retry_images_panel);
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imageRecyclerView = findViewById(R.id.image_recycler_view);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        requestQueue = Volley.newRequestQueue(this);
        requestCategories();
    }

    void requestCategories() {
        categoryRetryPanel.setVisibility(View.GONE);
        loadingOverlay.setVisibility(View.VISIBLE);
        request = new StringRequest(
                CATEGORIES_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("response").equals("OK")) {
                            categoriesAdapter = new ServerImageCategoriesAdapter(ServerImagesActivity.this, json.getJSONArray("categories"));
                            categoryRecyclerView.setAdapter(categoriesAdapter);
                        }
                    }
                    catch (JSONException e) {
                        Toast.makeText(ServerImagesActivity.this, "Invalid Response from server", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Response cannot be converted to json " + e.getMessage());
                        e.printStackTrace();
                    }
                    loadingOverlay.setVisibility(View.GONE);
                },
                error -> {
                    Toast.makeText(ServerImagesActivity.this, "Network Error", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley Error " + error.getMessage());
                    error.printStackTrace();
                    loadingOverlay.setVisibility(View.GONE);
                    categoryRetryPanel.setVisibility(View.VISIBLE);
                }
        );
        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        if (showingImagesPanel) {
            if (request != null) {
                request.cancel();
            }
            //imageRetryPanel.setVisibility(View.GONE);
            if (imageRecyclerView != null) {
                imageRecyclerView.setVisibility(View.GONE);
            }
            if (categoryRecyclerView != null) {
                categoryRecyclerView.setVisibility(View.VISIBLE);
            }
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
            showingImagesPanel = false;
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public void onCancelClick(View view) {
        onBackPressed();
    }


    void requestImageByCategory(final int categoryId) {
        showingImagesPanel = true;
        currentCategoryId = categoryId;
        loadingOverlay.setVisibility(View.VISIBLE);
        //imageRetryPanel.setVisibility(View.GONE);
        request = new StringRequest(
                Request.Method.POST,
                IMAGES_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("response").equals("OK")) {
                            imageAdapter = new ServerImageAdapter(ServerImagesActivity.this, json.getJSONArray("data"));
                            imageRecyclerView.setAdapter(imageAdapter);
                            imageRecyclerView.setVisibility(View.VISIBLE);
                            categoryRecyclerView.setVisibility(View.GONE);
                            Log.e(TAG, "Success");
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ServerImagesActivity.this, "Invalid Response from server", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Response cannot be converted to json " + e.getMessage() + "\n response:" + response);
                        e.printStackTrace();
                    }
                    loadingOverlay.setVisibility(View.GONE);
                },
                error -> {
                    Toast.makeText(ServerImagesActivity.this, "Network Error", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley Error " + error.getMessage());
                    error.printStackTrace();
                    loadingOverlay.setVisibility(View.GONE);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("category_id", String.valueOf(categoryId));
                return params;
            }
        };
        requestQueue.add(request);
    }

    public void onRetryCategoryClick(View view) {
        requestCategories();
    }

    public void onRetryImagesClick(View view) {
        requestImageByCategory(currentCategoryId);
    }

    class ServerImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ServerImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            imageView.setClipToOutline(true);
            imageView.setOnClickListener(view -> {
                try {
                    ServerImageDialogFragment dialogFragment = ServerImageDialogFragment.newInstance(HOST + imageAdapter.data.getJSONArray(getAdapterPosition()).getString(0));
                    dialogFragment.listener = new ServerImageDialogFragment.Listener() {
                        @Override
                        public void onImageSelected(String image_url) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("image_url", image_url);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }

                        @Override
                        public void onNothingSelected() {
                        }
                    };
                    dialogFragment.show(getSupportFragmentManager(), "dialog");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    class ServerImageAdapter extends RecyclerView.Adapter<ServerImageViewHolder> {
        final String TAG = "ServerImagesAdapter";
        Context context;
        JSONArray data;

        public ServerImageAdapter(Context context, JSONArray data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public ServerImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_image_row_layout, parent, false);
            return new ServerImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServerImageViewHolder holder, int position) {
            try {
                //Glide.with(context).load(data.getString(position)).thumbnail(.15f).override(256).placeholder(R.drawable.align_center).error(R.drawable.eye_dropper).into(holder.imageView);
                Glide.with(context).load(HOST + data.getJSONArray(position).getString(1)).placeholder(loadingDrawable).error(R.drawable.ic_error).into(holder.imageView);

            } catch (JSONException e) {
                Toast.makeText(context, "Something went wrong while reading the response.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onBindViewHolder JSON Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.length();
        }
    }


    class ServerImageCategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImageButton;
        TextView categoryLabelTextView;

        public ServerImageCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImageButton = itemView.findViewById(R.id.category_image);
            categoryLabelTextView = itemView.findViewById(R.id.category_label);
            categoryImageButton.setClipToOutline(true);
            categoryImageButton.setOnClickListener(view -> {
                try {
                    int categoryId = categoriesAdapter.data.getJSONObject(getAdapterPosition()).getInt("category_id");
                    requestImageByCategory(categoryId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    class ServerImageCategoriesAdapter extends RecyclerView.Adapter<ServerImageCategoryViewHolder> {
        final String TAG = "CategoryAdapter";
        Context context;
        JSONArray data;

        public ServerImageCategoriesAdapter(Context context, JSONArray data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public ServerImageCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_image_category_row_layout, parent, false);
            return new ServerImageCategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServerImageCategoryViewHolder holder, int position) {
            try {
                Glide.with(context).load(HOST + data.getJSONObject(position).getString("thumb_url")).placeholder(loadingDrawable).error(R.drawable.ic_error).into(holder.categoryImageButton);
                holder.categoryLabelTextView.setText(data.getJSONObject(position).getString("category_name"));
            } catch (JSONException e) {
                Toast.makeText(context, "Something went wrong while reading the response.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onBindViewHolder JSON Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.length();
        }
    }
}
