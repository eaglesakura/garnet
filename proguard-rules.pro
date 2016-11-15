-keepattributes Signature
-keepclassmembers class * {
    @com.eaglesakura.android.garnet.Inject *;
    @com.eaglesakura.android.garnet.Depend *;
    @com.eaglesakura.android.garnet.Provide *;
    @com.eaglesakura.android.garnet.Initializer *;
}
-keepclasseswithmembers class * implements com.eaglesakura.android.garnet.Provider
