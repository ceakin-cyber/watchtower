package com.example.myapplication.ui.activitylog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.FileType
import com.example.myapplication.databinding.ItemAttachmentBinding

class AttachmentAdapter(
    private val onAttachmentClick: (EvidenceAttachment) -> Unit
) : ListAdapter<EvidenceAttachment, AttachmentAdapter.AttachmentViewHolder>(AttachmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding = ItemAttachmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttachmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AttachmentViewHolder(
        private val binding: ItemAttachmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attachment: EvidenceAttachment) {
            with(binding) {
                textFileName.text = attachment.file_name
                
                // Set appropriate icon based on file type
                val iconRes = when (attachment.file_type) {
                    FileType.PHOTO -> android.R.drawable.ic_menu_camera
                    FileType.AUDIO -> android.R.drawable.ic_btn_speak_now
                    FileType.VIDEO -> android.R.drawable.ic_media_play
                    FileType.DOCUMENT -> android.R.drawable.ic_menu_edit
                    FileType.SCREENSHOT -> android.R.drawable.ic_menu_camera
                    FileType.OTHER -> android.R.drawable.ic_menu_edit
                }
                iconFileType.setImageResource(iconRes)
                
                // Set click listener
                root.setOnClickListener {
                    onAttachmentClick(attachment)
                }
            }
        }
    }

    class AttachmentDiffCallback : DiffUtil.ItemCallback<EvidenceAttachment>() {
        override fun areItemsTheSame(oldItem: EvidenceAttachment, newItem: EvidenceAttachment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EvidenceAttachment, newItem: EvidenceAttachment): Boolean {
            return oldItem == newItem
        }
    }
}