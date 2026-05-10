package com.moqi.im.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File

class RimeSafSync(private val context: Context) {
    private val resolver: ContentResolver = context.contentResolver

    fun importFrom(treeUri: Uri, runtimeRoot: File) {
        val tempRoot = File(context.cacheDir, "rime-saf-import").apply {
            deleteRecursively()
            mkdirs()
        }
        try {
            val root = treeRoot(treeUri)
            copyDocumentChildrenToFile(treeUri, root.documentId, tempRoot)
            requireContainsYaml(tempRoot)
            runtimeRoot.deleteRecursively()
            runtimeRoot.mkdirs()
            tempRoot.copyRecursively(runtimeRoot, overwrite = true)
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    fun exportTo(runtimeRoot: File, treeUri: Uri) {
        requireContainsYaml(runtimeRoot)
        val root = treeRoot(treeUri)
        deleteDocumentChildren(treeUri, root.documentId)
        runtimeRoot.listFiles().orEmpty().forEach { file ->
            copyFileToDocumentTree(file, root.uri)
        }
    }

    private fun copyDocumentChildrenToFile(treeUri: Uri, parentDocumentId: String, targetDir: File) {
        queryChildren(treeUri, parentDocumentId).forEach { child ->
            val target = File(targetDir, child.name).canonicalFile
            val targetRoot = targetDir.canonicalPath
            if (!target.path.startsWith(targetRoot + File.separator) && target.path != targetRoot) {
                error("文件名不安全: ${child.name}")
            }
            if (child.isDirectory) {
                target.mkdirs()
                copyDocumentChildrenToFile(treeUri, child.documentId, target)
            } else {
                target.parentFile?.mkdirs()
                resolver.openInputStream(child.uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: error("无法读取文件: ${child.name}")
            }
        }
    }

    private fun copyFileToDocumentTree(file: File, parentUri: Uri) {
        val documentUri = DocumentsContract.createDocument(
            resolver,
            parentUri,
            if (file.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else "application/octet-stream",
            file.name
        ) ?: error("无法创建文件: ${file.name}")
        if (file.isDirectory) {
            file.listFiles().orEmpty().forEach { child ->
                copyFileToDocumentTree(child, documentUri)
            }
        } else {
            resolver.openOutputStream(documentUri, "wt")?.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            } ?: error("无法写入文件: ${file.name}")
        }
    }

    private fun deleteDocumentChildren(treeUri: Uri, parentDocumentId: String) {
        queryChildren(treeUri, parentDocumentId).forEach { child ->
            if (child.isDirectory) {
                deleteDocumentChildren(treeUri, child.documentId)
            }
            DocumentsContract.deleteDocument(resolver, child.uri)
        }
    }

    private fun queryChildren(treeUri: Uri, parentDocumentId: String): List<DocumentEntry> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
        val entries = mutableListOf<DocumentEntry>()
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex) ?: documentId.substringAfterLast('/')
                val mimeType = cursor.getString(mimeIndex) ?: "application/octet-stream"
                entries += DocumentEntry(
                    documentId = documentId,
                    name = name,
                    mimeType = mimeType,
                    uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                )
            }
        }
        return entries
    }

    private fun treeRoot(treeUri: Uri): DocumentEntry {
        val documentId = DocumentsContract.getTreeDocumentId(treeUri)
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return DocumentEntry(
            documentId = documentId,
            name = "",
            mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
            uri = uri
        )
    }

    private fun requireContainsYaml(root: File) {
        if (!root.exists() || !root.walkTopDown().any { it.isFile && it.extension.equals("yaml", ignoreCase = true) }) {
            error("目录中没有发现 Rime YAML 配置")
        }
    }

    private data class DocumentEntry(
        val documentId: String,
        val name: String,
        val mimeType: String,
        val uri: Uri
    ) {
        val isDirectory: Boolean
            get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
    }
}
