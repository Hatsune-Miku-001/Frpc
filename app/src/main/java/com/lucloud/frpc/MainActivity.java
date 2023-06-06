package com.lucloud.frpc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private boolean storagePermissionGranted = false;
    private static final String FRPC_EXECUTABLE_NAME = "frpc";
    private static final String FRPC_INI_FILE_NAME = "frpc.ini";

    private boolean isFrpRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 请求存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                storagePermissionGranted = true;
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            storagePermissionGranted = true;
        }

        Button startStopButton = findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFrpRunning) {
                    startFrpService();
                } else {
                    stopFrpService();
                }
            }
        });

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            checkFrpInstallation();
        }
    }

    private void checkFrpInstallation() {
        File frpExecutable = new File(getFrpExecutablePath());
        if (frpExecutable.exists()) {
            Toast.makeText(this, "frpc已安装", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "frpc未安装，开始下载", Toast.LENGTH_SHORT).show();
            startFrpDownload();
        }
    }

    private String getFrpExecutablePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + FRPC_EXECUTABLE_NAME;
    }

    private void startFrpDownload() {
        // 使用系统方式下载frpc
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hatsune-Miku-001/Frpc/raw/main/frpc"));
        startActivity(browserIntent);
    }

    private void startFrpService() {
        String frpCommand = getFrpExecutablePath() + " -c " + getFrpIniFilePath();
        // 启动frpc服务
        // 执行frpCommand命令，例如通过Runtime.getRuntime().exec(frpCommand)来运行frpc
        try {
            Runtime.getRuntime().exec(frpCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isFrpRunning = true;
        updateButtonState();
    }

    private void stopFrpService() {
        // 停止frpc服务
        isFrpRunning = false;
        updateButtonState();
    }

    private String getFrpIniFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + FRPC_INI_FILE_NAME;
    }

    private void updateButtonState() {
        Button startStopButton = findViewById(R.id.start_stop_button);
        if (isFrpRunning) {
            startStopButton.setText("停止frpc");
        } else {
            startStopButton.setText("启动frpc");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermissionGranted = true;
                checkFrpInstallation();
            } else {
                Toast.makeText(this, "没有存储权限，无法下载frpc", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
