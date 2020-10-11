package com.postpc.chefi.listeners

import com.postpc.chefi.database.DbRecipe

interface RecipeClickListener {
    fun onToDoItemClicked(item: DbRecipe)
}