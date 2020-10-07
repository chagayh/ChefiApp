package com.example.chefi.fragment

import ItemOffsetDecoration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.adapters.ProfileAdapter
import com.example.chefi.adapters.TradeAdapter

class TradeFragment : Fragment() {

    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private lateinit var recyclerViewTrade: RecyclerView
    private lateinit var tradeRecipesAdapter: TradeAdapter
    private var SPAN_VALUE: Int = 3


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trade, container, false)
        recyclerViewTrade = view.findViewById(R.id.recyclerViewTrade)
        tradeRecipesAdapter = TradeAdapter()
        recyclerViewTrade.adapter = tradeRecipesAdapter
//        val lm = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
//        recyclerViewRecipes.layoutManager = lm
        val gridLayoutManager: GridLayoutManager = GridLayoutManager(activity, SPAN_VALUE)
//        val decoration = ItemOffsetDecoration(view.context, R.dimen.item_offset)
//        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return when (position) {
//                    0 -> SPAN_VALUE
//                    else -> 1
//                }
//            }
//        }
        recyclerViewTrade.layoutManager = gridLayoutManager
        val reps = appContext.getUserRecipes()
        tradeRecipesAdapter.setItems(reps)
//        recyclerViewTrade.addItemDecoration(decoration)
//        recyclerViewTrade.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
//        recyclerViewTrade.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.HORIZONTAL))
        return view
    }
}