package com.snagajob.mvpireference

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(compositeDisposable: CompositeDisposable)
{
    compositeDisposable.add(this)
}