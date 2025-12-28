package com.example.aerotutorial.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.aerotutorial.models.AirQualityData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVExporter {

    private final Context context;

    public CSVExporter(Context context) {
        this.context = context;
    }

    public interface ExportCallback {
        void onSuccess(String filePath);
        void onFailure(String error);
    }

    public void exportResearchData(List<AirQualityData> dataList, ExportCallback callback) {
        if (dataList == null || dataList.isEmpty()) {
            callback.onFailure("No data to export");
            return;
        }

        try {
            // Create filename with timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            String filename = "AeroSafe_Research_Data_" + timestamp + ".csv";

            // Get external storage directory
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AeroSafe");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, filename);

            // Write CSV data
            FileWriter writer = new FileWriter(file);

            // Write header
            writer.append("Timestamp,Location,Latitude,Longitude,AQI,PM2.5,PM10,NO2,O3,SO2,CO\n");

            // Write data rows
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (AirQualityData data : dataList) {
                String dateStr = dateSdf.format(new Date(data.getTimestamp()));
                writer.append(String.format(Locale.US, "%s,%s,%.6f,%.6f,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                    dateStr,
                    data.getLocation().replace(",", ";"), // Escape commas in location
                    data.getLatitude(),
                    data.getLongitude(),
                    data.getAqi(),
                    data.getPm25(),
                    data.getPm10(),
                    data.getNo2(),
                    data.getO3(),
                    data.getSo2(),
                    data.getCo()
                ));
            }

            writer.flush();
            writer.close();

            // Share the file
            shareFile(file);

            callback.onSuccess(file.getAbsolutePath());

        } catch (IOException e) {
            callback.onFailure("Error creating CSV: " + e.getMessage());
        }
    }

    private void shareFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getApplicationContext().getPackageName() + ".fileprovider",
                file
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(Intent.createChooser(intent, "Share CSV File"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

