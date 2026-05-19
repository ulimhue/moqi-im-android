package com.moqi.im.cloudclipboard

data class UserDictSnapshotEntry(
    val schemeSet: String,
    val deviceId: String,
    val name: String,
    val lastModified: Long
)

data class UserDictSnapshotSyncResult(
    val downloaded: Int,
    val uploaded: Int
)
