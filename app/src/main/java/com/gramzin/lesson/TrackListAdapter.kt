package com.gramzin.lesson

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.gramzin.lesson.databinding.TrackListItemBinding
import java.io.File
import java.net.URI


class TrackListAdapter(private val player: MyMediaPlayer,
                       private val onClick: (info: SongFileInfo) -> Unit):
    RecyclerView.Adapter<TrackListAdapter.TrackListHolder>() {
    init{
        player.addOnRemoveTrackListener {
            notifyItemRemoved(it)
        }
    }
    var multiSelect = false
    lateinit var binding: TrackListItemBinding
    var selectedTracks = arrayListOf<SongFileInfo>()
    var actionMode : ActionMode? = null


    inner class TrackListHolder(private val binding: TrackListItemBinding):
        RecyclerView.ViewHolder(binding.root){

        lateinit var info_: SongFileInfo
        var isPlaying = false

        fun bind(info:SongFileInfo){
            this.info_ = info
            binding.trackName.text = info_.title
            binding.artistName.text = info_.artist
            val mmr = MediaMetadataRetriever()
            if (File(info_.file_uri).exists()) {
                mmr.setDataSource(info_.file_uri)
                val data = mmr.getEmbeddedPicture()
                if (data != null) {
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    binding.albumCover.setImageBitmap(bitmap)
                } else {
                    binding.albumCover.setImageResource(R.drawable.uncovered)
                }
            }
            binding.root.setOnClickListener{
                if (multiSelect) {
                    selectTrack(info)
                }
                else{
                    onClick(info_)
                }
            }
            if (multiSelect && selectedTracks.contains(info)){
                binding.root.setBackgroundColor(Color.YELLOW)
            }
            else{
                binding.root.setBackgroundColor(Color.WHITE)
            }
            binding.root.setOnLongClickListener{
                actionMode = (it.context as AppCompatActivity).startSupportActionMode(callback)
                selectTrack(info)
                return@setOnLongClickListener true
            }
        }

        var callback = object : ActionMode.Callback{
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                multiSelect = true
                (binding.root.context as AppCompatActivity).menuInflater.inflate(R.menu.menu, p1)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                AlertDialog.Builder(binding.root.context).setTitle("Удаление")
                    .setMessage("Вы уверены, что хотите удалить выбранные треки с устройства?")
                    .setNeutralButton("Удалить"){_,_->
                        selectedTracks.forEach {
                            binding.root.context.contentResolver.delete(it.audio_uri!!, null, null)
                            player.trackList.remove(it)
                        }
                        selectedTracks.clear()
                        notifyDataSetChanged()
                        p0?.finish()
                    }.setNegativeButton("Отмена"){_,_->}.show()
                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                multiSelect = false
                selectedTracks.clear()
                notifyDataSetChanged()
            }

        }

        fun selectTrack(track: SongFileInfo){
            if(!selectedTracks.contains(track)){
                selectedTracks.add(track)
            }
            else{
                selectedTracks.remove(track)
            }
            actionMode?.title = "${selectedTracks.size} tracks selected"
            notifyItemChanged(absoluteAdapterPosition)
        }

        fun setHolderPlaying(play: Boolean){
            isPlaying = play
            if(play){
                binding.root.setBackgroundColor(Color.rgb(128, 128, 128))
            }
            else{
                binding.root.setBackgroundColor(Color.WHITE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackListHolder {
        binding = TrackListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        var holder = TrackListHolder(binding)
        player.addOnNextTrackListener {
            if (holder.isPlaying && !holder.info_.file_uri.equals(player.trackList[it].file_uri)){
                holder.setHolderPlaying(false)
            }
            else if(holder.info_.file_uri.equals(player.trackList[it].file_uri)){
                holder.setHolderPlaying(true)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: TrackListHolder, position: Int) {
        holder.bind(player.get(position))
    }

    override fun getItemCount(): Int {
        return player.size()
    }

}