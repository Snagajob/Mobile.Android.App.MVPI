package com.snagajob.mvpireference

import io.reactivex.Observable

/**
 * Used to merge an infinite number of observables into one.
 * Observable.merge only allows up to four streams.
 * Hopefully this works
 */
fun <T> merge(vararg sources: Observable<T>): Observable<T> {
    var result: Observable<T> = Observable.never()
    sources.forEach { result = Observable.merge(result, it) }
    return result
}