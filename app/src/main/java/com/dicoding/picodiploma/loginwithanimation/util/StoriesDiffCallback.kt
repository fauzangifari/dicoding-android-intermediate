package com.dicoding.picodiploma.loginwithanimation.util

import androidx.recyclerview.widget.DiffUtil
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem

class StoriesDiffCallback : DiffUtil.ItemCallback<ListStoryItem>() {
    override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
        return oldItem == newItem
    }
}
