# Mobile.Android.App.MVPI
Reference app for the Command/Scan pattern on Android

Note: Examples shown in README are pseudocode. Check actual implementations for exact code.

# Service Design
## Retrofit's `Result` Type
Wrapping your Retrofit calls in the Result type allows all errors associated with that network call to be caught in a clean way. We have tested this type against various other options provided by RxJava and Retrofit and have determined that this type catches additional errors that others do not.


## Errors
Services should define all possible return types for a given route and ideally include a catch all error type. Ex:
```kotlin
sealed class LoginServiceResult {
    data class Success(token: String): LoginServiceResult()
    class InvalidPassword: LoginServiceResult()
    class InvalidUser: LoginServiceResult()
    class Error: LoginServiceResult()
}
```

This allows the service to respond appropriately to all possible results as well as catch all unkown errors
in the stream by emitting an instance of `Error`. Example usage might be (loginService being your Retrofit object):
```kotlin
loginService.attemptLogin()
    .map { 
        // Check response object for errors and emit the appropriate type 
        if (it.isError)
            LoginServiceResult.Error()
        else
            // Can check HTTP codes here and respons appropriately
            if (code == 401)
                LoginServiceResult.InvalidPassword()
            else
                LoginServiceResult.Success()
    }
    .subscribe({loginResults.onNext(it)}, {loginResults.onNext(LoginServiceResult.Error())})
```

When creating the Retrofit object, we are using Singles to represent requests. This explicitly shows that we are not creating a never ending stream when making these requests. This also means we don't have to be concerned with any other stream breaking operations. Ex:
```
interface Login {
        @GET("/v1/Authentication/token")
        fun getToken(@Header("Authorization") authorizationToken: String): Single<Result<LoginResponse>>
}
```

## Shared Services
We have decided on implementing services in a way that allows all consumers of the service to respond to any new results they are subscribed to whether they made the request or not. This may not be appropriate for all service types.

This requires splitting the request from the response. We could decide not to split them, but in that case it's impossible to
subscribe to results of a service without firing off a request. Without splitting, a request would look like:
```
fun attemptLogin(username: String, password: String): Observable<LoginServiceResult>
```

A service with the same responsibilities (after splitting) would look like:
```
fun attemptToLogin(username: String, password: String)
fun loginResults(): Observable<LoginServiceResult>
```

By structuring services this way, anything that has subscribed to those results will be pushed a new result even if something
else caused the request.
