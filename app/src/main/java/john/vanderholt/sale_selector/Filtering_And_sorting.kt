package john.vanderholt.sale_selector

import android.util.Log
import org.json.JSONObject
import java.io.Serializable
import java.util.Comparator


class Filtering_And_sorting {
    companion object {
        //todo refactor this. probably split it into a filter and a settings object
        data class Filter_and_settings constructor(var bundles_only_int :Int = 0, // can be 0 for no 1 for yes and 2 for no bundles
                // this bit is very confusing
                                                   var discount : Setting_Range = Setting_Range(),
                                                   var reviews : Setting_Range = Setting_Range(),
                                                   var rating : Setting_Range = Setting_Range(),
                                                   var old_price : Setting_Range = Setting_Range(),
                                                   var new_price : Setting_Range = Setting_Range(),
                                                   var absolute_discount : Setting_Range = Setting_Range(),
                                                   var min_max_ranges: Defaults = Defaults(),
                                                   var sort_order_offset :Boolean = false,
                                                   var last_sort_by_idx :Int = 0,
                                                   var regions :Array<String> = arrayOf(),
                                                   var current_region :String = "") : Serializable {

            constructor(result_list: List<JSONObject>, regions :Array<String>, starting_region: String): this(){
                min_max_ranges = Defaults(result_list)
                set_defaults()
                this.regions = regions
                this.current_region = starting_region
            }

            fun filter_list(items: List<JSONObject>): List<JSONObject>{

                fun filter(item: JSONObject): Boolean{
                    val keys = Keys.Companion.Filter_Keys()

                    fun apply_bundle_filter():Boolean{
                        val is_bundle = item.getBoolean(keys.is_bundle)

                        val setting = this.bundles_only_int
                        if (is_bundle){
                            if (setting != 2){
                                return true
                            }
                        } else {
                            if (setting != 1){
                                return true
                            }
                        }
                        //Log.i("bundle_filter", is_bundle.toString())
                        return false
                    }

                    fun filter_int(key:String, range: Setting_Range): Boolean{
                        val value = item.getInt(key)
                        if (value >= range.min && value <= range.max){
                            return true
                        }
                        //Log.e(key, value.toString() + " min " + range.min.toString() + " max " + range.max.toString())
                        return false
                    }

                    fun filter_absolute_discount():Boolean{
                        val range = this.absolute_discount
                        val value = item.getInt(keys.old_price) - item.getInt(keys.new_price)
                        if (value >= range.min && value <= range.max){
                            return true
                        }
                        //Log.e("def_absolute_discount", value.toString() + " min " + range.min.toString() + " max " + range.max.toString())
                        return false
                    }


                    if (filter_absolute_discount() &&
                            filter_int(keys.discount, this.discount) &&
                            filter_int(keys.old_price, this.old_price) &&
                            filter_int(keys.new_price, this.new_price) &&
                            filter_int(keys.rating, this.rating) &&
                            filter_int(keys.reviews, this.reviews) &&
                            apply_bundle_filter()
                    ){
                        return true
                    }
                    return false
                }
                return items.filter {  filter(it) }
            }

            fun set_defaults(){
                this.bundles_only_int = min_max_ranges.def_bundles_only // can be 0 for no 1 for yes and 2 for no bundles
                this.discount = min_max_ranges.def_discount
                this.reviews = min_max_ranges.def_reviews
                this.rating = min_max_ranges.def_rating
                this.old_price = min_max_ranges.def_old_price
                this.new_price = min_max_ranges.def_new_price
                this.absolute_discount = min_max_ranges.def_absolute_discount
            }




            class Defaults constructor(var def_bundles_only: Int = 0,
                                       private val number_of_quantized_points:Int = 11,
                                       var n_reviews_quant_points: Array<Int> = arrayOf(),
                                       var def_discount: Setting_Range = Setting_Range(),
                                       var def_reviews: Setting_Range = Setting_Range(),
                                       var def_rating: Setting_Range = Setting_Range(),
                                       var def_absolute_discount: Setting_Range = Setting_Range(),
                                       var def_old_price: Setting_Range = Setting_Range(),
                                       var def_new_price: Setting_Range = Setting_Range()
            ) : Serializable {


                constructor(result_list: List<JSONObject>) : this() {

                    fun find_range(key: (JSONObject)->Int): Setting_Range {
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

                    fun find_range_short(key: String): Setting_Range {
                        return find_range(fun(json: JSONObject):Int{
                            return json.getInt(key)
                        })
                    }
                    val keys = Keys.Companion.Filter_Keys()

                    def_new_price =find_range_short(keys.new_price)
                    def_old_price = find_range_short(keys.old_price)
                    def_discount = find_range_short(keys.discount)
                    def_reviews = find_range_short(keys.reviews)
                    def_rating = find_range_short(keys.rating)
                    val absolute_discount_key = fun(json: JSONObject):Int{
                        return json.getInt(keys.old_price) - json.getInt(keys.new_price)
                    }
                    def_absolute_discount = find_range(absolute_discount_key)

                    this.n_reviews_quant_points = get_n_reviews_setting_quantized(result_list)

                }

                private fun get_n_reviews_setting_quantized(result_list: List<JSONObject>): Array<Int>{
                    /*
                    * the algorithm works like this.
                    * you have an array of results
                    * the prices in that range get boiled down to a unique list.
                    * it them checks if unique.length % n_points == 0
                    * if so then it just takes every n_points th point n_points times
                    * otherwise it takes n_points -1 points and uses the last item in the array as the last point
                    * */
                    val keys = Keys.Companion.Filter_Keys()
                    var ns = mutableListOf<Int>()
                    for (result in result_list){
                        ns.add(result.getInt(keys.reviews))
                    }
                    ns = ns.sortedWith(compareBy {it}).toMutableList()
                    for ( i in result_list.size-1 downTo 1){
                        if (ns[i] == ns[i-1]){
                            ns.removeAt(i)
                        }
                    }

                    return if (ns.size <= number_of_quantized_points){
                        ns.toTypedArray()
                    }else{
                        val stepsize = ns.size / number_of_quantized_points
                        val ret = mutableListOf<Int>()
                        for (i in 0 until number_of_quantized_points-1){// gets number_of_quantized_points -1 points
                            ret.add(ns[i*stepsize])
                        }
                        ret.add(ns[ns.lastIndex])
                        ret.toTypedArray()
                    }

                }

            }

            data class Setting_Range constructor(var min: Int, var max: Int) : Serializable{
                constructor() : this(Int.MAX_VALUE, 0)
            }

        }

        class Sorter {
            private val pagelength = 20

            fun sort(result_array: List<JSONObject>, comparator: Comparator<JSONObject>, reverse_order: Boolean): Array<Array<JSONObject>>{
                var sorted = result_array.sortedWith(comparator)
                if (reverse_order) sorted = sorted.reversed()
                return split_into_pages(sorted)
            }

            private fun split_into_pages(array: List<JSONObject>): Array<Array<JSONObject>>{

                fun slice_jsonarray(array: List<JSONObject>, start: Int, end: Int): Array<JSONObject>{
                    val ret = mutableListOf<JSONObject>()
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

                return temp.toTypedArray()
            }
        }
    }
}
