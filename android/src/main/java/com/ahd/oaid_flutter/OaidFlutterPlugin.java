package com.ahd.oaid_flutter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.bun.miitmdid.core.MdidSdkHelper;

import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** OaidFlutterPlugin */
public class OaidFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity mActivity;
  private Context mContext;
  private String productUA;
  private String imei;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    System.loadLibrary("msaoaidsec");
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "oaid_flutter");
    channel.setMethodCallHandler(this);
    mContext = flutterPluginBinding.getApplicationContext();
  }

  private String certFilename = "";
  private boolean isLogOn = false;
  private boolean isInit = false;

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        String version = android.os.Build.VERSION.RELEASE;
        result.success("Android " + version);
        break;
      case "getSdkVersion":
        result.success(MdidSdkHelper.SDK_VERSION_CODE);
        break;
      case "init":
//        System.loadLibrary("nllvm1630571663641560568");
//        System.loadLibrary("msaoaidsec");
        certFilename = call.argument("certFileName");
        isLogOn = call.argument("isLogOn");
        isInit = true;
        result.success(true);
        break;
      case "getOaid":
        getDeviceId(result, DeviceIdsHelper.GET_ID_TYPE_OAID);
        break;
      case "getVaid":
        getDeviceId(result, DeviceIdsHelper.GET_ID_TYPE_VAID);
        break;
      case "getAaid":
        getDeviceId(result, DeviceIdsHelper.GET_ID_TYPE_AAID);
        break;
      case "getUa":
        getUa(result);
        break;
      case "getImei":
        getImei(result);
        break;
      case "launch":
        launchApp(result, call);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private void launchApp(final Result result, MethodCall call) {
    String packageName = call.argument("packageName");
    if (isInstallApp(packageName)) {
      mActivity.startActivity(mContext.getPackageManager().getLaunchIntentForPackage(packageName));
      result.success(true);
    } else {
      result.success(false);
    }
  }

  private boolean isInstallApp(String name) {
    final PackageManager packageManager = mContext.getPackageManager();
    List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
    for (int i = 0; i < pinfo.size(); i++) {
      if (pinfo.get(i).packageName.equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  private void getUa(final Result result) {
    productUA = new WebView(mActivity).getSettings().getUserAgentString();

    if (productUA == null) {
      productUA = "";
    }
    if("".equals(productUA)){
      productUA = System.getProperty("http.agent");
    }

    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        result.success(productUA);
      }
    });
  }

  private void getImei(final Result result) {
    TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        imei = telephonyManager.getImei();
      } catch (Exception e) {
        Log.e("OaidPlugin", "imei get error:" + e.toString());
      }
    }
    if (imei == null) {
      imei = "";
    }
    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        result.success(imei);
      }
    });
  }

  private void getDeviceId(final Result result, String getType) {
//    if (!isInit) {
//      certFilename = "com.ahd.ahd_fun_camera.cert.pem";
////      System.loadLibrary("nllvm1630571663641560568");
//      System.loadLibrary("msaoaidsec");
//    }
    try {
      new DeviceIdsHelper(new DeviceIdsHelper.AppIdsUpdater() {
        @Override
        public void onIdsValid(final String ids) {
          mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              result.success(ids);
            }
          });
        }
      }, certFilename, getType, isLogOn).getDeviceIds(mActivity);
    } catch (Error error) {
      result.success("");
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    mActivity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    mActivity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    mActivity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    mActivity = null;
  }
}
