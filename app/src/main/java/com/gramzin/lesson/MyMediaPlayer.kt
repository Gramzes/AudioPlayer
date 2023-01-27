package com.gramzin.lesson

import android.media.MediaPlayer
import android.util.Log
import java.io.File

class MyMediaPlayer(val trackList: ArrayList<SongFileInfo>): MediaPlayer(){
    var trackNow = -1
    private var onRemoveListener = mutableListOf<(i: Int) -> Unit >()
    private var onStopListener = mutableListOf<() -> Unit>()
    private var onNextTrackListener = mutableListOf<(nextTrack: Int) -> Unit>()

    fun get(i: Int) = trackList[i]
    fun set(i: Int, value: SongFileInfo) {trackList[i] = value}
    fun size(): Int = trackList.size

    fun addOnRemoveTrackListener(listener: (i: Int) -> Unit){
        onRemoveListener.add(listener)
    }
    fun addOnStopListener(listener: () -> Unit){
        onStopListener.add(listener)
    }
    fun addOnNextTrackListener(listener: (nextTrack: Int) -> Unit) {
        onNextTrackListener.add(listener)
    }


    override fun stop() {
        super.stop()
        onStopListener.forEach { it.invoke() }
    }

    fun previousTrack(){
        trackNow--
        if (trackNow < 0){
            trackNow = trackList.size - 1
        }
        while(trackList.isNotEmpty() && !File(trackList[trackNow].file_uri).exists()){
            if (trackNow < 0){
                trackNow = trackList.size - 1
            }
            removeTrack(trackNow)
        }
        if (trackList.size != 0) {
            play(trackList[trackNow], isPlaying)
            onNextTrackListener.forEach { it.invoke(trackNow) }
        }
    }

    fun nextTrack(play:Boolean = isPlaying){
        trackNow++
        findNextTrack()
        if (trackNow >= 0) {
            play(trackList[trackNow], play)
            onNextTrackListener.forEach { it.invoke(trackNow) }
        }
    }

    fun nextTrack(number: Int, play:Boolean = isPlaying){
        Log.d("alexkeks", "nextTrack start:" + trackNow.toString() + "number: " + number)
        trackNow = number
        findNextTrack()
        if (trackNow >= 0) {
            play(trackList[trackNow], play)
            onNextTrackListener.forEach { it.invoke(trackNow) }
        }
    }

    fun nextTrack(uri: String, play:Boolean = isPlaying){
        val position = trackList.indexOfFirst { it.file_uri.equals(uri) }
        if (position == -1)
            return
        trackNow = position
        findNextTrack()
        if (trackNow >= 0) {
            play(trackList[trackNow], play)
            onNextTrackListener.forEach { it.invoke(trackNow) }
        }
    }

    private fun findNextTrack(){
        if (trackList.size == 0){
            trackNow = -1
        }
        if (trackNow > trackList.size - 1){
            trackNow = 0
        }
        while(trackList.isNotEmpty() && !File(trackList[trackNow].file_uri).exists()){
            removeTrack(trackNow)
            Log.d("alexkeks", "remove")
            if (trackNow > trackList.size - 1){
                trackNow = 0
            }
        }
    }
    private fun play(song:SongFileInfo, play: Boolean){
        reset()
        setDataSource(song.file_uri)
        prepare()
        if (play) {
            start()
        }
    }

    fun removeTrack(i: Int){
        trackList.removeAt(i)
        onRemoveListener.forEach { it.invoke(i) }
    }

}