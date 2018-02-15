package com.snagajob.mvpireference.services.login

data class LoginResponse(val member: MemberDetail,
                         val tokens: List<CustomerToken>)

data class CustomerToken(val token: String,
                         val customerId: String,
                         val customerName: String,
                         val expirationDateTime: String)

data class MemberDetail(val id: String,
                        val users: List<UserDetail>)

data class UserDetail(val id: String,
                      val customerId: String,
                      val displayName: String,
                      val avatarUrl: String?,
                      val modules: List<String>)