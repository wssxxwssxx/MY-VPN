package com.example.chikuvpn

import android.net.Uri

object Utils {
    /**
     * Convert drawable image resource to string
     *
     * @param resourceId drawable image resource
     * @return image path
     */
    fun getImgURL(resourceId: Int): String {
        return Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/$resourceId").toString()
    }
}
