package ir.siaray.downloadmanagerplussample;

import android.app.DownloadManager.Request;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.pnikosis.materialishprogress.ProgressWheel;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.enums.DownloadReason;
import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.enums.Errors;
import ir.siaray.downloadmanagerplus.interfaces.ActionListener;
import ir.siaray.downloadmanagerplus.interfaces.DownloadListener;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static ir.siaray.downloadmanagerplussample.MainActivity.DOWNLOAD_DIRECTORY;

public class NormalActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private int notificationVisibility = Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        initSpinner();
        inflateUi();
    }

    private void initSpinner() {
        Spinner dropdown = findViewById(R.id.sp_notification_type);
        String[] items = new String[]{
                "VISIBILITY_VISIBLE_NOTIFY_COMPLETED",
                "VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION",
                "VISIBILITY_VISIBLE",
                "VISIBILITY_HIDDEN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        dropdown.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                notificationVisibility = Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

                break;
            case 1:
                notificationVisibility = Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;
                break;

            case 2:
                notificationVisibility = Request.VISIBILITY_VISIBLE;
                break;

            case 3:
                notificationVisibility = Request.VISIBILITY_HIDDEN;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        notificationVisibility = Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    }

    private void inflateUi() {
        ViewGroup itemContainer = findViewById(R.id.item_container);
        View fView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        itemContainer.addView(fView);
        FileItem fItem = SampleUtils.getDownloadItem(1);
        SampleUtils.setFileSize(getApplicationContext(), fItem);
        initUi(fView, fItem);

        View sView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        itemContainer.addView(sView);
        FileItem sItem = SampleUtils.getDownloadItem(2);
        SampleUtils.setFileSize(getApplicationContext(), sItem);
        initUi(sView, sItem);

        View tView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        itemContainer.addView(tView);
        FileItem tItem = SampleUtils.getDownloadItem(3);
        SampleUtils.setFileSize(getApplicationContext(), tItem);
        initUi(tView, tItem);
    }

    private void initUi(View view, final FileItem item) {
        final ImageView ivAction = view.findViewById(R.id.iv_image);
        final ViewGroup btnAction = view.findViewById(R.id.btn_action);
        final ViewGroup btnDelete = view.findViewById(R.id.btn_delete);
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvSize = view.findViewById(R.id.tv_size);
        TextView tvSpeed = view.findViewById(R.id.tv_speed);
        TextView tvPercent = view.findViewById(R.id.tv_percent);
        ProgressWheel progressWheel = view.findViewById(R.id.progress_wheel);
        final RoundCornerProgressBar downloadProgressBar = view.findViewById(R.id.progressbar);

        tvName.setText(Utils.getFileName(item.getUri()));

        final ActionListener deleteListener = getDeleteListener(ivAction
                , btnAction
                , downloadProgressBar
                , progressWheel
                , tvSize
                , tvSpeed
                , tvPercent);
        item.setListener(getDownloadListener(ivAction
                , downloadProgressBar
                , progressWheel
                , tvSize
                , tvSpeed
                , tvPercent));

        //Download Button
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOnActionButton(item);
            }
        });

        //Showing progress for running downloads.
        showProgress(item, item.getListener());

        //Delete Button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Downloader downloader = Downloader.getInstance(NormalActivity.this)
                        .setUrl(item.getUri())
                        .setListener(item.getListener());

                downloader.deleteFile(item.getToken(), deleteListener);
            }
        });
    }

    private void clickOnActionButton(FileItem item) {
        final Downloader downloader = getDownloader(item, item.getListener()/*listener*/);
        if (downloader.getStatus(item.getToken()) == DownloadStatus.RUNNING
                || downloader.getStatus(item.getToken()) == DownloadStatus.PAUSED
                || downloader.getStatus(item.getToken()) == DownloadStatus.PENDING)
            downloader.cancel(item.getToken());
        else if (downloader.getStatus(item.getToken()) == DownloadStatus.SUCCESSFUL) {
            Utils.openFile(NormalActivity.this, downloader.getDownloadedFilePath(item.getToken()));
        } else
            downloader.start();
    }

    private Downloader getDownloader(FileItem item, DownloadListener listener) {
        Downloader request = Downloader.getInstance(this)
                .setListener(listener)
                .setUrl(item.getUri())
                .setToken(item.getToken())
                .setKeptAllDownload(false)//if true: canceled download token keep in db
                .setAllowedOverRoaming(true)
                .setVisibleInDownloadsUi(true)
                .setDescription(Utils.readableFileSize(item.getFileSize()))
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(notificationVisibility)
                .setAllowedNetworkTypes(Request.NETWORK_WIFI | Request.NETWORK_MOBILE)
                .setDestinationDir(DOWNLOAD_DIRECTORY
                        , Utils.getFileName(item.getUri()))
                .setNotificationTitle(SampleUtils.getFileShortName(Utils.getFileName(item.getUri())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true); //Api 16 and higher
        }
        return request;
    }

    private ActionListener getDeleteListener(final ImageView ivAction
            , final ViewGroup btnDelete
            , final RoundCornerProgressBar downloadProgressBar
            , ProgressWheel progressWheel
            , final TextView tvSize
            , final TextView tvSpeed
            , final TextView tvPercent) {
        return new ActionListener() {
            @Override
            public void onSuccess() {
                ivAction.setImageResource(R.mipmap.ic_start);
                downloadProgressBar.setProgress(0);
                tvSize.setText(" Deleted");
                tvPercent.setText("0%");
                Toast.makeText(NormalActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Errors error) {
                Toast.makeText(NormalActivity.this, "" + error, Toast.LENGTH_SHORT).show();

            }
        };
    }

    private DownloadListener getDownloadListener(final ImageView ivAction
            , final RoundCornerProgressBar downloadProgressBar
            , final ProgressWheel progressWheel
            , final TextView tvSize
            , final TextView tvSpeed
            , final TextView tvPercent) {
        return new DownloadListener() {
            DownloadStatus lastStatus = DownloadStatus.NONE;
            long startTime = 0;
            int lastDownloadedBytes = 0;
            int lastPercent = 0;

            @Override
            public void onComplete(int totalBytes) {
                Log.i("onComplete");
                ivAction.setImageResource(R.mipmap.ic_complete);
                downloadProgressBar.setProgress(100);
                lastStatus = DownloadStatus.SUCCESSFUL;
                progressWheel.setVisibility(View.GONE);
                tvPercent.setText("100%");
                tvSize.setText(Utils.readableFileSize(totalBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Completed");
            }

            @Override
            public void onPause(int percent, DownloadReason reason, int totalBytes, int downloadedBytes) {
                if (lastStatus != DownloadStatus.PAUSED) {
                    Log.i("onPause - percent: " + percent
                            + " lastStatus:" + lastStatus
                            + " reason:" + reason);
                    ivAction.setImageResource(R.mipmap.ic_cancel);
                    downloadProgressBar.setProgress(percent);
                    progressWheel.setVisibility(View.VISIBLE);
                    tvPercent.setText(percent + "%");
                    tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes) + " - Paused");
                }
                lastStatus = DownloadStatus.PAUSED;
            }

            @Override
            public void onPending(int percent, int totalBytes, int downloadedBytes) {
                if (lastStatus != DownloadStatus.PENDING) {
                    Log.i("onPending - lastStatus:" + lastStatus);
                    ivAction.setImageResource(R.mipmap.ic_cancel);
                    downloadProgressBar.setProgress(percent);
                    progressWheel.setVisibility(View.VISIBLE);
                    tvPercent.setText(percent + "%");
                    tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes) + " - Pending");
                }
                lastStatus = DownloadStatus.PENDING;
            }

            @Override
            public void onFail(int percent, DownloadReason reason, int totalBytes, int downloadedBytes) {
                Toast.makeText(NormalActivity.this, "Failed: " + reason, Toast.LENGTH_SHORT).show();
                Log.i("onFail - percent: " + percent
                        + " lastStatus:" + lastStatus
                        + " reason:" + reason);
                ivAction.setImageResource(R.mipmap.ic_start);
                downloadProgressBar.setProgress(percent);
                lastStatus = DownloadStatus.FAILED;
                progressWheel.setVisibility(View.GONE);
                tvPercent.setText(percent + "%");
                tvSize.setText(Utils.readableFileSize(downloadedBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Failed");

            }

            @Override
            public void onCancel(int totalBytes, int downloadedBytes) {
                Log.i("onCancel");
                ivAction.setImageResource(R.mipmap.ic_start);
                downloadProgressBar.setProgress(0);
                lastStatus = DownloadStatus.CANCELED;
                progressWheel.setVisibility(View.GONE);
                tvPercent.setText("0%");
                tvSize.setText(Utils.readableFileSize(downloadedBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Canceled");
            }

            @Override
            public void onRunning(int percent, int totalBytes, int downloadedBytes, float downloadSpeed) {
                if(percent>lastPercent) {
                    Log.i("onRunning percent: " + percent);
                    lastPercent=percent;
                }
                ivAction.setImageResource(R.mipmap.ic_cancel);
                downloadProgressBar.setProgress(percent);
                lastStatus = DownloadStatus.RUNNING;
                progressWheel.setVisibility(View.GONE);
                tvPercent.setText(percent + "%");
                if (totalBytes < 0 || downloadedBytes < 0)
                    tvSize.setText("loading...");
                else
                    tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes));
                tvSpeed.setText(Math.round(downloadSpeed) + " KB/sec");

            }

        };
    }

    private void showProgress(FileItem item, DownloadListener listener) {
        getDownloader(item, listener).showProgress();
    }

}
