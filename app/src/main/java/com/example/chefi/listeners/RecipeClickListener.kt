package com.example.chefi.listeners

import com.example.chefi.database.DbRecipe

interface RecipeClickListener {
    fun onToDoItemClicked(item: DbRecipe)
}