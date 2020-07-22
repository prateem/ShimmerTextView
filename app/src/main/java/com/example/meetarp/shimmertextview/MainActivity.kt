package com.example.meetarp.shimmertextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import com.meetarp.shimmertextview.ShimmerTextView

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