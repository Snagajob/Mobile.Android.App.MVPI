package com.snagajob.mvpireference.services.login

data class LoginResponse(val memberId: String, val tokens: List<CustomerToken>)

data class CustomerToken(val token: String, val customerId: String,
                         val customerName: String, val expirationDateTime: String)