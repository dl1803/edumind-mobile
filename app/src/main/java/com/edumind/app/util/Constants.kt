package com.edumind.app.util

import com.edumind.app.BuildConfig

object Constants {
    const val BASE_URL = BuildConfig.API_BASE_URL
    const val ACCESS_TOKEN_KEY = "access_token"
    const val REFRESH_TOKEN_KEY = "refresh_token"
    const val PREFS_NAME = "edumind_prefs"
    const val SAMPLE_HLS_URL = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
    val VIDEO_URL_TTL_MS: Long =
        if (BuildConfig.USE_FAKE_LESSON_API) 5L * 60L * 1000L else 2L * 60L * 60L * 1000L
}
