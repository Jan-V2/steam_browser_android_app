package com.example.john.testapp

import android.util.Log

class Url_Builder(){
    //utility for building urls
    private val items = mutableListOf<String>()

    fun add(path:String){
        items.add(path)
    }

    override fun toString(): String{
        return if (items.size > 0){

            var ret = items[0]
            if (items.size > 1){

                for(i in 1 until items.size){
                    ret += "/" + items[i]
                }
            }
            //Log.i("url_builder", ret)
            ret
        }else{
            //Log.i("url_builder", "empty")
            ""
        }
    }

    fun append_to_last_item(string: String){
        if (items.size > 0){
            items[items.size-1] += string
        }else{
            throw IllegalAccessError("Can't append to last item, no items in builder.")
        }
    }
}