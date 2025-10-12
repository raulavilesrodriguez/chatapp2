package com.packt.conversations.ui

import androidx.annotation.StringRes
import com.packt.conversations.R

enum class ActionOptions(@param:StringRes val id: Int) {
    GROUP(R.string.group),
    SETTINGS(R.string.settings);

    companion object{
        fun getOptions(): List<Int> {
            return entries.map { it.id }
        }

        fun getById(id: Int): ActionOptions {
            entries.forEach { actionOptions ->
                if(id == actionOptions.id) return actionOptions
            }
            return SETTINGS
        }
    }
}
