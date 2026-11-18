package com.sftwr.cleandevelopment.musicplayer.data.source.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object OnlineLyricFetcher {

    suspend fun fetchLyrics(title: String, artist: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First attempt: LRCLIB direct get endpoint
            val lrclibDirectResult = fetchFromLrclibGet(title, artist)
            if (lrclibDirectResult != null && lrclibDirectResult.isNotBlank()) {
                return@withContext Result.success(lrclibDirectResult)
            }

            // Second attempt: LRCLIB search endpoint
            val lrclibSearchResult = fetchFromLrclibSearch(title, artist)
            if (lrclibSearchResult != null && lrclibSearchResult.isNotBlank()) {
                return@withContext Result.success(lrclibSearchResult)
            }

            // Third attempt: Lyrics.ovh API fallback
            val lyricsOvhResult = fetchFromLyricsOvh(title, artist)
            if (lyricsOvhResult != null && lyricsOvhResult.isNotBlank()) {
                return@withContext Result.success(lyricsOvhResult)
            }

            Result.failure(Exception("No lyrics found online for $title by $artist"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchFromLrclibGet(title: String, artist: String): String? {
        val encodedArtist = URLEncoder.encode(artist, "UTF-8")
        val encodedTrack = URLEncoder.encode(title, "UTF-8")
        val urlStr = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTrack"

        val jsonStr = httpGet(urlStr) ?: return null
        val json = JSONObject(jsonStr)
        val synced = json.optString("syncedLyrics")
        if (!synced.isNull_or_blank()) return synced

        val plain = json.optString("plainLyrics")
        if (!plain.isNull_or_blank()) return plain

        return null
    }

    private fun fetchFromLrclibSearch(title: String, artist: String): String? {
        val query = "$title $artist".trim()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlStr = "https://lrclib.net/api/search?q=$encodedQuery"

        val jsonStr = httpGet(urlStr) ?: return null
        val array = JSONArray(jsonStr)
        if (array.length() == 0) return null

        val firstObj = array.getJSONObject(0)
        val synced = firstObj.optString("syncedLyrics")
        if (!synced.isNull_or_blank()) return synced

        val plain = firstObj.optString("plainLyrics")
        if (!plain.isNull_or_blank()) return plain

        return null
    }

    private fun fetchFromLyricsOvh(title: String, artist: String): String? {
        val encodedArtist = URLEncoder.encode(artist, "UTF-8")
        val encodedTrack = URLEncoder.encode(title, "UTF-8")
        val urlStr = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTrack"

        val jsonStr = httpGet(urlStr) ?: return null
        val json = JSONObject(jsonStr)
        val lyrics = json.optString("lyrics")
        return if (!lyrics.isNull_or_blank()) lyrics else null
    }

    private fun httpGet(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "MusicPlayerApp/1.0")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty() || this == "null"
    }
}
