package com.example.aerotutorial.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

public class LocationHelper {

    private final Context context;
    private LocationManager locationManager;

    public LocationHelper(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (locationManager == null) {
            callback.onLocationError("Location services not available");
            return;
        }

        // Check permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        // Try to get last known location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            // Try network provider
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            callback.onLocationReceived(location.getLatitude(), location.getLongitude());
        } else {
            callback.onLocationError("Unable to get current location");
        }
    }

    public boolean isLocationEnabled() {
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}

