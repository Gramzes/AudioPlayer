package com.gramzin.lesson

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gramzin.lesson.databinding.ActivityTrackListBinding

const val permissionCode = 777
const val preferencesFile  = "settings"
const val settingLastTrack = "lastTrack"

class TrackList : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var binding: ActivityTrackListBinding
    lateinit var mp: MyMediaPlayer

    lateinit var runnable: Runnable
    lateinit var mpModel: MediaPlayerModel

    private val onSBChanged = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            if (p2) {
                mp.seekTo(p1 * 1000)
            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    }

    fun onNextTrack(info: SongFileInfo){
        mp.nextTrack(info.file_uri!!)
        val bsb = BottomSheetBehavior.from(binding.bottomSheet.bottomSheetLayout)
        bsb.state = BottomSheetBehavior.STATE_COLLAPSED
        setNewTrack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.registerOnSharedPreferenceChangeListener(this)

        mpModel = ViewModelProvider(this)[MediaPlayerModel::class.java]
        if (mpModel.mp != null){
            mp = mpModel.mp!!
            setUITrack(mp.isPlaying)
            binding.rcTracks.adapter = TrackListAdapter(mp, this::onNextTrack)
            binding.rcTracks.layoutManager = LinearLayoutManager(this)
            setNewTrack()
        }
        else {
            checkPermission()
        }
        mp.setOnCompletionListener(this::onCompletion)

        with(binding.bottomSheet) {
            pauseBack.setOnClickListener(this@TrackList::pressPauseBtn)
            nextBackground.setOnClickListener(this@TrackList::pressNextBtn)
            prevBackground.setOnClickListener(this@TrackList::pressPrevBtn)
            seekBar2.setOnSeekBarChangeListener(onSBChanged)
            card.setOnClickListener(this@TrackList::pressCard)

            val bsb = BottomSheetBehavior.from(bottomSheetLayout)

            runnable = Runnable {
                seekBar2.progress = mp.currentPosition / 1000
                progressBar.progress = mp.currentPosition / 1000
                val minutes = (mp.currentPosition / 1000 % 60).toString().padStart(2, '0')
                if (binding.bottomSheet.card.alpha == 0f || binding.bottomSheet.card.alpha == 1f) {
                    binding.bottomSheet.currentPosition.text =
                        "${mp.currentPosition / 1000 / 60}:$minutes"
                }
                Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
            }

            Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)

            bsb.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when(newState){
                        BottomSheetBehavior.STATE_DRAGGING -> {

                        }
                        BottomSheetBehavior.STATE_HIDDEN -> mp.stop()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset>=0) {
                        binding.bottomSheet.card.animate().alpha(1-slideOffset).setDuration(0).start()
                    }
                }

            })
        }


    }

    override fun onStop() {
        val preferences = this.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val edit = preferences.edit()
        edit.putString(settingLastTrack, mp.trackList[mp.trackNow].file_uri).commit()
        super.onStop()
    }


    private fun pressCard(view: View){
        BottomSheetBehavior.from(binding.bottomSheet.bottomSheetLayout).setState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun pressPauseBtn(view: View) = with(binding.bottomSheet) {
        if (mp.isPlaying) {
            mp.pause()
            albumCover.animate().scaleX(1f).scaleY(1f).setDuration(200)
            pauseImage.animate().alpha(0f).setDuration(1000)
            playImage.animate().alpha(1f).setDuration(1000)
        } else {
            mp.start()
            albumCover.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
            playImage.animate().alpha(0f).setDuration(1000)
            pauseImage.animate().alpha(1f).setDuration(1000)
        }
    }

    private fun setUITrack(play: Boolean) = with(binding.bottomSheet){
        if (play) {
            pauseImage.alpha = 1f
            playImage.alpha = 0f
            albumCover.animate().scaleX(1.2f).scaleY(1.2f).setDuration(0)
        }
        else{
            pauseImage.alpha = 0f
            playImage.alpha = 1f
            albumCover.animate().scaleX(1f).scaleY(1f).setDuration(0)
        }
    }

    private fun pressNextBtn(view: View){
        mp.nextTrack()
        setNewTrack()
    }

    private fun pressPrevBtn(view: View){
        mp.previousTrack()
        setNewTrack()
    }

    private fun onCompletion(mediaPlayer: MediaPlayer){
        if(::mp.isInitialized) {
            mp.nextTrack(true)
            setNewTrack()
        }
    }

    private fun initMediaPlayer(){
        val pf = PreferenceManager.getDefaultSharedPreferences(this);
        val music = pf?.getBoolean("music", true)?: true
        val ringtone = pf?.getBoolean("ringtone", true)?: true
        val trackList = Songdata().getPlayList(this, music, ringtone)
        val preferences = this.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val lastTrack = preferences.getString(settingLastTrack, null)
        mp = MyMediaPlayer(trackList)
        binding.rcTracks.adapter = TrackListAdapter(mp, this::onNextTrack)
        binding.rcTracks.layoutManager = LinearLayoutManager(this)
        mpModel.mp = mp
        mp.nextTrack()
        lastTrack?.let { mp.nextTrack(lastTrack) }
        setNewTrack()
    }

    fun setNewTrack(){
        if (mp.trackNow < 0){
            return
        }
        val song = mp.trackList[mp.trackNow]

        with(binding.bottomSheet){
            trackName.text = song.title
            trackNameCard.text = song.title
            artistName.text = song.artist
            artistNameCard.text = song.artist

            seekBar2.progress = 0
            val trackLen = mp.duration / 1000
            seekBar2.max = trackLen
            progressBar.max = trackLen
            currentPosition.text = "0:00"
            val minutes = (trackLen % 60).toString().padStart(2, '0')
            trackLength.text = "${trackLen / 60}:$minutes"

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(song.file_uri)
            val data = mmr.getEmbeddedPicture()
            if (data!=null){
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                albumCover.setImageBitmap(bitmap)
                albumCoverMini.setImageBitmap(bitmap)
            }
            else{
                albumCover.setImageResource(R.drawable.uncovered)
                albumCoverMini.setImageResource(R.drawable.uncovered)
            }
        }
    }

    private fun checkPermission(){
        val permissionStatus = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            initMediaPlayer()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), permissionCode)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initMediaPlayer()
            }
            else{
                Toast.makeText(this, "Вы запретили доступ к аудио", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.Settings ->{
                val i = Intent(this, SettingsActivity::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        initMediaPlayer()
    }
}