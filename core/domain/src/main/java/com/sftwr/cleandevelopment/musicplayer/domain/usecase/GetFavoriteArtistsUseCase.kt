package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Placeholder Use Case for future Favorite Artists feature architecture.
 */
class GetFavoriteArtistsUseCase @Inject constructor() {
    operator fun invoke(): Flow<List<String>> {
        return flowOf(emptyList())
    }
}
