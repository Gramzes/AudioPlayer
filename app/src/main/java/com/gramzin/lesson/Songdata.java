package com.gramzin.lesson;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

public class Songdata {

    private ArrayList songList = new ArrayList();
    final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    public ArrayList<SongFileInfo> getPlayList(Context context, Boolean music, Boolean ringtone) {
        {

            try {
                String[] proj = {MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.IS_MUSIC
                };

                Cursor audioCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);

                if (audioCursor != null) {
                    if (audioCursor.moveToFirst()) {
                        do {
                            int isMusic = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC);
                            int isMusic_b = audioCursor.getInt(isMusic);
                            if ((isMusic_b > 0 && !music) || (isMusic_b < 1 && !ringtone)){
                                continue;
                            }

                            int audioTitle = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                            int audioDisplayName = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                            int audioartist = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                            int audioduration = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                            int audiodata = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                            int audioalbum = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                            int audioalbumid = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                            int song_id = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);

                            SongFileInfo info = new SongFileInfo();
                            info.setFile_uri(audioCursor.getString(audiodata));
                            info.setTitle(audioCursor.getString(audioTitle));
                            info.setDisplayName(audioCursor.getString(audioDisplayName));
                            info.setDuration((audioCursor.getString(audioduration)));
                            info.setArtist(audioCursor.getString(audioartist));
                            info.setAlbum(audioCursor.getString(audioalbum));
                            info.setId(audioCursor.getLong(song_id));
                            info.setAudio_uri(Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(info.getId())));
                            info.setAlbum_art((ContentUris.withAppendedId(albumArtUri, audioCursor.getLong(audioalbumid))).toString());
                            if (new File(info.getFile_uri()).exists()) {
                                songList.add(info);
                            }
                        } while (audioCursor.moveToNext());
                    }
                }
                assert audioCursor != null;
                audioCursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return songList;
    }
}

