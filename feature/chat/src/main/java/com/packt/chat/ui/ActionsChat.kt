package com.packt.chat.ui

import androidx.annotation.StringRes
import com.packt.chat.feature.chat.R

enum class ActionsChat(@param:StringRes val id: Int) {
    DELETE(R.string.delete_chat);

    companion object {
        fun getOptions(): List<Int> {
            return entries.map { it.id }
        }
        fun getById(id:Int): ActionsChat{
            entries.forEach { option ->
                if(id == option.id) return option
            }
            return DELETE
        }
    }
}

enum class ActionsGroup(@param:StringRes val id: Int){
    ADD(R.string.add_participant),
    LEFT(R.string.get_out_group);

    companion object{
        fun getOptions(): List<Int>{
            return entries.map { it.id }
        }
        fun getById(id:Int): ActionsGroup{
            entries.forEach { option ->
                if(id == option.id) return option
            }
            return ADD
        }
    }
}