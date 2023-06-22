package com.game.PaySDKManager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;

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
import java.util.List;

public class PaySdkMgr {


    private String TAG = "PaySdkMgr";
    private static  PaySdkMgr _instance = null;
    public static PaySdkMgr getsInstance() {
        if(PaySdkMgr._instance == null)
        {
            PaySdkMgr._instance = new PaySdkMgr();
        }
        return PaySdkMgr._instance;
    }
    private PaySdkMgr() {
        Log.e(TAG,"PaySdkMgr");
    }

    //国家代码:IQ;  货币:IQD;   mcc:418;
    public void setTestMode()
    {
        SMSTestUtil.setTestMode(true);//设置true时表示开启测试模式，并且是双卡模式，默认关闭
        SMSTestUtil.setTestMCCMNC("41820");//这里设置的是第一张卡的mccmnc
        SMSTestUtil.setTestMCCMNC2("405880");//这里是设置第二张卡的mccmnc
    }

    /***********************************支付sdk***************************************************/
    public void setStrict(boolean var1)
    {
        Log.e(TAG,"setStrict");
        //设置严格模式，默认true，设置true后，经过多次查询后，支付中也会返回失败
        PaySDKManager.getsInstance().setStrict(var1);
    }


    public void initAriesPay(Context var1, String var2, String var3, String var4, int var5, InitResultCallBack var6) {

        PaySDKManager.getsInstance().initAriesPay(var1, var2, var3, var4,var5,new InitResultCallBack() {
            @Override public void onSuccess(List<SupportPayInfoEntity> list, boolean supportOnlinePay, CountryCurrencyData countryCurrencyData) {

                var6.onSuccess(list,supportOnlinePay,countryCurrencyData);
            }
            @Override public void onFail(int code) {
                Log.i(TAG,"onFail:"+code);
                var6.onFail(code);
            }
        });
    }

    //美元转本位币
    public void convertUsdToLocal(Context var1, List<ConvertPriceInfo> var2,CurrencyConvertCallBack var3)
    {
        PaySDKManager.getsInstance().convertUsdToLocal(var1,var2,var3);

    }
//    //发起在线支付或者短代支付
//    public  void startSMSPay(Activity var1, StartPayEntity var2, StartPayCallBack var3)  {
//
//        StartPayEntity startPayEntity = new StartPayEntity();
//        startPayEntity.amount =  var2.amount;
//        startPayEntity.countryCode = var2.countryCode;
//        startPayEntity.currency = var2.currency;
//        startPayEntity.orderNum = var2.orderNum;//order number
//        startPayEntity.payMode = StartPayEntity.PAY_MODE_SMS;//支付方式
//
//        this.startPay(var1,startPayEntity,var3);
//    }
//    public  void startONLINEPay(Activity var1, StartPayEntity var2, StartPayCallBack var3)  {
//
//        StartPayEntity startPayEntity = new StartPayEntity();
//        startPayEntity.amount =  var2.amount;
//        startPayEntity.countryCode = var2.countryCode;
//        startPayEntity.currency = var2.currency;
//        startPayEntity.orderNum = var2.orderNum;//order number
//        startPayEntity.payMode = StartPayEntity.PAY_MODE_ONLINE;//支付方式
//
//        this.startPay(var1,startPayEntity,var3);
//
//    }
    public  void startPay(Activity var1, StartPayEntity var2, StartPayCallBack var3)  {

        StartPayEntity startPayEntity = new StartPayEntity();
        startPayEntity.amount =  var2.amount;
        startPayEntity.countryCode = var2.countryCode;
        startPayEntity.currency = var2.currency;
        startPayEntity.netPaySp = "card";//指定在线支付的渠道
        startPayEntity.orderNum = var2.orderNum;//order number
        startPayEntity.payMode = var2.payMode;//支付方式
        startPayEntity.matchDown = true;//支持向下取金额的功能，当支付方式支持的金额小 于发起支付的金额，开启这个功能可以匹配该支付方式。默认为false。
        startPayEntity.adjustMode = BigDecimal.ROUND_UP;//设置adjustMode会根据设置 的mode，结合对应的币种来处理传入的金额各个位数取整，以防传入
        startPayEntity.serviceConfigPriority = true;

        try {
            PaySDKManager.getsInstance().startPay(var1, startPayEntity, var3);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startPay: ",e );
            var3.onPayFail(1,null);
        }

    }
}
