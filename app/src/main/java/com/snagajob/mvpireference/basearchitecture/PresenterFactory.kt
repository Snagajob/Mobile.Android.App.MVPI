package com.snagajob.mvpireference.basearchitecture

abstract class PresenterFactory<out P> {
    abstract fun create(): P
}