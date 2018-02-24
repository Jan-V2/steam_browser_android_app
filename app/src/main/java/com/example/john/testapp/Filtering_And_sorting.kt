package com.example.john.testapp

import android.util.Log
import org.json.JSONObject
import java.io.Serializable
import java.util.Comparator


abstract class Filtering_And_sorting {

    data class Filter(var bundles_only :Int, // can be 0 for no 1 for yes and 2 for no bundles
                                             // this bit is very confusing
                      var discount : Setting_Range,
                      var reviews : Setting_Range,
                      var rating : Setting_Range,
                      var old_price : Setting_Range,
                      var new_price : Setting_Range,
                      var absolute_discount : Setting_Range) : Serializable {

        private val defaults = Defaults(0)

        fun filter_list(items: List<JSONObject>): List<JSONObject>{

            fun filter(item: JSONObject): Boolean{

                fun apply_bundle_filter():Boolean{
                    val is_bundle = item.getBoolean("is_bundle")

                    val setting = this.bundles_only
                    if (is_bundle){
                        if (setting != 2){
                            return true
                        }
                    } else {
                        if (setting != 1){
                            return true
                        }
                    }
                    Log.i("bundle_filter", is_bundle.toString())
                    return false
                }

                fun filter_int(key:String, range: Setting_Range): Boolean{
                    val value = item.getInt(key)
                    if (value >= range.min && value <= range.max){
                        return true
                    }
                    Log.e(key, value.toString())
                    return false
                }

                fun filter_absolute_discount():Boolean{
                    val range = this.absolute_discount
                    val value = item.getInt("old_price") - item.getInt("new_price")
                    if (value >= range.min && value <= range.max){
                        return true
                    }
                    Log.e("absolute_discount", value.toString())
                    return false
                }

                if (filter_absolute_discount()){
                    if (filter_int("discount_percents", this.absolute_discount)){
                        if (filter_int("old_price", this.old_price)){
                            if (filter_int("new_price", this.new_price)){
                                if (filter_int("percent_reviews_positive", this.rating)){
                                    if (filter_int("n_user_reviews", this.reviews)){
                                        if (apply_bundle_filter()){
                                            return true
                                        }}}}}}}
                return false
            }

            return items.filter {  filter(it) }

        }

        constructor(): this(0, Setting_Range(0,0), Setting_Range(0,0), Setting_Range(0,0), Setting_Range(0,0), Setting_Range(0,0),Setting_Range(0,0) )

        init {
            bundles_only = defaults.bundles_only // can be 0 for no 1 for yes and 2 for no bundles
            discount = defaults.discount
            reviews = defaults.reviews
            rating = defaults.rating
            old_price = defaults.old_price
            new_price = defaults.new_price
            absolute_discount = defaults.absolute_discount
        }

        fun set_defaults(){
            this.bundles_only = defaults.bundles_only // can be 0 for no 1 for yes and 2 for no bundles
            this.discount = defaults.discount
            this.reviews = defaults.reviews
            this.rating = defaults.rating
            this.old_price = defaults.old_price
            this.new_price = defaults.new_price
            this.absolute_discount = defaults.absolute_discount
        }

        private data class Defaults(var serialisation_placeholder: Int): Serializable{
            val bundles_only = 0 // can be 0 for no 1 for yes and 2 for no bundles
            val discount = Setting_Range(0, 100)
            val reviews = Setting_Range(0, 100000000)
            val rating = Setting_Range(0, 100)
            val absolute_discount = Setting_Range(0, 1000)
            val old_price = Setting_Range(0, 1000)
            val new_price = Setting_Range(0, 1000)
        }

        data class Setting_Range constructor(var min: Int, var max: Int) : Serializable

    }

    class Sorter {
        private val pagelength = 10

        fun sort(result_array: List<JSONObject>, comparator: Comparator<JSONObject>, reverse_order: Boolean): Array<List<JSONObject>>{
            var sorted = result_array.sortedWith(comparator)
            if (reverse_order) sorted = sorted.reversed()
            return split_into_pages(sorted)
        }

        private fun split_into_pages(array: List<JSONObject>): Array<List<JSONObject>>{

            fun slice_jsonarray(array: List<JSONObject>, start: Int, end: Int): List<JSONObject>{
                val ret = mutableListOf(JSONObject())
                ret.clear() // for some reason it's initialised with an empty index at 0
                var j = start
                while (j < end){
                    ret.add(array[j])
                    j++
                }
                return ret.toList()
            }
            var i = 0
            val temp = ArrayList<List<JSONObject>>()
            while (i < array.size){

                if (i + pagelength < array.size){
                    temp.add(slice_jsonarray(array, i, i + pagelength))
                } else {
                    temp.add(slice_jsonarray(array, i, array.size))
                }
                i += pagelength
            }

            return temp.mapToTypedArray { it }
        }

        inline fun <T, reified R> List<T>.mapToTypedArray(transform: (T) -> R): Array<R> {
            return when (this) {
                is RandomAccess -> Array(size) { index -> transform(this[index]) }
                else -> with(iterator()) { Array(size) { transform(next()) } }
            }
        }
    }

    class Sort_Comparators {
        val new_price = from_int_key("new_price")
        val old_price = from_int_key("old_price")
        val number_user_reviews = from_int_key("n_user_reviews")
        val percent_reviews_positive = from_int_key("percent_reviews_positive")
        val absolute_discount = compareBy<JSONObject> {it.getInt("old_price") - it.getInt("new_price")}

        private fun from_int_key(sort_key: String): Comparator<JSONObject> {
            return compareBy<JSONObject> {it.getInt(sort_key)}
        }
    }
}
