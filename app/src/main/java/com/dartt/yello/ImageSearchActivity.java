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
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImageSearchActivity extends AppCompatActivity {

    final String TAG = "ImageSearch";

    SearchView editTextImageSearch;
    RecyclerView recyclerViewImageGrid;
    ConstraintLayout loadingPanel;
    ConstraintLayout retryPanel;

    PixabayAdapter imageAdapter;

    RequestQueue requestQueue;
    Response.ErrorListener onErrorListener;

    ArrayList<String> searchHistory = new ArrayList<>();
    //HashMap<String, JSONArray> cachedQueries = new HashMap<>();

    StringRequest activeRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);

        editTextImageSearch = findViewById(R.id.edit_text_image_search);
        recyclerViewImageGrid = findViewById(R.id.recycler_image_results);
        loadingPanel = findViewById(R.id.loading_panel);
        retryPanel = findViewById(R.id.retry_images_panel);

        requestQueue = Volley.newRequestQueue(this);
        recyclerViewImageGrid.setLayoutManager(new GridLayoutManager(this, 2));
        imageAdapter = new PixabayAdapter(this, new JSONArray(), (TextView) findViewById(R.id.text_view_no_result));
        recyclerViewImageGrid.setAdapter(imageAdapter);


        onErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingPanel.setVisibility(View.GONE);
                retryPanel.setVisibility(View.VISIBLE);
                Toast.makeText(ImageSearchActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                Log.d(TAG, error.toString());
            }
        };

        editTextImageSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryImages(query, true);
                editTextImageSearch.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        queryImages("Minimal Background", true);
    }

    @Override
    public void onBackPressed() {
        int length = searchHistory.size();
        if (length > 1) {
            String query = searchHistory.get(length - 2);
            queryImages(query, false);
            if (length > 2) {
                editTextImageSearch.setQuery(query, false);
            }
            searchHistory.remove(length - 1);

            return;
        }
        super.onBackPressed();
    }

    public void onCancelClick(View view) {
        finish();
    }

    public void onRetryClick(View view) {
        queryImages(searchHistory.get(searchHistory.size() - 1), false);
    }

    void queryImages(String keyword, boolean addToHistory) {
        final String query = keyword.trim().toLowerCase();
        if (addToHistory) {
            searchHistory.add(query);
        }
        retryPanel.setVisibility(View.GONE);
        /*if (cachedQueries.containsKey(query)) {
            recyclerViewImageGrid.setScrollX(0);
            imageAdapter.setData(cachedQueries.get(query));
            return;
        }*/
        if (activeRequest != null) {
            activeRequest.cancel();
        }
        loadingPanel.setVisibility(View.VISIBLE);
        String url = "https://www.pixabay.com/api/?key=20611372-d4a4a10d99f6805c65245bf67&per_page=200&q=" + keyword;
        activeRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray hits = new JSONObject(response).getJSONArray("hits");
                            imageAdapter.setData(hits);
                            recyclerViewImageGrid.scrollToPosition(0);
                            //cachedQueries.put(query, hits);
                            loadingPanel.setVisibility(View.GONE);
                            activeRequest = null;
                        } catch (JSONException e) {
                            Log.d(TAG, "Cannot parse to json");
                        }
                    }
                }
                , onErrorListener
        );
        requestQueue.add(activeRequest);

    }

    class ServerImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ServerImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            imageView.setClipToOutline(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ServerImageDialogFragment dialogFragment = ServerImageDialogFragment.newInstance(imageAdapter.data.getJSONObject(getAdapterPosition()).getString("largeImageURL"));
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    class PixabayAdapter extends RecyclerView.Adapter<ServerImageViewHolder> {
        Context context;
        JSONArray data;
        TextView textViewEmptyData;

        public PixabayAdapter(Context context, JSONArray data, TextView textViewEmptyData) {
            this.context = context;
            this.data = data;
            this.textViewEmptyData = textViewEmptyData;
        }


        public void setData(JSONArray hits) {
            data = hits;
            notifyDataSetChanged();
            if (data.length() == 0) {
                textViewEmptyData.setVisibility(View.VISIBLE);
            }
            else {
                textViewEmptyData.setVisibility(View.GONE);
            }
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
                String webformatURL = data.getJSONObject(position).getString("webformatURL");
                String previewURL = data.getJSONObject(position).getString("previewURL");
                Glide.with(context).load(webformatURL).thumbnail(Glide.with(context).load(previewURL)).error(R.drawable.ic_error).into(holder.imageView);

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