package com.ahd.oaid_flutter;

import android.content.Context;
import android.util.Log;

import com.bun.miitmdid.core.InfoCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.bun.miitmdid.pojo.IdSupplierImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceIdsHelper implements IIdentifierListener {

    public static final String GET_ID_TYPE_OAID = "OAID";
    public static final String GET_ID_TYPE_VAID = "VAID";
    public static final String GET_ID_TYPE_AAID = "AAID";
    
    private String getIdType;

    public static final String TAG = "DeviceIdsHelper";
    public static final int HELPER_VERSION_CODE = 20210801; // DeviceIdsHelper版本号
    private final AppIdsUpdater appIdsUpdater;
    private boolean isCertInit = false;

    public boolean isSDKLogOn = false;          // TODO （1）设置 是否开启sdk日志
    private String ASSET_FILE_NAME_CERT = "com.example.oaidtest2.cert.pem";  // TODO （2）设置 asset证书文件名

    public DeviceIdsHelper(AppIdsUpdater appIdsUpdater, String certFileName, String getIdType, boolean isSDKLogOn){
        this.getIdType = getIdType;
        this.isSDKLogOn = isSDKLogOn;
        ASSET_FILE_NAME_CERT = certFileName;  // TODO （3）加固版本在调用前必须载入SDK安全库
        // DeviceIdsHelper版本建议与SDK版本一致
//        if(MdidSdkHelper.SDK_VERSION_CODE != HELPER_VERSION_CODE){
//            Log.w(TAG,"SDK version not match.");
//        }
        this.appIdsUpdater = appIdsUpdater;
    }

    private int getSysCode(String sysCode) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(sysCode);
        m.find();
        int code = Integer.parseInt(m.group());
        return code;
    }
    private boolean getRealCert(Context cxt, String certStr) {
        isCertInit = MdidSdkHelper.InitCert(cxt, certStr);
        return isCertInit;
    }

    /**
     * 获取OAID
     * @param cxt
     */
    public void getDeviceIds(Context cxt){

        boolean certFlag = false;
        String mSystemVersionCodeStr = SystemUtil.getSystemVersion();
        int mSystemVersionCode = getSysCode(mSystemVersionCodeStr);
        // 初始化SDK证书
        if (!isCertInit) { // 证书只需初始化一次
            // 证书为PEM文件中的所有文本内容（包括首尾行、换行符）
            if (SystemUtil.getDeviceBrand().contains("vivo")) {//VIVO 9.0.0以上
                if (mSystemVersionCode >= 900) {
                    certFlag = true;
                }
            } else if (SystemUtil.getDeviceBrand().contains("华为")) {//鸿蒙系统2.6.2
                if (mSystemVersionCode >= 262) {
                    certFlag = true;
                }
            } else if (SystemUtil.getDeviceBrand().contains("小米")) {//MIUI系统10.2.0
                if (mSystemVersionCode >= 1020) {
                    certFlag = true;
                }
            } else if (SystemUtil.getDeviceBrand().contains("OPPO")) {//OPPO colorOS 6.0.0
                if (mSystemVersionCode >= 600) {
                    certFlag = true;
                }
            } else if (SystemUtil.getDeviceBrand().contains("联想")) {//联想 ZUi 11.4.0
                if (mSystemVersionCode >= 1140) {
                    certFlag = true;
                }
            } else if (SystemUtil.getDeviceBrand().contains("Realme")) {// colorOS 6.0.0
                if (mSystemVersionCode >= 600) {
                    certFlag = true;
                }
            } else {
                //三星 魅族 努比亚 中兴 华硕 一加 黑鲨 摩托罗拉 Freeme OS 酷赛 荣耀 酷派 10.0.0
                if (mSystemVersionCode >= 10) {               // 其他手机  10以上android系统
                    certFlag = true;
                }
            }
            if (certFlag) {
                getRealCert(cxt, ASSET_FILE_NAME_CERT);
            } else {
                getRealCert(cxt, "");
            }
            if (!isCertInit) {
                Log.e("Tag", "getDeviceIds: cert init failed");
            }
        }

        // TODO （4）初始化SDK证书
        if(!isCertInit){ // 证书只需初始化一次
            // 证书为PEM文件中的所有文本内容（包括首尾行、换行符）
            isCertInit = MdidSdkHelper.InitCert(cxt, loadPemFromAssetFile(cxt, ASSET_FILE_NAME_CERT));
            if(!isCertInit){
                Log.w(TAG, "getDeviceIds: cert init failed");
            }
        }

        //（可选）设置InitSDK接口回调超时时间(仅适用于接口为异步)，默认值为5000ms.
        // 注：请在调用前设置一次后就不再更改，否则可能导致回调丢失、重复等问题
        MdidSdkHelper.setGlobalTimeout(5000);

        // TODO （5）调用SDK获取ID
        int code = MdidSdkHelper.InitSdk(cxt, true, this);

        // TODO （6）根据SDK返回的code进行不同处理
        IdSupplierImpl unsupportedIdSupplier = new IdSupplierImpl();
        if(code == InfoCode.INIT_ERROR_CERT_ERROR){                         // 证书未初始化或证书无效，SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"oaid证书未初始化或证书无效");
            onSupport(unsupportedIdSupplier);
        }else if(code == InfoCode.INIT_ERROR_DEVICE_NOSUPPORT){             // 不支持的设备, SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"oaid不支持的设备");
            onSupport(unsupportedIdSupplier);
        }else if( code == InfoCode.INIT_ERROR_LOAD_CONFIGFILE){            // 加载配置文件出错, SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"oaid加载配置文件出错");
            onSupport(unsupportedIdSupplier);
        }else if(code == InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT){      // 不支持的设备厂商, SDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"oaid不支持的设备厂商");
            onSupport(unsupportedIdSupplier);
        }else if(code == InfoCode.INIT_ERROR_SDK_CALL_ERROR){             // sdk调用出错, SSDK内部不会回调onSupport
            // APP自定义逻辑
            Log.w(TAG,"oaid sdk调用出错");
            onSupport(unsupportedIdSupplier);
        } else if(code == InfoCode.INIT_INFO_RESULT_DELAY) {             // 获取接口是异步的，SDK内部会回调onSupport
            Log.i(TAG, "result delay (async)");
        }else if(code == InfoCode.INIT_INFO_RESULT_OK){                  // 获取接口是同步的，SDK内部会回调onSupport
            Log.i(TAG, "result ok (sync)");
        }else {
            // sdk版本高于DeviceIdsHelper代码版本可能出现的情况，无法确定是否调用onSupport
            // 不影响成功的OAID获取
            Log.w(TAG,"getDeviceIds: unknown code: " + code);
        }
    }

    /**
     * APP自定义的getDeviceIds(Context cxt)的接口回调
     * @param supplier
     */
    @Override
    public void onSupport(IdSupplier supplier) {
        if(supplier==null) {
            Log.w(TAG, "onSupport: supplier is null");
            return;
        }
        if(appIdsUpdater ==null) {
            Log.w(TAG, "onSupport: callbackListener is null");
            return;
        }
        // 获取Id信息
        // 注：IdSupplier中的内容为本次调用MdidSdkHelper.InitSdk()的结果，不会实时更新。 如需更新，需调用MdidSdkHelper.InitSdk()
        boolean isSupported = supplier.isSupported();
        boolean isLimited  = supplier.isLimited();
        String oaid=supplier.getOAID();
        String vaid=supplier.getVAID();
        String aaid=supplier.getAAID();

        //TODO (7) 自定义后续流程，以下显示到UI的示例
//        String idsText= "support: " + (isSupported ? "true" : "false") +
//                "\nlimit: " + (isLimited ? "true" : "false") +
//                "\nOAID: " + oaid +
//                "\nVAID: " + vaid +
//                "\nAAID: " + aaid + "\n";
//        Log.d(TAG, "onSupport: ids: \n" + idsText);
//        appIdsUpdater.onIdsValid(idsText);
        switch (getIdType) {
            case GET_ID_TYPE_OAID:
                appIdsUpdater.onIdsValid(oaid);
                break;
            case GET_ID_TYPE_VAID:
                appIdsUpdater.onIdsValid(vaid);
                break;
            case GET_ID_TYPE_AAID:
                appIdsUpdater.onIdsValid(aaid);
                break;
            default:
                appIdsUpdater.onIdsValid(null);
                break;
        }
    }

    public interface AppIdsUpdater{
        void onIdsValid(String ids);
    }

    /**
     * 从asset文件读取证书内容
     * @param context
     * @param assetFileName
     * @return 证书字符串
     */
    public static String loadPemFromAssetFile(Context context, String assetFileName){
        try {
            InputStream is = context.getAssets().open(assetFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null){
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "loadPemFromAssetFile failed");
            return "";
        }
    }
}

