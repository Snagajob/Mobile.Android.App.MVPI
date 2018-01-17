package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Result

sealed class RemoteConfigResult : Result() {
    class Success: RemoteConfigResult()
    class Failure: RemoteConfigResult()
}
