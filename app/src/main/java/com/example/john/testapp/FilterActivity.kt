package com.example.john.testapp

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_filter.*
import java.io.Serializable

class FilterActivity : AppCompatActivity() {

    private lateinit var filter : Filtering_And_sorting.Filter
    private var sort_comparators = Filtering_And_sorting.Sort_Comparators()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        filter =  intent.getSerializableExtra("filter") as Filtering_And_sorting.Filter

        range_slider.stepsThumbsApart = 0
        range_slider.setOnThumbValueChangeListener { multiSlider, thumb, thumbIndex, value ->
            run {
                if (thumbIndex == 0){
                    lower_value.text = thumb.value.toString() + "%"
                } else {
                    upper_value.text = thumb.value.toString() + "%"
                }


            }
        }

    }

    fun return_to_main_activity(){
        val ret_intent = Intent(this, MainActivity::class.java)
        ret_intent.putExtra("filter", filter as Serializable)
        setResult(Activity.RESULT_OK, ret_intent)
        finish()
    }
}
