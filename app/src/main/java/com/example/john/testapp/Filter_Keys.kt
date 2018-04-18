package com.example.john.testapp

import org.json.JSONObject
import java.io.Serializable
import java.util.Comparator


class Keys {
    companion object {
        class Filter_Keys{
            val new_price = "new_price"
            val old_price = "old_price"
            val reviews = "n_user_reviews"
            val rating = "percent_reviews_positive"
            val discount = "discount_percents"
            val title = "titles"
            val is_bundle = "is_bundle"
            val is_old_bundle = "is_old_bundle"
            val new_cnd_imgid = "new_cdn_id"
        }

        class Sort_Comparators {
            private val keys = Filter_Keys()
            val new_price = from_int_key(keys.new_price)
            val old_price = from_int_key(keys.old_price)
            val discount = from_int_key(keys.discount)
            val number_user_reviews = from_int_key(keys.reviews)
            val rating = from_int_key(keys.rating)
            val absolute_discount = compareBy<JSONObject> {it.getInt(keys.old_price) - it.getInt(keys.new_price)}
            val alphbetically = compareBy<JSONObject> {it.getString(keys.title)}

            private fun from_int_key(sort_key: String): Comparator<JSONObject> {
                return compareBy<JSONObject> {it.getInt(sort_key)}
            }
        }

        class Sort_By_Setting {
            val strings = arrayOf("Original price", "Discounted price", "Discount in percent", "Discount in euros",
                    "Rating", "Number of reviews", "Alphabetically")
            private val string_comparator_hashmap: HashMap<String, Comparator<JSONObject>>

            init {
                val comparators = Sort_Comparators()
                string_comparator_hashmap = hashMapOf(
                        strings[0] to comparators.old_price,
                        strings[1] to comparators.new_price ,
                        strings[2] to comparators.discount,
                        strings[3] to comparators.absolute_discount ,
                        strings[4] to comparators.rating,
                        strings[5] to comparators.number_user_reviews ,
                        strings[6] to comparators.alphbetically
                )
            }

/*        fun get_index(setting :Comparator<JSONObject>):Array<String>{

        }*/

            fun get_comparator(key:String): Comparator<JSONObject>? {
                return string_comparator_hashmap[key]
            }
        }

        class Sort_Order_Setting {
            val strings = arrayOf("High to low", "Low to high")
            private val reverse_sort_hashmap: HashMap<String, Boolean>

            init {
                reverse_sort_hashmap = hashMapOf(
                        strings[0] to true,
                        strings[1] to false
                )
            }

            fun get_setting(key:String): Boolean? {
                return reverse_sort_hashmap[key]
            }
        }

        class Bundles_Only_Setting {
            val strings = arrayOf("Bundles and non bundles","Bundles only", "No bundles")

            fun get_setting(key:String): Int {
                return strings.indexOf(key)
            }
/*
        fun get_strings(setting: Int): Array<String> {

            var ret
            for (i in 0 until setting) {

            }
        }*/
        }

        class Serialisable_Keys{
            val filter = "filter"
            val sort_by = "sort_by"
            val sort_order = "sort_order"
        }

        class Serialsable_Settings(val sort_by: String, val sort_order: String): Serializable
    }

}