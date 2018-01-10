package com.snagajob.mvpireference.retrofit

sealed class RetrofitException: RuntimeException() {

    class UnauthorizedException: RetrofitException()
    class NetworkException: RetrofitException()
    class NotFoundException: RetrofitException()
    class ValidationException: RetrofitException()
    class UnknownRetrofitException: RetrofitException()

}