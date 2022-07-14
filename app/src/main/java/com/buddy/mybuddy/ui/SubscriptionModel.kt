package com.buddy.mybuddy.ui

class SubscriptionModel(
    var userId: String?,
    var startDate: String?,//    public Boolean getStatus(boolean status) {
    //        return status;
    //    }
    var endDate: String?,
    var platform: String?,
    var token: String?,
    var freeTrial: Int
) {


    override fun toString(): String {
        return "userId=$userId,sub_start_date=$startDate,end_date=$endDate,platform=$platform,token=$token,freeTrial=$freeTrial"
    }
}