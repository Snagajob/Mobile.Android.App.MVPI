# Mobile.Android.App.MVPI
Reference app for the Command/Scan pattern on Android

# Service Design
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

This allows consumers of the service to respond appropriately to all possible results as well as catch all unkown errors
in the stream by emitting an instance of `Error`. Example usage might look like:
```kotlin
loginService.loginResults()
    .onErrorReturn { LoginServiceResult.Error() }
    .subscribe { //do all the things based on type }
```

## Shared Services
We have decided on implementing services in a way that allows all consumers of the service to respond to any new results they are
subscribed to whether they made the request or not. This may not be appropriate for all service types.

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
