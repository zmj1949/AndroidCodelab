package com.gaoxuan.developer.preview.r;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class EncurityCryptoHelper {

    private  static SharedPreferences getSharedPreferences(Context context)  {
        EncryptedSharedPreferences sharedPreferences = null;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences
                    .create(
                            "secret_shared_prefs",
                            masterKeyAlias,
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sharedPreferences;
    }

   public static void putString(Context context,String key,String value){
       SharedPreferences.Editor sharedPrefsEditor = getSharedPreferences(context).edit();
       sharedPrefsEditor.putString(key, value).commit();
   }
    public static String getString(Context context,String key){
        return getSharedPreferences(context).getString(key,"");
    }

}
