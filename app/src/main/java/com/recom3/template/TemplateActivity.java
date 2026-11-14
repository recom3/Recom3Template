package com.recom3.template;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;

import java.util.Locale;

public final class TemplateActivity extends Activity implements LocationListener {
    private static final long FIX_TIMEOUT_MS = 5000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable expireFix = new Runnable() {
        @Override public void run() {
            fix = null;
            render();
        }
    };

    private TemplateHud hud;
    private LocationManager locationManager;
    private Location fix;
    private boolean gpsAvailable;
    private boolean gpsEnabled;
    private boolean permissionDenied;
    private boolean listening;
    private boolean paused;
    private boolean showAccuracy;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            paused = state.getBoolean("paused");
            showAccuracy = state.getBoolean("showAccuracy");
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsAvailable = locationManager != null
                && locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER);
        hud = new TemplateHud(this);
        setContentView(hud);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        startGps();
        render();
    }

    @Override protected void onPause() {
        stopGps();
        super.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("paused", paused);
        state.putBoolean("showAccuracy", showAccuracy);
    }

    private void startGps() {
        if (!gpsAvailable || paused || listening) return;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f,
                    this, Looper.getMainLooper());
            listening = true;
            permissionDenied = false;
        } catch (SecurityException exception) {
            permissionDenied = true;
        }
    }

    private void stopGps() {
        clearFix();
        if (listening) {
            listening = false;
            locationManager.removeUpdates(this);
        }
    }

    private void clearFix() {
        handler.removeCallbacks(expireFix);
        fix = null;
    }

    @Override public void onLocationChanged(Location location) {
        if (!listening || !gpsEnabled) return;

        // API 16 lacks a monotonic timestamp on Location. Filter stale deliveries using
        // their wall-clock age, then expire the remaining display time on the main looper.
        long age = Math.max(0L, System.currentTimeMillis() - location.getTime());
        if (age >= FIX_TIMEOUT_MS) return;
        fix = location;
        handler.removeCallbacks(expireFix);
        handler.postAtTime(expireFix, SystemClock.uptimeMillis() + FIX_TIMEOUT_MS - age);
        render();
    }

    @Override public void onProviderEnabled(String provider) {
        if (!listening || !LocationManager.GPS_PROVIDER.equals(provider)) return;
        gpsEnabled = true;
        render();
    }

    @Override public void onProviderDisabled(String provider) {
        if (!listening || !LocationManager.GPS_PROVIDER.equals(provider)) return;
        gpsEnabled = false;
        clearFix();
        render();
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {
        // Provider enablement and sample timeout are handled independently.
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!event.isCanceled()) {
                    showAccuracy = !showAccuracy;
                    render();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                if (!event.isCanceled() && gpsAvailable) {
                    paused = !paused;
                    if (paused) stopGps();
                    else startGps();
                    render();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private int gpsStatus() {
        if (!gpsAvailable) return R.string.gps_unavailable;
        if (paused) return R.string.gps_paused;
        if (permissionDenied) return R.string.gps_denied;
        if (!gpsEnabled) return R.string.gps_off;
        return fix == null ? R.string.gps_searching : R.string.gps_ready;
    }

    private void render() {
        int metric = showAccuracy ? R.string.metric_accuracy : R.string.metric_speed;
        int unit = showAccuracy ? R.string.unit_accuracy : R.string.unit_speed;
        int controls = R.string.controls_metric;
        if (gpsAvailable) {
            controls = paused ? R.string.controls_resume : R.string.controls_pause;
        }
        String value = getString(R.string.no_value);
        if (fix != null) {
            if (showAccuracy && fix.hasAccuracy()) {
                value = String.format(Locale.getDefault(), "%.0f", fix.getAccuracy());
            } else if (!showAccuracy && fix.hasSpeed()) {
                value = String.format(Locale.getDefault(), "%.0f", fix.getSpeed() * 3.6f);
            }
        }
        hud.show(getString(gpsStatus()), fix != null, getString(metric), value,
                getString(unit), getString(controls));
    }
}
