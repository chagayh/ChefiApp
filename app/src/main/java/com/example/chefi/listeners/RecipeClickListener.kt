package com.example.chefi.listeners

import com.example.chefi.database.Recipe

interface RecipeClickListener {
    fun onToDoItemClicked(item: Recipe)
}