package com.dartt.yello;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.Locale;

public class LanguageDownloadDialogFragment extends DialogFragment {

    static final String TAG = "LanguageDialog";

    int languageIndex;
    String downloadOutputPath;

    Lang lang;

    Button btnCancel;
    Button btnConfirm;
    Button btnRetry;
    ProgressBar progressBar;
    TextView lblTitle;
    TextView lblProgress;
    Activity activity;

    Listener listener;
    private CustomDownloadManager downloadManager;

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        languageIndex = getArguments().getInt("language_index");
        lang = LanguageManager.languages[languageIndex];
        downloadOutputPath = activity.getCacheDir() + "/" + lang.code;


        View v = inflater.inflate(R.layout.dialog_language_download, container, false);
        btnCancel = v.findViewById(R.id.button_cancel);
        btnRetry = v.findViewById(R.id.button_retry);
        btnConfirm = v.findViewById(R.id.button_confirm);
        progressBar = v.findViewById(R.id.progress_bar);
        lblTitle = v.findViewById(R.id.label_title);
        lblProgress = v.findViewById(R.id.label_progress);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadManager != null) {
                    downloadManager.interrupt();
                }
                dismiss();
                if (listener != null) {
                    listener.onDismiss(languageIndex, false);
                }
            }
        });
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownloading();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onDismiss(languageIndex, true);
                }
            }
        });

        setCancelable(false);
        startDownloading();
        return v;
    }

    public void startDownloading() {
        lblTitle.setText("Downloading...");
        btnRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        lblProgress.setVisibility(View.VISIBLE);
        //TODO change url/ something like https://inbasagar.com/fonts?language_code=tamil
        downloadManager = new CustomDownloadManager(activity, "https://inbasagar.com/projects/Yello/" + lang.code + ".zip", downloadOutputPath, 5) {
            @Override
            void onGetFileSize(long fileSize) {
                super.onGetFileSize(fileSize);
                progressBar.setMax((int) fileSize);
                Log.e(TAG, "File Size: " + fileSize);
            }

            @Override
            void onDownloadProgressed(long progress) {
                if (fileSize != -1) {
                    super.onDownloadProgressed(progress);
                    progressBar.setProgress((int) progress);
                    lblProgress.setText(String.format(Locale.US, "%d%%", progress * 100 / fileSize));
                    Log.e(TAG, "Download Progress: " + progress + " / " + fileSize);
                }
            }

            @Override
            void onDownloadFailed(String errorMessage, boolean willRetry) {
                if (willRetry) {
                    Log.e(TAG, "Failed, Retrying. Error: " + errorMessage);
                } else {
                    downloadManager = null;
                    lblTitle.setText("Failed to download. Please check network");
                    btnRetry.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    lblProgress.setVisibility(View.GONE);
                    Toast.makeText(context, "Download Failed. Please check network.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed. Error: " + errorMessage);
                }
            }

            @Override
            void onDownloadCompleted(File file, boolean cached) {
                startExtracting(file);
                Log.e(TAG, "Downloaded: " + lang.code + " cached:" + cached);
            }
        };
        downloadManager.startDownloading();
    }

    private void startExtracting(File zipFile) {
        lblTitle.setText("Installing...");
        btnCancel.setVisibility(View.GONE);
        CustomZipExtractor zipExtractor = new CustomZipExtractor(activity, zipFile, lang.getFontsPath(activity)) {
            @Override
            void onGetFileCount(int fileCount) {
                progressBar.setMax(fileCount);
            }

            @Override
            void onProgress(int progress) {
                progressBar.setProgress(progress);
                lblProgress.setText(String.format(Locale.US, "%d/%d", progress, fileCount));
            }

            @Override
            void onExtractionFailed(Exception exception) {
                btnCancel.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                lblProgress.setVisibility(View.GONE);
                lblTitle.setText("Failed to install. Please Retry.");
                Log.e(TAG, "Extraction Failed. Exception: " + exception.toString());
                exception.printStackTrace();
            }

            @Override
            void onExtractionComplete(String[] fileNames, String outputPath) {
                lblTitle.setText(String.format(Locale.US, "Installed %s!", LanguageManager.languages[languageIndex].name));
                lang.setDownloaded(activity, true);
                lang.setActive(activity, true);
                progressBar.setVisibility(View.GONE);
                lblProgress.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnConfirm.setVisibility(View.VISIBLE);
                for (String fileName : fileNames) {
                    Log.v(TAG, "Extracted " + outputPath + fileName);
                }
            }
        };
        zipExtractor.startExtraction();
    }


    public static LanguageDownloadDialogFragment newInstance(int languageIndex, Listener listener) {
        Bundle args = new Bundle();
        args.putInt("language_index", languageIndex);
        LanguageDownloadDialogFragment fragment = new LanguageDownloadDialogFragment();
        fragment.listener = listener;
        fragment.setArguments(args);
        return fragment;
    }

    interface Listener {
        void onDismiss(int languageIndex, boolean installSuccess);
    }

}

