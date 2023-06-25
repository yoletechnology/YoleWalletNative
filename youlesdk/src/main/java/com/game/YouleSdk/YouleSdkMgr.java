package com.game.YouleSdk;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

//import com.applovin.mediation.MaxAdFormat;
//import com.applovin.mediation.ads.MaxAdView;
//import com.applovin.mediation.ads.MaxInterstitialAd;
//import com.applovin.mediation.ads.MaxRewardedAd;
//import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
//import com.applovin.sdk.AppLovinPrivacySettings;
//import com.applovin.sdk.AppLovinSdk;
//import com.applovin.sdk.AppLovinSdkConfiguration;
//import com.game.AppLovinSdk.AppLovinMgr;
import com.game.MobileAdsSDK.MobileAdsMgr;
import com.game.PaySDKManager.PaySdkMgr;
import com.transsion.pay.paysdk.manager.PaySDKManager;
import com.transsion.pay.paysdk.manager.entity.ConvertPriceInfo;
import com.transsion.pay.paysdk.manager.entity.CountryCurrencyData;
import com.transsion.pay.paysdk.manager.entity.OrderEntity;
import com.transsion.pay.paysdk.manager.entity.StartPayEntity;
import com.transsion.pay.paysdk.manager.entity.SupportPayInfoEntity;
import com.transsion.pay.paysdk.manager.inter.CurrencyConvertCallBack;
import com.transsion.pay.paysdk.manager.inter.InitResultCallBack;
import com.transsion.pay.paysdk.manager.inter.StartPayCallBack;
import com.transsion.pay.paysdk.manager.testmode.SMSTestUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;




public class YouleSdkMgr {

    public enum PayMode {
        PAY_MODE_SMS,
        PAY_MODE_ONLINE,
        PAY_MODE_ALL,
        PAY_MODE_Native,//本地 先短信 失败后一直广告
    }
    private String TAG = "YouleSdkMgr";
    private static  YouleSdkMgr _instance = null;
    private CountryCurrencyData tempData = null;
    private Context var =  null;
    private PhoneInfo info =  null;
    private String payOrderNum = "tank";//支付的订单号
    private HashMap<String,String> list;
    private boolean isPlayerIng = false;
    private boolean isVSAFail = false;


    private String appkey = "";
    private String model = "";
    private String AP_ID = "";
    private String CP_ID = "";
    private String API_KEY = "";
    private String rewardedAdId = "";
    private boolean isMakeText = false;
    public static YouleSdkMgr getsInstance() {
        if(YouleSdkMgr._instance == null)
        {
            YouleSdkMgr._instance = new YouleSdkMgr();
        }
        return YouleSdkMgr._instance;
    }
    private YouleSdkMgr() {
        Log.e(TAG,"YouleSdkMgr");
    }
    public void initAd(Context var1, HashMap<String,String> var2,boolean isDebugger,boolean isMakeText)
    {

        list = var2;
        appkey = list.get("appkey");
        model = list.get("model");
        AP_ID = list.get("AP_ID");
        CP_ID = list.get("CP_ID");
        API_KEY = list.get("API_KEY");
        rewardedAdId = list.get("RewardedAdId");

        var = var1;
        MobileAdsMgr.getsInstance().initAd(var1);
        info = new PhoneInfo(var1);
        this.isMakeText = isMakeText;
        if(isDebugger == true)
        {
            PaySdkMgr.getsInstance().setTestMode();
        }

        PaySdkMgr.getsInstance().setStrict(true);
        PaySdkMgr.getsInstance().initAriesPay(var1,AP_ID,CP_ID,API_KEY,StartPayEntity.PAY_MODE_ALL, new InitResultCallBack() {

            @Override
            public void onSuccess(List<SupportPayInfoEntity> list, boolean b, CountryCurrencyData countryCurrencyData) {
                Log.i(TAG,"onSuccess:"+
                        countryCurrencyData.countryCode + "	"+
                        countryCurrencyData.currency + " "+
                        countryCurrencyData.mcc +" "+
                        countryCurrencyData.smsOptimalAmount);
                        tempData = countryCurrencyData;
            }

            @Override
            public void onFail(int i) {
                Log.i(TAG,"onFail:"+i);
            }
        });


    }
    public void preloadAd(Activity var1)
    {
        MobileAdsMgr.getsInstance().preloadAd(var1,rewardedAdId);
        MobileAdsMgr.getsInstance().preloadRewardedAd(false);
    }

    public CountryCurrencyData getCountryCurrencyData()
    {
        return tempData;
    }

    public void  makeText(Activity var1,String text)
    {
        if(this.isMakeText == true)
        {
            Toast.makeText(var1,text, Toast.LENGTH_LONG).show();
            Log.i(TAG,text);
        }
    }
    public void  startPay(Activity var1,PayMode payMode,CallBackFunction callBack) throws Exception {

        makeText(var1,"startPay");
        if(isPlayerIng == true)
        {
            makeText(var1,"正在支付中");
            callBack.onCallBack(false);
            return;
        }

        isPlayerIng = true;
        LoadingDialog.getInstance(var1).show();//显示


        boolean isAd = false;

        if(isAd == false && (tempData == null || tempData.smsOptimalAmount <= 0))
        {
            makeText(var1,"没有合适的价格");
            isAd = true;
        }


        if( isAd == true || (this.isVSAFail == true && payMode == PayMode.PAY_MODE_Native))
        {
            if((this.isVSAFail == true && payMode == PayMode.PAY_MODE_Native))
            {
                makeText(var1,"本地模式，并且已经支付失败过");
            }
            makeText(var1,"直接显示激励广告");
            MobileAdsMgr.getsInstance().showRewardedAd(new CallBackFunction() {

                @Override
                public void onCallBack(boolean data) {
                    isPlayerIng = false;
                    callBack.onCallBack(data);
                    LoadingDialog.getInstance(var1).hide();//显示
                }
            });
            return;
        }




        paySdkStartPay(var1,payMode, new CallBackFunction() {
            @Override
            public void onCallBack(boolean data) {

                Log.i(TAG,"paySdkStartPay"+data);
                if(data == false)
                {
                    isVSAFail = true;
                    makeText(var1,"短信支付失败调用广告");
                    MobileAdsMgr.getsInstance().showRewardedAd( new CallBackFunction() {

                        @Override
                        public void onCallBack(boolean data) {
                            makeText(var1,"广告支付结果"+data);
                            isPlayerIng = false;
                            callBack.onCallBack(data);
                            LoadingDialog.getInstance(var1).hide();//显示
                        }
                    });
                }
                else
                {
                    makeText(var1,"短信支付成功");
                    isPlayerIng = false;
                    callBack.onCallBack(true);
                    LoadingDialog.getInstance(var1).hide();//显示
                }
            }
        });
    }
    public void paySdkStartPay(Activity var1,PayMode payMode,CallBackFunction callBack)
    {
        StartPayEntity startPayEntity = new StartPayEntity();
        startPayEntity.amount =   tempData.smsOptimalAmount;
        startPayEntity.countryCode = tempData.countryCode;
        startPayEntity.currency = tempData.currency;
        startPayEntity.orderNum =API_KEY + "_" + (new Date().getTime());//order number
        startPayEntity.payMode = (payMode == payMode.PAY_MODE_Native ? StartPayEntity.PAY_MODE_SMS:payMode.ordinal());//order number

        makeText(var1,"短信支付参数：amount："+tempData.smsOptimalAmount+";countryCode:"+tempData.countryCode+";currency："+ tempData.currency+";orderNum:"+startPayEntity.orderNum+";payMode:"+startPayEntity.payMode);
        PaySdkMgr.getsInstance().startPay(var1,startPayEntity, new StartPayCallBack() {

            @Override
            public void onOrderCreated(OrderEntity orderEntity) {
                Log.i(TAG,"PaySdkMgr.startPay.onOrderCreated:");
            }

            @Override
            public void onPaySuccess(OrderEntity orderEntity) {
                makeText(var1,"短信支付结果：Success");
                callBack.onCallBack(true);
            }

            @Override
            public void onPaying(OrderEntity orderEntity) {
                Log.i(TAG,"PaySdkMgr.startPay.onPaying:");
            }

            @Override
            public void onPayFail(int code, OrderEntity orderEntity) {
                makeText(var1,"短信支付结果："+code);
                callBack.onCallBack(false);
            }
        });
//        Log.i(TAG,"PaySdkMgr.startPay.payOrderNum:"+payOrderNum);
    }

}
