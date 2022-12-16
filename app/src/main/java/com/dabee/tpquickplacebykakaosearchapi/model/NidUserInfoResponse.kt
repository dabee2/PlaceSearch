package com.dabee.tpquickplacebykakaosearchapi.model

data class NidUserInfoResponse(var lesultcode:String,var message:String,var response:NidUser)

data class NidUser(var id:String, var email:String)
