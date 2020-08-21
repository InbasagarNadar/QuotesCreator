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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerImagesActivity extends AppCompatActivity {

    final String TAG = "ServerImageActivity";
    final String JSON_URL = "https://www.inbasagar.com/qc/images.php";
    RecyclerView recyclerView;
    StringRequest request;
    RequestQueue requestQueue;
    ServerImagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_images);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        requestQueue = Volley.newRequestQueue(this);
        jsonRequest();
    }

    void jsonRequest() {
        request = new StringRequest(
                JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            adapter = new ServerImagesAdapter(ServerImagesActivity.this, json.getJSONArray("image_urls"));
                            recyclerView.setAdapter(adapter);
                        }
                        catch (JSONException e) {
                            Toast.makeText(ServerImagesActivity.this, "Invalid Response from server", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Response cannot be converted to json " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ServerImagesActivity.this, "Network Error", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Response cannot be converted to json " + error.getMessage());
                        error.printStackTrace();
                    }
                }
        );
        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onCancelClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onImageSelected(int index) {

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
                        ServerImageDialogFragment dialogFragment = ServerImageDialogFragment.newInstance(adapter.data.getString(getAdapterPosition()));
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
                }
            });
        }
    }

    class ServerImagesAdapter extends RecyclerView.Adapter<ServerImageViewHolder> {
        final String TAG = "ServerImagesAdapter";
        Context context;
        JSONArray data;

        public ServerImagesAdapter(Context context, JSONArray data) {
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
                Glide.with(context).load(data.getString(position)).thumbnail(.15f).override(256).placeholder(R.drawable.align_center).error(R.drawable.eye_dropper).into(holder.imageView);

            }
            catch (JSONException e) {
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
