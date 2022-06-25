package com.buddy.mybuddy


import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), PurchasesUpdatedListener {
    private var error_status: String? = null
    var purchaseState = 0
    val TAG:String ="chk"
    // TODO: Rename and change types of parameters
    val skuList = ArrayList<String>()
    private var param1: String? = null
    private var param2: String? = null
     lateinit var btnMonthly: Button
    lateinit var btnYearly: Button
    lateinit var txtMonthly: TextView
    lateinit var txtYearly: TextView
    private val email_address: String? =null
    private  var subscription_status:String? = null
    private var mProductDetailsMap: Map<String, SkuDetails> = HashMap()

    //Purchase.PurchaseState purchaseState ;
    var productDetailsList: List<ProductDetails>? = null
    private lateinit var billingClient: BillingClient
    var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener? = null
    lateinit var progressDialog:ProgressDialog

    var purchasesResult: Purchase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        progressDialog = ProgressDialog(requireActivity().applicationContext)
        billingClient =
            BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener(this)
                .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_home, container, false)
        btnMonthly=v.findViewById<Button>(R.id.btnMonthly)
        btnYearly=v.findViewById<Button>(R.id.btnMonthly)
        txtMonthly=v.findViewById<TextView>(R.id.txtMonthly)
        txtYearly=v.findViewById<TextView>(R.id.txtYearly)
        initBillingClient()
        requestSubscriptionDetails()
        startBillingConnection()
        val subscribe_btn_month = v.findViewById<View>(R.id.btnMonthly) as Button
        val subscribe_btn_year = v.findViewById<View>(R.id.btnYearly) as Button
        subscribe_btn_month.setOnClickListener { //                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(1, true)

        }
        subscribe_btn_year.setOnClickListener { //                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "un_subscribe", false);
            billingFlow(0, true) //billingFlow("genius_drive_plus_yearly_subscription");

        }
        return v
    }

    private fun startBillingConnection() {
        billingClient =
            BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener(this)
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.i("chk bill res code", billingResult.toString())
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    Log.v(TAG, "Billing client successfully setup")
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
                                mProductDetailsMap = requestSubscriptionDetails() as Map<String, SkuDetails>
                                Log.v(
                                    TAG,
                                    "No subscription is there"
                                )
                               /* val status: Int =
                                    MyApplication.getInstance().getAppDatabase().subscriptionDao()
                                        .isDataExist(email_address)*/
                                subscription_status = SharedPreferenceController.instance
                                    ?.getValue(requireActivity(), "sub_status")
                                Log.v(
                                    TAG,
                                    "message +++++ subscription_status $subscription_status email_address $email_address"
                                )
                              /*  val isEmailExist: Boolean =
                                    MyApplication.getInstance().getAppDatabase().subscriptionDao()
                                        .isEmailExist(email_address)
                                if (isEmailExist) Log.i(
                                    "chk exist->",
                                    "yes" + subscription_status.equals(
                                        email_address,
                                        ignoreCase = true
                                    )
                                ) else Log.i(
                                    "chk exist->",
                                    "no" + subscription_status.equals(
                                        email_address,
                                        ignoreCase = true
                                    ) + email_address
                                )*/
                               // if (isEmailExist || subscription_status.equals(
                                if ( subscription_status.equals(
                                        email_address,
                                        ignoreCase = true
                                    )
                                ) {
                                    Log.i("chk sub->", "renew sub")
                                    Log.v(
                                        TAG,
                                        " +++ +used sub show sub"
                                    )
                                    Handler(Looper.getMainLooper())
                                        .post { // welcomeMessage();
                                            subscribeDialog()
                                        }
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


                    ////first demo////
                    mProductDetailsMap = requestSubscriptionDetails() as Map<String, SkuDetails>
                    for (entry in mProductDetailsMap.keys) {
                        Log.i("chk", "$entry ")

                        // do stuff
                    }
                }
            }
            private fun welcomeMessage() {
                Log.i("chk wel", "hii")
                //newUserSubscribeDialog();
                val msg = resources.getString(R.string.Welcome_message)
                val dialog = Dialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.alert_box_trail)
                val text = dialog.findViewById<View>(R.id.header) as TextView
                text.text = msg
                val dialogButton = dialog.findViewById<View>(R.id.free_trail_btn) as Button
                dialogButton.setOnClickListener { //                initiateGoogleSingOut();
                    //  subscribeDialog();
                    //billingFlow("subscription_genius_drive_plus");
                    newUserSubscribeDialog()
                    dialog.dismiss()
                }
                dialog.show()
            }

            /*private Purchase queryPurchase() {
         Purchase p = null;
        mBillingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                new PurchasesResponseListener() {
                    public void onQueryPurchasesResponse(
                            BillingResult billingResult,
                            List<Purchase> purchases) {
                        purchasesResult =purchases.get(0);
                        p = purchases.get(0);
                        Log.v(TAG, "purchase getBillingResult: " + billingResult.getResponseCode() + " purchase getPurchasesList: " + purchases.get(0) + " purchase getResponseCode: " + purchases.get(0).getProducts());
                    }

                }
        );
       return p;
    }*/
            /* Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
        purchasesResult.getPurchasesList().get(0).getPurchaseState();
        Log.v(TAG, "purchase getBillingResult: " + purchasesResult.getBillingResult() + " purchase getPurchasesList: " + purchasesResult.getPurchasesList() + " purchase getResponseCode: " + purchasesResult.getResponseCode());
        return purchasesResult;
    }*/
            private fun requestSubscriptionDetails(): Map<*, *>? {
                val list: ImmutableList<Product> = ImmutableList.of(
                    Product.newBuilder()
                        .setProductId("monthly_sub")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    Product.newBuilder()
                        .setProductId("yearly_sub")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
                val productDetail_map: Map<String, SkuDetails> = HashMap()
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(list)
                    .build()
                billingClient.queryProductDetailsAsync(
                    params,
                    ProductDetailsResponseListener { billingResult, pDetailsList ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            productDetailsList = pDetailsList
                            txtMonthly.text=pDetailsList.get(0).name+ " "+pDetailsList.get(0).subscriptionOfferDetails!!.get(0).pricingPhases.pricingPhaseList.get(1).formattedPrice
                            txtYearly.text=pDetailsList.get(1).name+ " "+pDetailsList.get(1).subscriptionOfferDetails!!.get(0).pricingPhases.pricingPhaseList.get(1).formattedPrice
                            // productDetail_map.put(list.get(0).getSku(), list.get(0));
                            //productDetail_map.put(list.get(1).getSku(), list.get(1));
                            Log.v(
                                TAG,
                                "SkuDetailsResponse message" + list[0].toString() + "  " + list[0].toString()
                            )
                        } else {
                            Log.v(
                                TAG,
                                "SkuDetailsResponse message not found"
                            )
                        }
                    }
                )
                return productDetail_map
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
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
                        PurchasesResponseListener { billingResult, purchases ->
                            purchasesResult = purchases[0]
                            Log.v(
                                TAG,
                                "purchase getBillingResult: " + billingResult.responseCode + " purchase getPurchasesList: " + purchases[0] + " purchase getResponseCode: " + purchases[0].products
                            )
                            val token: String = purchasesResult!!.purchaseToken //   .getPurchasesList().get(0).getPurchaseToken();
                            val date = Date()
                        /*    start_date = Util.setStartDate(date)
                            end_date = Util.calculateMonthPeriod(date)
                            subscription = Subscription(
                                email_address,
                                start_date,
                                end_date,
                                platform,
                                token,
                                1
                            )
                            MyApplication.getInstance().getAppDatabase().subscriptionDao()
                                .insert(subscription)
                            Log.i("CHKSUB subscription", subscription.toString())
                            //                    SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this, "subscribe", true);
                            sendToServer()*/
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
    private fun requestSubscriptionDetails(): Map<*, *>? {
        val list: ImmutableList<Product> = ImmutableList.of(
            Product.newBuilder()
                .setProductId("sub_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            Product.newBuilder()
                .setProductId("sub_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val productDetail_map: Map<String, SkuDetails> = HashMap()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(list)
            .build()
        billingClient.queryProductDetailsAsync(
            params,
            ProductDetailsResponseListener { billingResult, pDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetailsList = pDetailsList
                    // productDetail_map.put(list.get(0).getSku(), list.get(0));
                    //productDetail_map.put(list.get(1).getSku(), list.get(1));
                    Log.v(
                        TAG,
                        "SkuDetailsResponse message" + list.get(0).toString()
                            .toString() + "  " + list.get(0).toString()
                    )
                } else {
                    Log.v(
                        TAG,
                        "SkuDetailsResponse message not found"
                    )
                }
            }
        )
        return productDetail_map
    }

    private fun errorMessage() {
        val msg = "some prob"//resources.getString(R.string.error_message)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_box_deactivate)
        val textheader = dialog.findViewById<View>(R.id.header) as TextView
        textheader.text = "Alert Message"
        val text = dialog.findViewById<View>(R.id.message) as TextView
        text.text= "plz try again"
        val yes_btn = dialog.findViewById<View>(R.id.btn_yes) as Button
        val no_btn = dialog.findViewById<View>(R.id.btn_No) as Button
        yes_btn.setOnClickListener {
            startBillingConnection()
            // newUserSubscribeDialog();
            //subscribeDialog();
            //billingFlow();
            dialog.dismiss()
        }
        no_btn.setOnClickListener {
           // requireActivity().finish()
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
            // purchasesResult = purchaselist;
//            purchaseState = purchasesResult.getPurchasesList().get(0).getPurchaseState();
//            SharedPreferenceController.getInstance().setIntValue(this,"purchaseState",purchaseState);
        } catch (exception: Exception) {
            Log.v(
                TAG,
                "queryPurchase exception: $exception"
            )
        }
        //        boolean un_subscribe = SharedPreferenceController.getInstance().getBoolean(this, "un_subscribe");
//        if (un_subscribe) {
//            SharedPreferenceController.getInstance().setBooleanValue(this, "un_subscribe", false);
//            String days = RemaningDaysOfSubscription();
//            int daysvalue = Integer.parseInt(days);
//            Log.v(TAG,"daysvalue message " + daysvalue);
//            if (daysvalue >= 7){
//                Log.v(TAG,"skip message");
//            }else {
//                Log.v(TAG,"show message");
//                if (daysvalue == 0){ //days.equals("0")
//                    subscribeDialog();
//                }else {
//                    subscribeRemainingMessage(days);
//                }
//            }
//        }
        val billing_status: Boolean =
            SharedPreferenceController.instance!!.getBoolean(requireContext(), "purchase_update")
        if (billing_status) {
            SharedPreferenceController.instance!!.setBooleanValue(requireContext(), "purchase_update", false)
            errorMessage()
        }
       // refreshNotification()

    }

    fun newUserSubscribeDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_newuser_subscription)
        val params: ViewGroup.LayoutParams = dialog.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = params as WindowManager.LayoutParams
        val subscribe_btn_month = dialog.findViewById<View>(R.id.btnMonthly) as Button
        val subscribe_btn_year = dialog.findViewById<View>(R.id.btnYearly) as Button
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
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_box_subscription)
        val textheader = dialog.findViewById<View>(R.id.header) as TextView
        textheader.text = "Alert Message"
        val text = dialog.findViewById<View>(R.id.message) as TextView
        text.text = msg
        val subscribe_btn_month = dialog.findViewById<View>(R.id.btn_Subscribe_Month) as Button
        val subscribe_btn_year = dialog.findViewById<View>(R.id.btn_Subscribe_Year) as Button
        val deactivate_btn = dialog.findViewById<View>(R.id.btn_Deactivate) as Button
        val cancel_btn = dialog.findViewById<View>(R.id.btn_cancel) as Button
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
            requireActivity().finish()
            dialog.dismiss()
        }
        //dialog.create();
        dialog.show()
    }
    private fun billingFlow(product_index: Int, new_user_flag: Boolean) {
        val offerToken =
            productDetailsList!![product_index].subscriptionOfferDetails!![0].offerToken
        val productDetailsParamsList: ImmutableList<ProductDetailsParams>

        // Set the parameters for the offer that will be presented
// in the billing flow creating separate productDetailsParamsList variable
        Log.i("chk product=>", productDetailsList!![product_index].subscriptionOfferDetails!![0].toString()+"....."+product_index.toString()+"...token->"+offerToken.toString())
        if (new_user_flag) {
            productDetailsParamsList = ImmutableList.of(
                ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetailsList!![product_index])
                    .setOfferToken(offerToken)
                    .build()
            )
        } else {
            productDetailsParamsList = ImmutableList.of(
                ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetailsList!![product_index])
                    .setOfferToken(offerToken)
                    .build()
            )
        }
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()


        val billingResult: BillingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
        Log.v("CHKSUB billingflow", "response $billingResult")

    }

  /*  private fun insertModelData(subscription: Subscription) {
        subscriptionModel.setUserId(subscription.getUserId())
        subscriptionModel.setStartDate(subscription.getStartDate())
        subscriptionModel.setEndDate(subscription.getEndDate())
        subscriptionModel.setPlatform(subscription.getPlatform())
        subscriptionModel.setToken(subscription.getToken())
        subscriptionModel.setFreeTrial(subscription.getFreeTrial())
        SharedPreferenceController.getInstance().storeSubscriptionModel(this, subscriptionModel)
        Log.v(TAG, subscriptionModel.toString())
    }*/

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
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                btnMonthly.text=skuDetailsList[0].title
                txtMonthly.text=skuDetailsList[0].description
                btnYearly.text=skuDetailsList[1].title
                txtYearly.text=skuDetailsList[1].description
            }
            btnMonthly.setOnClickListener {
                skuDetailsList?.get(0)?.let {
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it)
                        .build()
                    billingClient?.launchBillingFlow(requireActivity(), billingFlowParams)?.responseCode
                }?:noSKUMessage()
            }
            btnYearly.setOnClickListener {
                skuDetailsList?.get(1)?.let {
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it)
                        .build()
                    billingClient?.launchBillingFlow(requireActivity(), billingFlowParams)?.responseCode
                }?:noSKUMessage()
            }
        }
    }
    fun noSKUMessage()
    {
        
    }

    private fun success() {
        val msg = "subscription successfull"
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_box_success)
        val text = dialog.findViewById<View>(R.id.header) as TextView
        text.text = msg
        val btn_ok = dialog.findViewById<View>(R.id.ok_btn) as Button
        btn_ok.setOnClickListener {
            SharedPreferenceController.instance!!.setBooleanValue(requireActivity(), "login_status", true)
            dialog.dismiss()
        }
        dialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.v(TAG, "ITEM_ALREADY_OWNED")
                error_status = "ITEM_ALREADY_OWNED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                Log.v(TAG, "BILLING_UNAVAILABLE")
                error_status = "BILLING_UNAVAILABLE"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.v(TAG, "DEVELOPER_ERROR")
                error_status = "DEVELOPER_ERROR"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                Log.v(TAG, "FEATURE_NOT_SUPPORTED")
                error_status = "FEATURE_NOT_SUPPORTED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Log.v(TAG, "SERVICE_DISCONNECTED")
                error_status = "SERVICE_DISCONNECTED"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {
                Log.v(TAG, "SERVICE_TIMEOUT")
                error_status = "SERVICE_TIMEOUT"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.v(TAG, "ITEM_UNAVAILABLE")
                error_status = "ITEM_UNAVAILABLE"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.ERROR -> {
                Log.v(TAG, "ERROR")
                error_status = "ERROR"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Log.v(TAG, "SERVICE_UNAVAILABLE")
                error_status = "SERVICE_UNAVAILABLE"
                SharedPreferenceController.instance!!
                    .setBooleanValue(requireContext(), "purchase_update", true)
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