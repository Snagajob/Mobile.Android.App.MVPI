package com.snagajob.mvpireference.services.login

data class LoginResponse(val memberId: String, val tokens: List<CustomerToken>)

data class CustomerToken(val securityToken: String, val customerId: String,
                         val customerName: String, val expirationDateTime: String)