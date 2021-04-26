package com.gaoxuan.developer.preview.r;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationHelper {

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static String mCountry = "";
    private static String mCity = "";
    private static String mArea = ""; // region
    private static String mDistrict = "";
    private static String mAddress = "";
    private static String mStreet = "";
    private static String mStreetNo = "";
    private static String mBusiness = "";//商圈
    private static Location mLastLocation;
    private static Location mLocation;
    private static LocationManager mLocationManager;

    public static void init(Context context){
        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        mLocation = getCurrentLocation(context, mLocationManager);
    }


    public static String getLocation(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mCountry",mCountry);
            jsonObject.put("mCity",mCity);
            jsonObject.put("mArea",mArea);
            jsonObject.put("mAddress",mAddress);
            jsonObject.put("mDistrict",mDistrict);
            jsonObject.put("mStreet",mStreet);
            jsonObject.put("mStreetNo",mStreetNo);
            jsonObject.put("mBusiness",mBusiness);
            jsonObject.put("lat",getLat(mLocation));
            jsonObject.put("lng",getLng(mLocation));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private static Double getLng(Location location) {
        Double ret = 0.0;
        if (location != null)
            ret = location.getLongitude();
        return ret;
    }

    private static Double getLat(Location location) {
        Double ret = 0.0;
        if (location != null) {
            ret = location.getLatitude();
        }
        return ret;
    }
    @SuppressLint("MissingPermission")
    private static Location getCurrentLocation(final Context context, final LocationManager locationManager) {
        Location _LastLocation = null;
        try {
            if (locationManager != null) {

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                           /* if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }*/
                        try {

                            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                                if(myNetworkLocationListener==null){
                                    myNetworkLocationListener = new MyNetworkLocationListener();
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, myNetworkLocationListener);
                                }
                            }

                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                                if (myGPSLocationListener==null){
                                    myGPSLocationListener= new MyGPSLocationListener();
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, myGPSLocationListener);
                                }

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

                if (mLastLocation != null) {
                    _LastLocation = mLastLocation;
                }else {
                    _LastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (_LastLocation == null){
                        _LastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (_LastLocation != null) {
            final Location final_LastLocation = _LastLocation;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    try {
                        Geocoder gc = new Geocoder(context, Locale.getDefault());
                        List<Address> result = gc.getFromLocation(final_LastLocation.getLatitude(), final_LastLocation.getLongitude(), 1);
                        if (result != null && !result.isEmpty()) {
                            Address a = result.get(0);
                            mAddress = a.getAddressLine(0);//具体位置
                            mCountry = a.getCountryName();//国家
                            mArea = a.getAdminArea();//省
                            mDistrict = TextUtils.isEmpty(a.getSubLocality())?a.getSubAdminArea():a.getSubLocality();
                            mCity = a.getLocality();
                            mStreet = a.getThoroughfare();//街道
                            mStreetNo = a.getSubThoroughfare();
                            if (result.size()>=2){
                                mBusiness = a.getAddressLine(1);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
        return _LastLocation;
    }
    private static MyNetworkLocationListener myNetworkLocationListener;
    private static MyGPSLocationListener myGPSLocationListener;




    private static final class MyNetworkLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location newLocation) {
            if (isBetterLocation(newLocation,mLastLocation)){
                mLastLocation = newLocation;
            }
            removeLocationListener();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            removeLocationListener();
        }

    }

    private static final class MyGPSLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location newLocation) {
            if (isBetterLocation(newLocation,mLastLocation)){
                mLastLocation = newLocation;
            }
            removeLocationListener();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            removeLocationListener();
        }

    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private static void removeLocationListener(){
        if (mLocationManager != null) {
            if (myNetworkLocationListener!=null){
                mLocationManager.removeUpdates(myNetworkLocationListener);
                myNetworkLocationListener=null;
            }
            if (myGPSLocationListener!=null){
                mLocationManager.removeUpdates(myGPSLocationListener);
                myGPSLocationListener=null;
            }
        }

    }
}
