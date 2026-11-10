package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.ContentUris
import android.provider.MediaStore
import com.sftwr.cleandevelopment.musicplayer.data.model.PlaylistEntity
import com.sftwr.cleandevelopment.musicplayer.data.model.PlaylistSongCrossRef
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import com.sftwr.cleandevelopment.musicplayer.data.source.local.PlaylistDao
import com.sftwr.cleandevelopment.musicplayer.data.source.local.SongDao
import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.Genre
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        val recentlyAddedCount = playlistDao.getRecentlyAddedCountFlow()
        val recentlyPlayedCount = playlistDao.getRecentlyPlayedCountFlow()
        val mostPlayedCount = playlistDao.getMostPlayedCountFlow()
        val favoriteCount = playlistDao.getFavoriteCountFlow()
        val customPlaylists = playlistDao.getCustomPlaylistsWithCountFlow()

        return combine(
            recentlyAddedCount,
            recentlyPlayedCount,
            mostPlayedCount,
            favoriteCount,
            customPlaylists
        ) { recAdded, recPlayed, mostPlayed, favorites, customs ->
            val defaultPlaylists = listOf(
                Playlist.Default(
                    id = -1L,
                    name = "Recently Added",
                    trackCount = recAdded,
                    type = PlaylistType.RECENTLY_ADDED
                ),
                Playlist.Default(
                    id = -2L,
                    name = "Recently Played",
                    trackCount = recPlayed,
                    type = PlaylistType.RECENTLY_PLAYED
                ),
                Playlist.Default(
                    id = -3L,
                    name = "Most Played",
                    trackCount = mostPlayed,
                    type = PlaylistType.MOST_PLAYED
                ),
                Playlist.Default(
                    id = -4L,
                    name = "Favorite Tracks",
                    trackCount = favorites,
                    type = PlaylistType.FAVORITES
                )
            )

            val customDomainPlaylists = customs.map { custom ->
                Playlist.Custom(
                    id = custom.id,
                    name = custom.name,
                    trackCount = custom.trackCount,
                    createdAt = custom.createdAt
                )
            }

            defaultPlaylists + customDomainPlaylists
        }.flowOn(Dispatchers.IO)
    }

    override fun getPlaylistSongs(playlistId: Long, playlistType: PlaylistType): Flow<List<Song>> {
        val entityFlow = when (playlistType) {
            PlaylistType.RECENTLY_ADDED -> playlistDao.getRecentlyAddedSongsFlow()
            PlaylistType.RECENTLY_PLAYED -> playlistDao.getRecentlyPlayedSongsFlow()
            PlaylistType.MOST_PLAYED -> playlistDao.getMostPlayedSongsFlow()
            PlaylistType.FAVORITES -> playlistDao.getFavoriteSongsFlow()
            PlaylistType.CUSTOM -> playlistDao.getPlaylistSongsFlow(playlistId)
        }
        return entityFlow.map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFavoriteAlbums(): Flow<List<Album>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.groupBy { it.albumId }
                    .filter { (_, songEntities) ->
                        songEntities.isNotEmpty() && songEntities.all { it.isFavorite }
                    }
                    .map { (albumId, songEntities) ->
                        val first = songEntities.first()
                        Album(
                            albumId = albumId,
                            title = first.album.ifBlank { "Unknown Album" },
                            artist = first.artist.ifBlank { "Unknown Artist" },
                            trackCount = songEntities.size,
                            albumArtUri = "content://media/external/audio/albumart/$albumId",
                            isFavorite = true
                        )
                    }
                    .sortedBy { it.title }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getFavoriteArtists(): Flow<List<Artist>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.groupBy { it.artist.ifBlank { "Unknown Artist" } }
                    .filter { (_, songEntities) ->
                        songEntities.isNotEmpty() && songEntities.all { it.isFavorite }
                    }
                    .map { (artistName, songEntities) ->
                        val first = songEntities.first()
                        val albumCount = songEntities.map { it.albumId }.distinct().size
                        Artist(
                            name = artistName,
                            trackCount = songEntities.size,
                            albumCount = albumCount,
                            artistArtUri = "content://media/external/audio/albumart/${first.albumId}",
                            isFavorite = true
                        )
                    }
                    .sortedBy { it.name }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getFavoriteGenres(): Flow<List<GenreItem>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.groupBy { it.genre.ifBlank { "Unknown Genre" } }
                    .filter { (_, songEntities) ->
                        songEntities.isNotEmpty() && songEntities.all { it.isFavorite }
                    }
                    .map { (genreName, songEntities) ->
                        Genre(
                            name = genreName,
                            trackCount = songEntities.size
                        )
                    }
                    .sortedBy { it.name }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createCustomPlaylist(name: String, songIds: List<Long>): Long = withContext(Dispatchers.IO) {
        val entity = PlaylistEntity(name = name)
        val playlistId = playlistDao.insertPlaylist(entity)
        if (songIds.isNotEmpty()) {
            val crossRefs = songIds.map { songId ->
                PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
            }
            playlistDao.insertPlaylistSongCrossRefs(crossRefs)
        }
        playlistId
    }

    override suspend fun deleteCustomPlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        val crossRef = PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
        playlistDao.insertPlaylistSongCrossRef(crossRef)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistSongCrossRef(playlistId = playlistId, songId = songId)
    }

    override suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.setFavorite(songId, isFavorite)
    }

    override suspend fun toggleAlbumFavorite(albumId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.setAlbumFavorite(albumId, isFavorite)
    }

    override suspend fun toggleArtistFavorite(artistName: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.setArtistFavorite(artistName, isFavorite)
    }

    override suspend fun toggleGenreFavorite(genreName: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.setGenreFavorite(genreName, isFavorite)
    }

    override suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.recordSongPlayed(songId)
    }
}

private fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
    albumArtUri = null,
    duration = duration,
    size = size,
    path = path,
    year = year,
    genre = genre,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayedTimestamp = lastPlayedTimestamp,
    dateAdded = dateAdded
)
