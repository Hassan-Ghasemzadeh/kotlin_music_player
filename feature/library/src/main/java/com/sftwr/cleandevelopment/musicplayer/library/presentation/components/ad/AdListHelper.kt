package com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ad

/**
 * Sealed wrapper for list items, representing either original data content or an injected Ad.
 */
sealed class ListItemWithAd<out T> {
    data class Item<T>(val data: T) : ListItemWithAd<T>()
    data class Ad(val adKey: String) : ListItemWithAd<Nothing>()
}

/**
 * Extension function that safely inserts a Native Ad item precisely every [interval] items (default 10 items).
 * If [isAdFree] is true, returns only original items with zero ad insertions.
 * Prevents index out of bounds by iterating cleanly over valid indices.
 */
fun <T> List<T>.insertAdsEvery(interval: Int = 10, isAdFree: Boolean = false): List<ListItemWithAd<T>> {
    if (isAdFree || interval <= 0 || isEmpty()) {
        return map { ListItemWithAd.Item(it) }
    }

    val result = mutableListOf<ListItemWithAd<T>>()
    var countSinceLastAd = 0

    for (i in indices) {
        if (i > 0 && countSinceLastAd == interval) {
            result.add(ListItemWithAd.Ad("ad_item_$i"))
            countSinceLastAd = 0
        }
        result.add(ListItemWithAd.Item(this[i]))
        countSinceLastAd++
    }

    return result
}
