package com.postpc.chefi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.postpc.chefi.Chefi
import com.postpc.chefi.R
import com.postpc.chefi.database.AppRecipe
import kotlin.collections.ArrayList
import com.postpc.chefi.adapters.CommentAdapter


class CommentFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: CommentFragmentArgs by navArgs()
    private lateinit var recyclerViewComment: RecyclerView
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comment, container, false)
        val curRecipe: AppRecipe? = args.curRecipe
        recyclerViewComment = view.findViewById(R.id.recyclerViewComment)
        commentAdapter = CommentAdapter(curRecipe, view)
        recyclerViewComment.adapter = commentAdapter
        recyclerViewComment.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        commentAdapter.setItems(curRecipe?.comments)
        return view
    }

    override fun onPause() {
        super.onPause()
        commentAdapter.setItems(ArrayList())
    }
}