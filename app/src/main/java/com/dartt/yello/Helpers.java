package com.dartt.yello;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

class CustomZipExtractor {
    int fileCount = 0;
    final File zipFile;
    final String outputPath;
    final Handler mainThread;
    Thread zipExtractorThread;

    CustomZipExtractor(Context context, File zipFile, String outputPath) {
        this.zipFile = zipFile;
        this.outputPath = outputPath;
        mainThread = new Handler(context.getMainLooper());
    }

    void startExtraction() {
        zipExtractorThread = new Thread(new ZipExtractor(zipFile, outputPath) {
            @Override
            void onGetFileCount(final int fileCount) {
                CustomZipExtractor.this.fileCount = fileCount;
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomZipExtractor.this.onGetFileCount(fileCount);
                    }
                });
            }

            @Override
            void onProgress(final int progress) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomZipExtractor.this.onProgress(progress);
                    }
                });
            }

            @Override
            void onExtractionFailed(final Exception exception) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomZipExtractor.this.onExtractionFailed(exception);
                    }
                });
            }

            @Override
            void onExtractionComplete(final String[] fileNames, final String outputPath) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomZipExtractor.this.onExtractionComplete(fileNames, outputPath);
                    }
                });
            }
        });
        zipExtractorThread.start();
    }

    void onGetFileCount(int fileCount) {

    }

    void onProgress(int progress) {

    }

    void onExtractionFailed(Exception exception) {

    }

    void onExtractionComplete(String[] fileNames, String outputPath) {

    }
}

abstract class ZipExtractor implements Runnable {
    final File zipFile;
    final String outputPath;

    ZipExtractor(File zipFile, String outputPath) {
        this.zipFile = zipFile;
        this.outputPath = outputPath;
    }

    @Override
    public void run() {

        try {
            int fileCount = 0;
            ZipEntry zipEntry;
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            while (zipInputStream.getNextEntry() != null) {
                fileCount++;
                zipInputStream.closeEntry();
            }
            onGetFileCount(fileCount);
            zipInputStream.close();
            zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            new File(outputPath).mkdirs();

            int i = 0;
            int readLength;
            byte[] buffer = new byte[1024];
            String[] fileNames = new String[fileCount];

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                fileNames[i] = zipEntry.getName();
                //if (zipEntry.isDirectory()) {File fmd = new File(outputPath, fileNames[i]);fmd.mkdirs();continue;}
                FileOutputStream zipOutputStream = new FileOutputStream(new File(outputPath, fileNames[i]));
                while ((readLength = zipInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, readLength);
                }
                zipOutputStream.close();
                zipInputStream.closeEntry();
                onProgress(++i);
            }
            zipInputStream.close();
            onExtractionComplete(fileNames, outputPath);
        } catch (IOException e) {
            onExtractionFailed(e);
        }
    }

    abstract void onGetFileCount(int fileCount);

    abstract void onProgress(int progress);

    abstract void onExtractionFailed(Exception exception);

    abstract void onExtractionComplete(String[] fileNames, String outputPath);
}

abstract class Downloader implements Runnable {

    final String LOG_TAG = "Downloader";

    Context context;
    String downloadUrl;
    String outputPath;

    Downloader(Context context, String downloadUrl, String outputPath) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.outputPath = outputPath;
    }

    long getLastModified() {
        return context.getSharedPreferences("temp_downloads", Context.MODE_PRIVATE).getLong(downloadUrl, 0);
    }

    void setLastModified(long lastModified) {
        context.getSharedPreferences("temp_downloads", Context.MODE_PRIVATE).edit().putLong(downloadUrl, lastModified).apply();
    }

    @Override
    public void run() {
        InputStream httpInputStream;
        FileOutputStream fileOutputStream;
        HttpsURLConnection httpsURLConnection;
        File cachedFile = new File(outputPath);
        try {
            final URL url = new URL(downloadUrl);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setConnectTimeout(15000);
            httpsURLConnection.setReadTimeout(15000);
            httpsURLConnection.setRequestMethod("GET");

            long serverFileSize = httpsURLConnection.getContentLength();
            long serverLastModified = httpsURLConnection.getLastModified();
            long cachedFileSize = 0;
            onGetFileSize(serverFileSize);
            if (cachedFile.exists()) {
                cachedFileSize = cachedFile.length();
                long cacheLastModified = getLastModified();
                if (cachedFileSize == serverFileSize && serverLastModified == cacheLastModified) {
                    onDownloadCompleted(cachedFile, true);
                    return;
                }

                if (serverLastModified == cacheLastModified) {
                    httpsURLConnection.disconnect();
                    httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.setDoInput(true);
                    httpsURLConnection.setConnectTimeout(15000);
                    httpsURLConnection.setReadTimeout(15000);
                    httpsURLConnection.setRequestMethod("GET");
                    httpsURLConnection.setRequestProperty("Range", "bytes=" + cachedFileSize + "-" + serverFileSize);
                    fileOutputStream = new FileOutputStream(cachedFile, true);
                    onDownloadProgressed(cachedFileSize);
                }
                else {
                    setLastModified(serverLastModified);
                    fileOutputStream = new FileOutputStream(cachedFile);
                }

            }
            else {
                setLastModified(serverLastModified);
                cachedFile.createNewFile();
                fileOutputStream = new FileOutputStream(cachedFile);
            }

            // Start Downloading
            final int responseCode;
            responseCode = httpsURLConnection.getResponseCode();
            if (responseCode == 200 || responseCode == 206) {
                try {
                    httpInputStream = httpsURLConnection.getInputStream();
                    final byte[] buffer = new byte[4 * 1024];
                    int length = -1;
                    long finished = cachedFile.length();
                    long start = System.currentTimeMillis();
                    while ((length = httpInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, length);
                        finished += length;
                        if (System.currentTimeMillis() - start > 1000) {
                            onDownloadProgressed(finished);
                            start = System.currentTimeMillis();
                        }
                        if (Thread.currentThread().isInterrupted()) {
                            httpInputStream.close();
                            fileOutputStream.close();
                            httpsURLConnection.disconnect();
                            Log.v(LOG_TAG, "Download Interrupted");
                            onDownloadInterrupted();
                            return;
                        }
                    }
                    httpInputStream.close();
                    fileOutputStream.close();
                    httpsURLConnection.disconnect();
                    onDownloadCompleted(cachedFile, false);
                    Log.d(LOG_TAG, "Download success");
                } catch (IOException e) {
                    e.printStackTrace();
                    String errorMessage = "Failed to download. Exception: " + e.getMessage();
                    Log.e(LOG_TAG, errorMessage);
                    onDownloadFailed(errorMessage);
                }
            }
            else if (responseCode == 404) {
                String errorMessage = "File not found on server: " + responseCode;
                Log.e(LOG_TAG, errorMessage);
                onDownloadFailed(errorMessage);
            }
            else {
                String errorMessage = "Unexpected response code from server: " + responseCode;
                Log.e(LOG_TAG, errorMessage);
                onDownloadFailed(errorMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Failed to download. Cache related code: " + e.getMessage();
            Log.e(LOG_TAG, errorMessage);
            onDownloadFailed(errorMessage);
        }
    }

    abstract void onGetFileSize(long fileSize);

    abstract void onDownloadProgressed(long progress);

    abstract void onDownloadCompleted(File file, boolean cached);

    abstract void onDownloadFailed(String errorMessage);

    abstract void onDownloadInterrupted();
}

class CustomDownloadManager {
    Context context;
    String downloadUrl;
    String outputLocation;
    int maxRetries;
    int tryCount;
    long fileSize = 1;
    long progress;
    Thread downloadThread;

    Handler mainThread;

    CustomDownloadManager(Context context, String downloadUrl, String outputLocation, int maxRetries) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.outputLocation = outputLocation;
        this.maxRetries = maxRetries;
        mainThread = new Handler(context.getMainLooper());
    }

    void startDownloading() {
        downloadThread = new Thread(createDownloader());
        downloadThread.start();
        tryCount++;
    }


    private Downloader createDownloader() {
        return new Downloader(context, downloadUrl, outputLocation) {
            @Override
            void onGetFileSize(final long fileSize) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomDownloadManager.this.onGetFileSize(fileSize);
                    }
                });
            }

            @Override
            void onDownloadProgressed(final long progress) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomDownloadManager.this.onDownloadProgressed(progress);
                    }
                });
            }

            @Override
            void onDownloadCompleted(final File file, final boolean cached) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomDownloadManager.this.onDownloadCompleted(file, cached);
                    }
                });
            }

            @Override
            void onDownloadFailed(final String errorMessage) {
                if (tryCount < maxRetries) {
                    startDownloading();
                    mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            CustomDownloadManager.this.onDownloadFailed(errorMessage, true);
                        }
                    });
                }
                else {
                    mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            CustomDownloadManager.this.onDownloadFailed(errorMessage, false);
                        }
                    });
                }
            }

            @Override
            void onDownloadInterrupted() {

            }
        };
    }

    void onGetFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    void onDownloadProgressed(long progress) {
        this.progress = progress;
    }

    void onDownloadCompleted(File file, boolean cached) {

    }

    void onDownloadFailed(String errorMessage, boolean willRetry) {

    }

    public final void interrupt() {
        downloadThread.interrupt();
    }
}


interface FunctionParameter {
    void execute();
}


abstract class Undo {
    private final String TAG = "Undo";
    private final ArrayList<Undoable> states = new ArrayList<>();
    private ViewContainer activeViewContainer;
    private JSONObject activeViewContainerOldState;
    int index = -1;

    void onUndoClick() {
        try {
            if (index >= 0) {
                states.get(index).undo();
                index--;
                onUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error Occurred while copying old state" + e.toString());
        }
    }

    void onRedoClick() {
        try {
            if (index < states.size() - 1) {
                states.get(index + 1).redo();
                index++;
                onUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error Occurred while copying new state" + e.toString());
        }

    }

    void beforeEdit(ViewContainer viewContainer) {
        try {
            activeViewContainer = viewContainer;
            activeViewContainerOldState = viewContainer.toJSON();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occurred while recording old state: " + e.toString());
        }
    }

    void afterEdit(ViewContainer viewContainer) {
        if (activeViewContainer == viewContainer) {
            try {
                JSONObject newState = viewContainer.toJSON();
                // Add to state if old state and new state are different
                if (!activeViewContainerOldState.toString().equals(newState.toString())) {
                    states.subList(++index, states.size()).clear();
                    states.add(new ViewContainerState(viewContainer, activeViewContainerOldState, newState));
                }
                onUpdate();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error occurred while recording new state: " + e.toString());
            }
        }
        else {
            activeViewContainer = null;
            Log.d(TAG, "Object Mismatch");
        }
    }

    void onElementAdded(ViewContainer viewContainer) {
        try {
            states.subList(++index, states.size()).clear();
            states.add(new ViewContainerState(viewContainer, null, viewContainer.toJSON()));
            onUpdate();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error occurred while recording new state: " + e.toString());
        }
    }

    void addState(Undoable state) {
        states.subList(++index, states.size()).clear();
        states.add(state);
        onUpdate();
    }

    void onUpdate() {
        boolean undoVisible = false;
        boolean redoVisible = false;
        if (index > -1) {
            undoVisible = true;
        }
        if (index < states.size() - 1) {
            redoVisible = true;
        }
        changeButtonState(undoVisible, redoVisible);
    }

    abstract void changeButtonState(boolean undoVisible, boolean redoVisible);

    static class ViewContainerState implements Undoable {
        final ViewContainer element;
        final JSONObject oldJsonState;
        final JSONObject newJsonState;

        ViewContainerState(ViewContainer viewContainer, JSONObject oldJsonState, JSONObject newJsonState) {
            this.element = viewContainer;
            this.oldJsonState = oldJsonState;
            this.newJsonState = newJsonState;
        }

        @Override
        public void undo() {
            try {
                element.copyAttrFromJSON(oldJsonState);
            } catch (JSONException e) {
                Log.e("ViewState", "Error occurred while undoing: " + e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void redo() {
            try {
                element.copyAttrFromJSON(newJsonState);
            } catch (JSONException e) {
                Log.e("ViewState", "Error occurred while redoing: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    interface Undoable {
        void undo();

        void redo();
    }

}

interface CustomItemSelectListener {
    void onItemSelected(int position);
}

class CustomHtml {
    public static String toHtml(Spannable text) {
        final SpannableStringBuilder ssBuilder = new SpannableStringBuilder(text);
        int start, end;

        // Replace Style spans with <b></b> or <i></i>
        StyleSpan[] styleSpans = ssBuilder.getSpans(0, text.length(), StyleSpan.class);
        for (int i = styleSpans.length - 1; i >= 0; i--) {
            StyleSpan span = styleSpans[i];
            start = ssBuilder.getSpanStart(span);
            end = ssBuilder.getSpanEnd(span);
            ssBuilder.removeSpan(span);
            if (span.getStyle() == Typeface.BOLD) {
                ssBuilder.insert(start, "<b>");
                ssBuilder.insert(end + 3, "</b>");
            }
            else if (span.getStyle() == Typeface.ITALIC) {
                ssBuilder.insert(start, "<i>");
                ssBuilder.insert(end + 3, "</i>");
            }
        }

        // Replace underline spans with <u></u>
        UnderlineSpan[] underSpans = ssBuilder.getSpans(0, ssBuilder.length(), UnderlineSpan.class);
        for (int i = underSpans.length - 1; i >= 0; i--) {
            UnderlineSpan span = underSpans[i];
            start = ssBuilder.getSpanStart(span);
            end = ssBuilder.getSpanEnd(span);
            ssBuilder.removeSpan(span);
            ssBuilder.insert(start, "<u>");
            ssBuilder.insert(end + 3, "</u>");
        }
        SparseArray<String> strings = new SparseArray<>();
        StringBuilder finalString = new StringBuilder();
        ForegroundColorSpan[] foregroundSpans = ssBuilder.getSpans(0, ssBuilder.length(), ForegroundColorSpan.class);
        if (foregroundSpans.length == 0) {
            finalString.append(ssBuilder);
        }
        else {
            int lastSpanEnd = 0;
            for (int i = foregroundSpans.length - 1; i >= 0; i--) {
                ForegroundColorSpan span = foregroundSpans[i];
                start = ssBuilder.getSpanStart(span);
                end = ssBuilder.getSpanEnd(span);
                ssBuilder.removeSpan(span);
                String colorCode = Integer.toHexString(span.getForegroundColor()).substring(2);
                String spanBegin = String.format("<span style=\"color:#%s;\">", colorCode);
                strings.append(start, spanBegin + ssBuilder.subSequence(start, end) + "</span>");
                if (lastSpanEnd < end) {
                    lastSpanEnd = end;
                }
            }
            int prevEnd = 0;
            while (strings.size() > 0) {
                int min = strings.keyAt(0);
                for (int i = 0; i < strings.size(); i++) {
                    if (strings.keyAt(i) < min) {
                        min = strings.keyAt(i);
                    }
                }
                finalString.append(ssBuilder.subSequence(prevEnd, min));
                prevEnd = strings.get(min).length() + min - 36;
                finalString.append(strings.get(min));
                strings.remove(min);
            }
            if (prevEnd < ssBuilder.length()) {
                finalString.append(ssBuilder.subSequence(prevEnd, ssBuilder.length()));
            }
        }

        replace(finalString, '\n', "<br/>");

        return finalString.toString();
    }

    private static void replace(StringBuilder b, char oldChar, String newStr) {
        for (int i = b.length() - 1; i >= 0; i--) {
            if (b.charAt(i) == oldChar) {
                b.replace(i, i + 1, newStr);
            }
        }
    }
}
