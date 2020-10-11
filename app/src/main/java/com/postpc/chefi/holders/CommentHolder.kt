package com.postpc.chefi.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.R

class CommentHolder(view: View): RecyclerView.ViewHolder(view) {
    val content: TextView = view.findViewById(R.id.textViewCommentContent)
    val name: TextView = view.findViewById(R.id.textViewComment)
    val username: TextView = view.findViewById(R.id.textViewCommentSub)
    val timestamp: TextView = view.findViewById(R.id.textViewTimestampComment)
}