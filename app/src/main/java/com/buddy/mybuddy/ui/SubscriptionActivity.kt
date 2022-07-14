package com.buddy.mybuddy.ui

import android.app.Dialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.android.billingclient.api.*
import com.buddy.mybuddy.R
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList

class SubscriptionActivity : AppCompatActivity(), PurchasesUpdatedListener {
    private var error_status: String? = null
    var purchaseState = 0
    val TAG: String = "chk"
    val skuList = ArrayList<String>()
    lateinit var btnMonthly: Button
    lateinit var btnYearly: Button
    lateinit var txtMonthly: TextView
    lateinit var txtYearly: TextView
    private val email_address: String? = null
    private var subscription_status: String? = null
    var productDetailsList: List<ProductDetails>? = null
    private lateinit var billingClient: BillingClient
    var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener? = null
    lateinit var progressDialog: ProgressDialog
    var purchasesResult: Purchase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        progressDialog = ProgressDialog(applicationContext)
        btnMonthly = findViewById(R.id.btnMonthly)
        btnYearly = findViewById(R.id.btnMonthly)
        txtMonthly = findViewById(R.id.txtMonthly)
        txtYearly = findViewById(R.id.txtYearly)
        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this)
                .build()
        // queryAvaliableProducts()
        initBillingClient()

        startBillingConnection()
        val subscribe_btn_month = findViewById(R.id.btnMonthly) as Button
        val subscribe_btn_year = findViewById(R.id.btnYearly) as Button
        subscribe_btn_month.setOnClickListener { //                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(1, true)

        }
        subscribe_btn_year.setOnClickListener { //                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(0, true) //billingFlow("genius_drive_plus_yearly_subscription");

        }
    }

    private fun startBillingConnection() {
        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this)
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.i("chk bill res code", billingResult.toString())
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    Log.v(TAG, "Billing client successfully setup")

                    val list: ImmutableList<QueryProductDetailsParams.Product> = ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("monthly_sub")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("yearly_sub")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )

                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(list)
                        .build()
                    billingClient.queryProductDetailsAsync(
                        params,
                        { billingResult, pDetailsList ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                productDetailsList = pDetailsList
                                txtMonthly.text =
                                    pDetailsList.get(0).name + " " + pDetailsList.get(0).subscriptionOfferDetails!!.get(
                                        0
                                    ).pricingPhases.pricingPhaseList.get(1).formattedPrice
                                txtYearly.text =
                                    pDetailsList.get(1).name + " " + pDetailsList.get(1).subscriptionOfferDetails!!.get(
                                        0
                                    ).pricingPhases.pricingPhaseList.get(1).formattedPrice

                                billingClient.queryPurchasesAsync(
                                    QueryPurchasesParams.newBuilder()
                                        .setProductType(BillingClient.ProductType.SUBS).build(),
                                    { billingResult1: BillingResult?, purchases: List<Purchase> ->
                                        if (purchases.size > 0) {
                                            Log.v(
                                                TAG,
                                                "+++++ subscription details " + purchases[0].skus
                                            )
                                            if (!purchases[0].purchaseToken
                                                    .isEmpty() && purchases[0].purchaseState == 1
                                            ) {
                                                Log.v(
                                                    TAG,
                                                    "subscription is there"
                                                )
                                            } else {
                                                Log.v(
                                                    TAG,
                                                    "condition failed " + purchases[0].purchaseToken
                                                        .isEmpty() + " ++++ " + purchases[0].purchaseState
                                                )
                                            }
                                        } else {

                                            Log.v(
                                                TAG,
                                                "No subscription is there"
                                            )
                                            /* val status: Int =
                                                 MyApplication.getInstance().getAppDatabase().subscriptionDao()
                                                     .isDataExist(email_address)*/
                                            subscription_status = SharedPreferenceController.instance
                                                ?.getValue(applicationContext, "sub_status")
                                            Log.v(
                                                TAG,
                                                "message +++++ subscription_status $subscription_status email_address $email_address"
                                            )
                                            if (subscription_status.equals(
                                                    email_address,
                                                    ignoreCase = true
                                                )
                                            ) {
                                                Log.i("chk sub->", "renew sub")
                                                Log.v(
                                                    TAG,
                                                    " +++ +used sub show sub"
                                                )
                                                subscribeDialog()

                                            } else {
                                                Log.i("chk sub->", "welcome sub new user")
                                                Log.v(
                                                    TAG,
                                                    " ++++ No valid subscription found"
                                                )
                                                welcomeMessage()
                                            }
                                        }
                                    }
                                )
                            } else {
                                Log.v(
                                    TAG,
                                    "SkuDetailsResponse message not found"
                                )
                            }
                        }

                    )
                }
            }

            private fun welcomeMessage() {
                Log.i("chk wel", "hii")
                //newUserSubscribeDialog();
                val msg = resources.getString(R.string.Welcome_message)
                val dialog = Dialog(applicationContext)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.alert_box_trail)
                val text = dialog.findViewById(R.id.header) as TextView
                text.text = msg
                val dialogButton = dialog.findViewById(R.id.free_trail_btn) as Button
                dialogButton.setOnClickListener {
                    newUserSubscribeDialog()
                    dialog.dismiss()
                }
                dialog.show()
            }
            override fun onBillingServiceDisconnected() {
                Log.e(TAG, "Billing client disconnected")
            }
        })
    }



    fun initBillingClient() {
        Log.i("chk pos", "i m in initBillingClient")
        acknowledgePurchaseResponseListener =
            AcknowledgePurchaseResponseListener { billingResult ->
                Log.i(
                    "chk init clint code",
                    billingResult.toString() + "..." + BillingClient.BillingResponseCode.OK
                )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.v(TAG, "Subscribed")
                    billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build(),
                        { billingResult, purchases ->
                            purchasesResult = purchases[0]
                            Log.v(
                                TAG,
                                "purchase getBillingResult: " + billingResult.responseCode + " purchase getPurchasesList: " + purchases[0] + " purchase getResponseCode: " + purchases[0].products
                            )
                            val token: String =
                                purchasesResult!!.purchaseToken //   .getPurchasesList().get(0).getPurchaseToken();


                            purchasesResult!!.getSkus() //   .getPurchasesList().get(0).getSku();
                            Log.i(
                                "CHKSUB purchaselist",
                                purchasesResult!!.getProducts().get(0).toString()
                            ) //   .getPurchasesList().get(0).toString());
                            stopBar()
                            success()
                        })
                } else {
                    Log.i("chk error:", billingResult.toString())
                    errorMessage()
                }
            }
    }


    private fun errorMessage() {
        val msg = "some prob"//resources.getString(R.string.error_message)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_box_deactivate)
        val textheader = dialog.findViewById(R.id.header) as TextView
        textheader.text = "Alert Message"
        val text = dialog.findViewById(R.id.message) as TextView
        text.text = "plz try again"
        val yes_btn = dialog.findViewById(R.id.btn_yes) as Button
        val no_btn = dialog.findViewById(R.id.btn_No) as Button
        yes_btn.setOnClickListener {
            startBillingConnection()
            // newUserSubscribeDialog();
            //subscribeDialog();
            //billingFlow();
            dialog.dismiss()
        }
        no_btn.setOnClickListener {
            // finish()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        Log.i("chk resume", "i m in resume")
        try {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                PurchasesResponseListener { billingResult: BillingResult, purchases: List<Purchase> ->
                    if (purchases.size > 0) {
                        purchaseState = purchasesResult!!.getPurchaseState()
                        purchasesResult = purchases[0]
                        Log.v(
                            TAG,
                            "purchase getBillingResult: " + billingResult.responseCode + " purchase getPurchasesList: " + purchases[0] + " purchase getResponseCode: " + purchases[0].products
                        )
                    } else {
                        Log.v("chk purchase res", "No item found")
                    }
                })
          } catch (exception: Exception) {
            Log.v(
                TAG,
                "queryPurchase exception: $exception"
            )
        }

        val billing_status: Boolean =
            SharedPreferenceController.instance!!.getBoolean(this, "purchase_update")
        if (billing_status) {
            SharedPreferenceController.instance!!.setBooleanValue(this, "purchase_update", false)
            errorMessage()
        }
        // refreshNotification()

    }

    fun newUserSubscribeDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_newuser_subscription)
        val params: ViewGroup.LayoutParams = dialog.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = params as WindowManager.LayoutParams
        val subscribe_btn_month = dialog.findViewById(R.id.btnMonthly) as Button
        val subscribe_btn_year = dialog.findViewById(R.id.btnYearly) as Button
        subscribe_btn_month.setOnClickListener { //                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(1, true)
            dialog.dismiss()
        }
        subscribe_btn_year.setOnClickListener { //                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(0, true) //billingFlow("genius_drive_plus_yearly_subscription");
            dialog.dismiss()
        }
        dialog.show()
    }

    fun subscribeDialog() {
        Log.i("chk dialog", "i m in sub1 dialog")
        val msg = resources.getString(R.string.subscriptionPeriodExpired_message)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_box_subscription)
        val textheader = dialog.findViewById(R.id.header) as TextView
        textheader.text = "Alert Message"
        val text = dialog.findViewById(R.id.message) as TextView
        text.text = msg
        val subscribe_btn_month = dialog.findViewById(R.id.btn_Subscribe_Month) as Button
        val subscribe_btn_year = dialog.findViewById(R.id.btn_Subscribe_Year) as Button
        val deactivate_btn = dialog.findViewById(R.id.btn_Deactivate) as Button
        val cancel_btn = dialog.findViewById(R.id.btn_cancel) as Button
        subscribe_btn_month.setOnClickListener { v: View? ->
//                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(1, false)
            dialog.dismiss()
        }
        subscribe_btn_year.setOnClickListener { v: View? ->
//                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(0, false) //billingFlow("genius_drive_plus_yearly_subscription");
            dialog.dismiss()
        }
        deactivate_btn.setOnClickListener { v: View? ->
            //deactivateDialog()
            dialog.dismiss()
        }
        cancel_btn.setOnClickListener { v: View? ->
            finish()
            dialog.dismiss()
        }
        //dialog.create();
        dialog.show()
    }

    private fun billingFlow(product_index: Int, new_user_flag: Boolean) {
        Log.i(
            "chk product list:",
            productDetailsList!!.size.toString() + "*" + productDetailsList.toString()
        )
        //  var a = ProductDetails{jsonString='{"productId":"yearly_sub","type":"subs","title":"yearly subscription (mybuddy)","name":"yearly subscription","localizedIn":["en-US"],"skuDetailsToken":"AEuhp4ItZ1SE0cw_TWBCvdbIEfNmOQJXUaJqaE9QWCKbAMnO7DMgwTifghQ0PMAdXHYz","subscriptionOfferDetails":[{"offerIdToken":"AUj\/YhjxrjuLuEYTLEc8B\/jEk2Az3zZ4HMzb04ijWFFEQtupLJrKU99Fo4tjEMfBMyweDRaGg0YAZU0WsGRYD1gZUfS8gAzy86oVbfexBqDhfBcoAQNT3pYlPw==","pricingPhases":[{"priceAmountMicros":1000000000,"priceCurrencyCode":"INR","formattedPrice":"₹1,000.00","billingPeriod":"P1Y","recurrenceMode":1}],"offerTags":[]}]}', parsedJson={"productId":"yearly_sub","type":"subs","title":"yearly subscription (mybuddy)","name":"yearly subscription","localizedIn":["en-US"],"skuDetailsToken":"AEuhp4ItZ1SE0cw_TWBCvdbIEfNmOQJXUaJqaE9QWCKbAMnO7DMgwTifghQ0PMAdXHYz","subscriptionOfferDetails":[{"offerIdToken":"AUj\/YhjxrjuLuEYTLEc8B\/jEk2Az3zZ4HMzb04ijWFFEQtupLJrKU99Fo4tjEMfBMyweDRaGg0YAZU0WsGRYD1gZUfS8gAzy86oVbfexBqDhfBcoAQNT3pYlPw==","pricingPhases":[{"priceAmountMicros":1000000000,"priceCurrencyCode":"INR","formattedPrice":"₹1,000.00","billingPeriod":"P1Y","recurrenceMode":1}],"offerTags":[]}]}, productId='yearly_sub', productType='subs', title='yearly subscription (mybuddy)', productDetailsToken='AEuhp4ItZ1SE0cw_TWBCvdbIEfNmOQJXUaJqaE9QWCKbAMnO7DMgwTifghQ0PMAdXHYz', subscriptionOfferDetails=[com.android.billingclient.api.ProductDetails$SubscriptionOfferDetails@50ce91a]}]
        Log.i(
            "product index->",
            product_index.toString()
        )//+ " productDetailsList!![product_index]=>"+productDetailsList!![product_index]+" *** ")
        val offerToken =
            productDetailsList!![product_index].subscriptionOfferDetails!![0].offerToken
        val productDetailsParamsList: ImmutableList<BillingFlowParams.ProductDetailsParams>

        // Set the parameters for the offer that will be presented
// in the billing flow creating separate productDetailsParamsList variable
        Log.i(
            "chk product=>",
            productDetailsList!![product_index].subscriptionOfferDetails!![0].toString() + "....." + product_index.toString() + "...token->" + offerToken.toString()
        )
        if (new_user_flag) {
            productDetailsParamsList = ImmutableList.of(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetailsList!![product_index])
                    .setOfferToken(offerToken)
                    .build()
            )
        } else {
            productDetailsParamsList = ImmutableList.of(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetailsList!![product_index])
                    .setOfferToken(offerToken)
                    .build()
            )
        }
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()


        val billingResult: BillingResult = billingClient.launchBillingFlow(this, billingFlowParams)
        Log.v("CHKSUB billingflow", "response $billingResult")

    }

    ///////////////////////end billing////////////////////


    private fun progressBar(input: String) {

        progressDialog.setMessage(input)
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.show()
    }

    private fun stopBar() {
        progressDialog.dismiss()
    }

    private fun queryAvaliableProducts() {
        skuList.add("monthly_sub")
        skuList.add("yearly_sub")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            Log.i("chk res code ", billingResult.responseCode.toString())
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                btnMonthly.text = skuDetailsList[0].title
                txtMonthly.text =
                    skuDetailsList[0].description + "***" + skuDetailsList[1].freeTrialPeriod
                btnYearly.text = skuDetailsList[1].title
                txtYearly.text =
                    skuDetailsList[1].description + "***" + skuDetailsList[1].freeTrialPeriod
            }
            btnMonthly.setOnClickListener {
                skuDetailsList?.get(0)?.let {
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it)
                        .build()
                    billingClient?.launchBillingFlow(this, billingFlowParams)?.responseCode
                } ?: noSKUMessage()
            }
            btnYearly.setOnClickListener {
                skuDetailsList?.get(1)?.let {
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it)
                        .build()
                    billingClient?.launchBillingFlow(this, billingFlowParams)?.responseCode
                } ?: noSKUMessage()
            }
        }
    }

    fun noSKUMessage() {

    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }

    private fun success() {
        val msg = "subscription successfull"
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_box_success)
        val text = dialog.findViewById(R.id.header) as TextView
        text.text = msg
        val btn_ok = dialog.findViewById(R.id.ok_btn) as Button
        btn_ok.setOnClickListener {
            SharedPreferenceController.instance!!.setBooleanValue(this, "login_status", true)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: MutableList<Purchase>?) {
        val responseCode: Int = billingResult.getResponseCode()
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.v(TAG, "OK")
                //SUB
                if (list != null) {
                    for (purchase in list) {
                        handlePurchasedItemSub(purchase)
                        progressBar("Fetching Transaction Information")
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.v(TAG, "USER_CANCELED")
                error_status = "USER_CANCELED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.v(TAG, "ITEM_ALREADY_OWNED")
                error_status = "ITEM_ALREADY_OWNED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                Log.v(TAG, "BILLING_UNAVAILABLE")
                error_status = "BILLING_UNAVAILABLE"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.v(TAG, "DEVELOPER_ERROR")
                error_status = "DEVELOPER_ERROR"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                Log.v(TAG, "FEATURE_NOT_SUPPORTED")
                error_status = "FEATURE_NOT_SUPPORTED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Log.v(TAG, "SERVICE_DISCONNECTED")
                error_status = "SERVICE_DISCONNECTED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {
                Log.v(TAG, "SERVICE_TIMEOUT")
                error_status = "SERVICE_TIMEOUT"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.v(TAG, "ITEM_UNAVAILABLE")
                error_status = "ITEM_UNAVAILABLE"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.ERROR -> {
                Log.v(TAG, "ERROR")
                error_status = "ERROR"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Log.v(TAG, "SERVICE_UNAVAILABLE")
                error_status = "SERVICE_UNAVAILABLE"
                SharedPreferenceController.instance!!
                    .setBooleanValue(this, "purchase_update", true)
            }
            else -> {}
        }
    }

    private fun handlePurchasedItemSub(purchases: Purchase) {
        if (purchases.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchases.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchases.purchaseToken)
                    .build()
                acknowledgePurchaseResponseListener?.let {
                    billingClient.acknowledgePurchase(
                        acknowledgePurchaseParams,
                        it
                    )
                }
                Log.v(TAG, "handleItemAlreadyPurchasedSub done")
            } else {
                Log.v(
                    TAG,
                    "check the token" + purchases.purchaseToken
                )
            }
        } else if (purchases.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.v(TAG, "PENDING")
        } else if (purchases.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Log.v(TAG, "UNSPECIFIED_STATE")
        }
    }
}