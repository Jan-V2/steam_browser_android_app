package com.example.john.testapp

import android.util.Log
import org.json.JSONObject
import java.io.Serializable
import java.util.Comparator


class Filtering_And_sorting {
    companion object {
        data class Filter constructor(var bundles_only :Int = 0, // can be 0 for no 1 for yes and 2 for no bundles
                // this bit is very confusing
                                      var discount : Setting_Range = Setting_Range(),
                                      var reviews : Setting_Range = Setting_Range(),
                                      var rating : Setting_Range = Setting_Range(),
                                      var old_price : Setting_Range = Setting_Range(),
                                      var new_price : Setting_Range = Setting_Range(),
                                      var absolute_discount : Setting_Range = Setting_Range(),
                                      var defaults: Defaults = Defaults(),
                                      var sort_order_offset :Boolean = false,
                                      var last_sort_by_idx :Int = 0) : Serializable {


            constructor(result_list: List<JSONObject>): this(){
                defaults = Defaults(result_list)
                set_defaults()
            }

            fun filter_list(items: List<JSONObject>): List<JSONObject>{

                fun filter(item: JSONObject): Boolean{
                    val keys = Keys.Companion.Filter_Keys()

                    fun apply_bundle_filter():Boolean{
                        val is_bundle = item.getBoolean(keys.is_bundle)

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
                        Log.e(key, value.toString() + " min " + range.min.toString() + " max " + range.max.toString())
                        return false
                    }

                    fun filter_absolute_discount():Boolean{
                        val range = this.absolute_discount
                        val value = item.getInt(keys.old_price) - item.getInt(keys.new_price)
                        if (value >= range.min && value <= range.max){
                            return true
                        }
                        Log.e("def_absolute_discount", value.toString() + " min " + range.min.toString() + " max " + range.max.toString())
                        return false
                    }


                    if (filter_absolute_discount()){
                        if (filter_int(keys.discount, this.discount)){
                            if (filter_int(keys.old_price, this.old_price)){
                                if (filter_int(keys.new_price, this.new_price)){
                                    if (filter_int(keys.rating, this.rating)){
                                        if (filter_int(keys.reviews, this.reviews)){
                                            if (apply_bundle_filter()){
                                                return true
                                            }}}}}}}
                    return false
                }

                return items.filter {  filter(it) }

            }

            fun set_defaults(){
                this.bundles_only = defaults.def_bundles_only // can be 0 for no 1 for yes and 2 for no bundles
                this.discount = defaults.def_discount
                this.reviews = defaults.def_reviews
                this.rating = defaults.def_rating
                this.old_price = defaults.def_old_price
                this.new_price = defaults.def_new_price
                this.absolute_discount = defaults.def_absolute_discount
            }


            class Defaults constructor(var def_bundles_only: Int = 0,
                                       var def_discount: Setting_Range = Setting_Range(),
                                       var def_reviews: Setting_Range = Setting_Range(),
                                       var def_rating: Setting_Range = Setting_Range(),
                                       var def_absolute_discount: Setting_Range = Setting_Range(),
                                       var def_old_price: Setting_Range = Setting_Range(),
                                       var def_new_price: Setting_Range = Setting_Range()
            ) : Serializable {
                constructor(result_list: List<JSONObject>) : this() {

                    fun find_min_max(result_list: List<JSONObject>, key: (JSONObject)->Int): Setting_Range {
                        var min = Int.MAX_VALUE
                        var max = 0

                        for (result in result_list){
                            val value = key(result)
                            if (value > max){
                                max = value
                            }
                            if (value < min){
                                min = value
                            }
                        }
                        return Setting_Range(min, max)
                    }

                    val find_range_short = fun(key: String): Setting_Range{
                        return find_min_max(result_list,
                                fun(json: JSONObject):Int{return json.getInt(key)})
                    }
                    val keys = Keys.Companion. Filter_Keys()

                    def_new_price =find_range_short(keys.new_price)
                    def_old_price = find_range_short(keys.old_price)
                    def_discount = find_range_short(keys.discount)
                    def_reviews = find_range_short(keys.reviews)
                    def_rating = find_range_short(keys.rating)
                    val absolute_discount_key = fun(json: JSONObject):Int{
                        return json.getInt(keys.old_price) - json.getInt(keys.new_price)
                    }
                    def_absolute_discount = find_min_max(result_list, absolute_discount_key)


                }

            }

            data class Setting_Range constructor(var min: Int, var max: Int) : Serializable{
                constructor() : this(Int.MAX_VALUE, 0)
            }

        }

        class Sorter {
            private val pagelength = 10

            fun sort(result_array: List<JSONObject>, comparator: Comparator<JSONObject>, reverse_order: Boolean): Array<Array<JSONObject>>{
                var sorted = result_array.sortedWith(comparator)
                if (reverse_order) sorted = sorted.reversed()
                return split_into_pages(sorted)
            }

            private fun split_into_pages(array: List<JSONObject>): Array<Array<JSONObject>>{

                fun slice_jsonarray(array: List<JSONObject>, start: Int, end: Int): Array<JSONObject>{
                    val ret = mutableListOf(JSONObject())
                    ret.clear() // for some reason it's initialised with an empty index at 0
                    var j = start
                    while (j < end){
                        ret.add(array[j])
                        j++
                    }
                    return ret.toTypedArray()
                }
                var i = 0
                val temp = ArrayList<Array<JSONObject>>()
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
    }
}
