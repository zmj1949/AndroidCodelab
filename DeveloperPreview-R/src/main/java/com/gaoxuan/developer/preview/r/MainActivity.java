package com.gaoxuan.developer.preview.r;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 0;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private TextView tvResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = findViewById(R.id.tv_result);
        LocationHelper.init(getApplicationContext());



    }
/*    @RequiresApi(api = Build.VERSION_CODES.R)
    private void auditDataAccess(){
        //审核对数据的访问
        AppOpsManager.OnOpNotedCallback appOpsCallback =
                new AppOpsManager.OnOpNotedCallback() {
                    private void logPrivateDataAccess(String opCode, String trace) {
                        Log.i("xdsd", "Private data accessed. " +
                                "Operation: $opCode\nStack Trace:\n$trace");
                    }

                    @Override
                    public void onNoted(@NonNull SyncNotedAppOp syncNotedAppOp) {
                        logPrivateDataAccess(syncNotedAppOp.getOp(),
                                Arrays.toString(new Throwable().getStackTrace()));
                    }

                    @Override
                    public void onSelfNoted(@NonNull SyncNotedAppOp syncNotedAppOp) {
                        logPrivateDataAccess(syncNotedAppOp.getOp(),
                                Arrays.toString(new Throwable().getStackTrace()));
                    }

                    @Override
                    public void onAsyncNoted(@NonNull AsyncNotedAppOp asyncNotedAppOp) {
                        logPrivateDataAccess(asyncNotedAppOp.getOp(),
                                asyncNotedAppOp.getMessage());
                    }
                };

        AppOpsManager appOpsManager = getSystemService(AppOpsManager.class);
        if (appOpsManager != null) {
            appOpsManager.setOnOpNotedCallback(getMainExecutor(), appOpsCallback);
        }
    }*/

    //测试是否会弹出一次的权限提示框，并且允许仅限这次后，下次启动还需要授权，看截图。
    public void oneTimePermissions(View view) {

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission();
        }
    }


    public void backgroundLocation(View view) {

      String location = LocationHelper.getLocation();
      tvResult.setText(location);

    }

    public void getMac(View view) {
         String mac = getWifiMacAddress();
         tvResult.setText("mac:"+mac);
    }

    public void biometric(View view) {

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                tvResult.setText("App can authenticate using biometrics.");
                BiometricManagerHelper.showBiometricPrompt(this);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                tvResult.setText("No biometric features available on this device.");

                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                tvResult.setText("Biometric features are currently unavailable.");

                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                tvResult.setText("The user hasn't associated any biometric credentials " +
                        "with their account.");
                break;
        }
    }




    @Override
    protected void onStop() {
        super.onStop();
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocation = getCurrentLocation(getApplicationContext(), mLocationManager);
                Log.d("gaoxuan","getCurrentLocation---");
            }
        },3000);*/

    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Toast.makeText(getApplicationContext(),"别墨迹，请授权",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"已授权",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),"未授权",Toast.LENGTH_SHORT).show();
            }

        }
    }






    private static String getWifiMacAddress() {
        String ret = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iF = interfaces.nextElement();

                byte[] addr = iF.getHardwareAddress();
                if (addr == null || addr.length == 0) {
                    continue;
                }

                StringBuilder buf = new StringBuilder();
                for (byte b : addr) {
                    buf.append(String.format("%02X:", b));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                String mac = buf.toString();

                if (TextUtils.equals(iF.getName(), "wlan0")) {
                    ret = mac;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = e.toString();
        }
        return String.valueOf(ret);
    }


    public void encurityCrypto(View view) {
        EncurityCryptoHelper.putString(getApplicationContext(),"test","test");
        String content = EncurityCryptoHelper.getString(getApplicationContext(),"test");
        Log.d("xdsd","content:"+content);
    }

    public void getApps(View view) {
        List<String> list =  getPkgListNew(getApplicationContext());
        tvResult.setText("list:"+list.size());
    }

    private List<String> getPkgListNew(Context context) {
        List<String> packages = new ArrayList<String>();
        List<String> systemApp = new ArrayList<String>();
        List<String> userApp = new ArrayList<String>();

        try {
            List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);
            Log.d("xdsd","packageInfos:"+packageInfos.size());

            for (PackageInfo info : packageInfos) {
                String pkg = info.packageName;
                packages.add(pkg);
                if (isSystemApp(info)){
                    systemApp.add(pkg);
                }else {
                    userApp.add(pkg);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();;
        }
        Log.d("xdsd","systemApp:"+systemApp.size()+",userApp:"+userApp.size());
        Log.d("xdsd","userApp:"+userApp);

        return packages;
    }

    private boolean isSystemApp(PackageInfo pi) {
        boolean isSysApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
        boolean isSysUpd = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
        return isSysApp || isSysUpd;
    }
}
