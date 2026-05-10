package com.moqi.im

import android.content.Context
import java.io.File

fun Context.moqiAndroidDataDir(): File {
    return File(getExternalFilesDir(null) ?: filesDir, "data").apply {
        mkdirs()
    }
}
