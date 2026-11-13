package com.sftwr.cleandevelopment.musicplayer.data.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File

/**
 * Helper utility to determine whether a given audio file path matches any excluded folders
 * (supporting both absolute file paths and SAF DocumentTree content URIs).
 */
object FolderFilterHelper {
    fun isPathExcluded(filePath: String, excludedFolders: Set<String>, context: Context): Boolean {
        if (filePath.isEmpty() || excludedFolders.isEmpty()) return false
        val file = File(filePath)
        val parentFolder = file.parent ?: filePath

        for (excluded in excludedFolders) {
            if (parentFolder.equals(excluded, ignoreCase = true) ||
                parentFolder.startsWith("$excluded/") ||
                filePath.equals(excluded, ignoreCase = true) ||
                filePath.startsWith("$excluded/")) {
                return true
            }

            // Handle SAF content URIs (e.g., content://.../tree/primary%3AMusic)
            if (excluded.startsWith("content://")) {
                try {
                    val uri = Uri.parse(excluded)
                    val treeId = DocumentsContract.getTreeDocumentId(uri)
                    if (treeId != null) {
                        val split = treeId.split(":")
                        if (split.size > 1) {
                            val subPath = split[1]
                            if (subPath.isNotBlank() && (filePath.contains("/$subPath/") || filePath.endsWith("/$subPath") || filePath.contains(subPath, ignoreCase = true))) {
                                return true
                            }
                        }
                    }
                } catch (e: Exception) {
                    val decoded = Uri.decode(excluded)
                    if (filePath.contains(decoded, ignoreCase = true)) {
                        return true
                    }
                }
            } else {
                val decoded = Uri.decode(excluded)
                if (filePath.contains(decoded, ignoreCase = true) || parentFolder.contains(decoded, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Formats a raw folder path or SAF content URI into a clean, human-readable folder name
     * suitable for display in UI lists.
     */
    fun formatFolderPath(folderPath: String): String {
        if (folderPath.startsWith("content://")) {
            try {
                val uri = Uri.parse(folderPath)
                val treeId = DocumentsContract.getTreeDocumentId(uri)
                if (treeId != null) {
                    val split = treeId.split(":")
                    if (split.size > 1) {
                        return split[1].replace("%2F", "/")
                    }
                }
                val decoded = Uri.decode(folderPath)
                val segments = decoded.split("/")
                return segments.lastOrNull { it.isNotBlank() } ?: decoded
            } catch (_: Exception) {
                return Uri.decode(folderPath)
            }
        }
        val file = File(folderPath)
        return file.name.ifBlank { folderPath }
    }
}
