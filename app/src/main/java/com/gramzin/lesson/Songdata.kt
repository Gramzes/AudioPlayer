package com.gramzin.lesson

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

class Songdata {
    private val songList: ArrayList<SongFileInfo> = ArrayList<SongFileInfo>()
    val albumArtUri = Uri.parse("content://media/external/audio/albumart")
    fun getPlayList(
        context: Context,
        music: Boolean?,
        ringtone: Boolean?
    ): ArrayList<SongFileInfo> {
        run {
            try {
                val proj = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.IS_MUSIC
                )
                val audioCursor = context.contentResolver
                    .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, null, null, null)
                if (audioCursor != null) {
                    if (audioCursor.moveToFirst()) {
                        do {
                            val isMusic =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                            val isMusic_b = audioCursor.getInt(isMusic)
                            if (isMusic_b > 0 && !music!! || isMusic_b < 1 && !ringtone!!) {
                                continue
                            }
                            val audioTitle =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                            val audioDisplayName =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                            val audioartist =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                            val audioduration =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                            val audiodata =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                            val audioalbum =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                            val audioalbumid =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                            val song_id =
                                audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                            val info = SongFileInfo()

                            info.file_uri = audioCursor.getString(audiodata)
                            info.title = audioCursor.getString(audioTitle)
                            info.displayName = audioCursor.getString(audioDisplayName)
                            info.duration = audioCursor.getString(audioduration)
                            info.artist = audioCursor.getString(audioartist)
                            info.album = audioCursor.getString(audioalbum)
                            info.id = audioCursor.getLong(song_id)
                            info.audio_uri = Uri.withAppendedPath(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                info.id.toString()
                            )
                            info.album_art = ContentUris.withAppendedId(
                                albumArtUri,
                                audioCursor.getLong(audioalbumid)
                            ).toString()

                            if (File(info.file_uri).exists()) {
                                songList.add(info)
                            }
                        } while (audioCursor.moveToNext())
                    }
                }
                assert(audioCursor != null)
                audioCursor!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return songList
    }
}