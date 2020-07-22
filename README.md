[![Latest release](https://img.shields.io/bintray/v/prateem/maven/shimmertextview?label=latest&style=flat-square)](#)

# ShimmerTextView
Android library that exposes a custom TextView that can toggle a shimmering silhouette. Written in Kotlin.

## Get it
[![Latest release](https://img.shields.io/bintray/v/prateem/maven/shimmertextview?label=latest&style=flat-square)](#)

Available on jCenter.

```
implementation 'com.meetarp:shimmertextview:$shimmerTextViewVersion'
```

## Example
An example app is available that will build the following to a device:

### Visual
<img src="https://raw.githubusercontent.com/prateem/ShimmerTextView/master/example.gif" width="360" height="740">

### Activity
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val shimmerText = findViewById<ShimmerTextView>(R.id.shimmerText)
        val shimmerTextEnd = findViewById<ShimmerTextView>(R.id.shimmerTextEnd)
        
        shimmerText.setTraceColor(android.R.color.darker_gray) // Same as default
        shimmerText.setShimmerColor(android.R.color.white) // Same as default
        
        findViewById<CheckBox>(R.id.checkbox).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                shimmerText.startShimmer()
                shimmerTextEnd.startShimmer(800)
            } else {
                shimmerText.stopShimmer()
                shimmerTextEnd.stopShimmer()
            }
        }
    }

}
```

## Usage

`ShimmerTextView` exposes the following methods:

|Method|Description|Defaults|
|-------------|-----------|-------|
|`setTraceColor(ColorRes)`|Set the color of the traced silhouette(s). Argument must be a color resource integer.|`android.R.color.darker_gray`|
|`setShimmerColor(ColorRes)`|Set the color of the shimmer. Argument must be a color resource integer.|`android.R.color.white`|
|`startShimmer(Long)`|Start the shimmer animation. The argument provided is the period duration of the shimmer in milliseconds.|1000 ms|
|`stopShimmer()`|End any shimmer animation that may be active.|N/A|
