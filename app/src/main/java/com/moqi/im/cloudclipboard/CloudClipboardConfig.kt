package com.moqi.im.cloudclipboard

data class CloudClipboardConfig(
    val enabled: Boolean,
    val baseUrl: String,
    val username: String,
    val password: String,
    /** 墨奇设置根目录，如 /moqi-input-method/ */
    val settingsRootPath: String,
    val minIntervalMs: Long
) {
    fun settingsRootUrl(): String {
        val base = baseUrl.trimEnd('/')
        val path = settingsRootPath.trimStart('/').trimEnd('/')
        return "$base/$path/"
    }

    /** 云剪贴板目录：{设置目录}/clip/ */
    fun clipDirectoryUrl(): String {
        val base = baseUrl.trimEnd('/')
        val clipPath = MoqiWebDavPaths.clipPathUnder(settingsRootPath).trimStart('/')
        return "$base/$clipPath"
    }

    /** 用户词库目录：{设置目录}/dict/ */
    fun dictDirectoryUrl(): String {
        val base = baseUrl.trimEnd('/')
        val dictPath = MoqiWebDavPaths.dictPathUnder(settingsRootPath).trimStart('/')
        return "$base/$dictPath"
    }
}
