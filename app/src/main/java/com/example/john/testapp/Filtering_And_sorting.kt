package com.example.john.testapp

import android.util.Log
import org.json.JSONObject
import java.io.Serializable
import java.util.Comparator


abstract class Filtering_And_sorting {

    class Keys{
        val new_price = "new_price"
        val old_price = "old_price"
        val reviews = "n_user_reviews"
        val rating = "percent_reviews_positive"
        val discount = "discount_percents"

    }

    data class Filter constructor(var bundles_only :Int = 0, // can be 0 for no 1 for yes and 2 for no bundles
                                             // this bit is very confusing
                                  var discount : Setting_Range = Setting_Range(),
                                  var reviews : Setting_Range = Setting_Range(),
                                  var rating : Setting_Range = Setting_Range(),
                                  var old_price : Setting_Range = Setting_Range(),
                                  var new_price : Setting_Range = Setting_Range(),
                                  var absolute_discount : Setting_Range = Setting_Range(),
                                  private var defaults: Defaults = Defaults()) : Serializable {

        constructor(result_list: List<JSONObject>): this(){
            defaults = Defaults(result_list)
            set_defaults()
        }

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
                    Log.e(key, value.toString() + " min " + range.min.toString() + " max " + range.max.toString())
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
                val keys = Keys()

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
            this.bundles_only = defaults.bundles_only // can be 0 for no 1 for yes and 2 for no bundles
            this.discount = defaults.discount
            this.reviews = defaults.reviews
            this.rating = defaults.rating
            this.old_price = defaults.old_price
            this.new_price = defaults.new_price
            this.absolute_discount = defaults.absolute_discount
        }


        class Defaults constructor(var bundles_only: Int = 0,
                                   var discount: Setting_Range = Setting_Range(),
                                   var reviews: Setting_Range = Setting_Range(),
                                   var rating: Setting_Range = Setting_Range(),
                                   var absolute_discount: Setting_Range = Setting_Range(),
                                   var old_price: Setting_Range = Setting_Range(),
                                   var new_price: Setting_Range = Setting_Range()
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
                val keys = Keys()

                new_price =find_range_short(keys.new_price)
                old_price = find_range_short(keys.old_price)
                discount = find_range_short(keys.discount)
                reviews = find_range_short(keys.reviews)
                rating = find_range_short(keys.rating)
                val absolute_discount_key = fun(json: JSONObject):Int{
                    return json.getInt(keys.old_price) - json.getInt(keys.new_price)
                }
                absolute_discount = find_min_max(result_list, absolute_discount_key)


            }

        }

        data class Setting_Range constructor(var min: Int, var max: Int) : Serializable{
            constructor() : this(Int.MAX_VALUE, 0)
        }

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
        private val keys = Keys()
        val new_price = from_int_key(keys.new_price)
        val old_price = from_int_key(keys.old_price)
        val discount = from_int_key(keys.discount)
        val number_user_reviews = from_int_key(keys.reviews)
        val percent_reviews_positive = from_int_key(keys.rating)
        val absolute_discount = compareBy<JSONObject> {it.getInt(keys.old_price) - it.getInt(keys.new_price)}

        private fun from_int_key(sort_key: String): Comparator<JSONObject> {
            return compareBy<JSONObject> {it.getInt(sort_key)}
        }
    }
}
