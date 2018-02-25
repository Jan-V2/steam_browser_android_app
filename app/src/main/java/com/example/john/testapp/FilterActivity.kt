package com.example.john.testapp

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import io.apptik.widget.MultiSlider
import kotlinx.android.synthetic.main.activity_filter.*
import java.io.Serializable

class FilterActivity : AppCompatActivity() {

    private lateinit var filter : Filtering_And_sorting.Filter
    private lateinit var rangebar_collection: Rangebar_Collection
    private lateinit var currency_symbol: String
    private var sort_comparators = Filtering_And_sorting.Sort_Comparators()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        filter =  intent.getSerializableExtra("filter") as Filtering_And_sorting.Filter
        currency_symbol = intent.getStringExtra("currency_symbol")
        rangebar_collection = get_rangebar_collection()

        test_button.setOnClickListener {
            openOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        val inflater = menuInflater
        inflater.inflate(R.menu.test_menu, menu)
        return true
    }

    fun get_rangebar_collection(): Rangebar_Collection{
        return Rangebar_Collection(
                Rangebar(rangebar0, upper_value0, rangebar_name0, lower_value0,
                            "Discount", filter.defaults.def_discount, filter.discount, "%", false),
                Rangebar(rangebar1, upper_value1, rangebar_name1, lower_value1,
                            "Discounted price", filter.defaults.def_new_price, filter.new_price, currency_symbol),
                Rangebar(rangebar2, upper_value2, rangebar_name2, lower_value2,
                            "Total def_discount", filter.defaults.def_absolute_discount, filter.absolute_discount, currency_symbol),
                Rangebar(rangebar3, upper_value3, rangebar_name3, lower_value3,
                            "Original price", filter.defaults.def_old_price, filter.old_price, currency_symbol),
                Rangebar(rangebar4, upper_value4, rangebar_name4, lower_value4,
                            "Number of def_reviews", filter.defaults.def_reviews, filter.reviews),
                Rangebar(rangebar5, upper_value5, rangebar_name5, lower_value5,
                            "Rating", filter.defaults.def_rating, filter.rating, "%", false)
                )
    }

    override fun onBackPressed() {
        return_to_main_activity()
    }

    fun return_to_main_activity() {
        val ret_intent = Intent(this, MainActivity::class.java)
        filter.discount = rangebar_collection.discount.current_setting
        filter.new_price = rangebar_collection.new_price.current_setting
        filter.old_price = rangebar_collection.old_price.current_setting
        filter.rating = rangebar_collection.rating.current_setting
        filter.reviews = rangebar_collection.reviews.current_setting
        filter.absolute_discount = rangebar_collection.absolute_discount.current_setting
        ret_intent.putExtra("filter", filter as Serializable)
        setResult(Activity.RESULT_OK, ret_intent)
        finish()
    }

    class Rangebar_Collection constructor(
            val discount: Rangebar,
            val new_price: Rangebar,
            val absolute_discount: Rangebar,
            val old_price: Rangebar,
            val reviews: Rangebar,
            val rating: Rangebar)

    class Rangebar constructor(val rangebar: MultiSlider, val upper_value: TextView,
                               val rangebar_title: TextView, val lower_value: TextView,
                               val name: String,
                               min_max: Filtering_And_sorting.Filter.Setting_Range,
                               val current_setting: Filtering_And_sorting.Filter.Setting_Range,
                               val value_name: String = "", val value_name_prepend: Boolean = true) {
        init{
            rangebar.stepsThumbsApart = 0
            rangebar.min = min_max.min
            rangebar.max = min_max.max
            rangebar.getThumb(0).value = current_setting.min
            rangebar.getThumb(1).value = current_setting.max
            rangebar_title.text = name
            set_thumb_text(rangebar.getThumb(0), true)
            set_thumb_text(rangebar.getThumb(1), false)

            rangebar.setOnThumbValueChangeListener { multiSlider, thumb, thumbIndex, value ->
                run {
                    var is_lower_value = true
                    if (thumbIndex == 1) is_lower_value = !is_lower_value
                    set_thumb_text(thumb, is_lower_value)
                }
            }
        }
        private fun set_thumb_text(thumb: MultiSlider.Thumb, is_lower_text: Boolean){
            val val_name: String
            if (value_name_prepend){
                val_name = value_name + thumb.value.toString()
            }else{
                val_name = thumb.value.toString() + value_name
            }
            if (is_lower_text){
                lower_value.text =  val_name + ">"
                current_setting.min = thumb.value
            }else{
                upper_value.text = "<" + val_name
                current_setting.max = thumb.value
            }

        }
    }

}
