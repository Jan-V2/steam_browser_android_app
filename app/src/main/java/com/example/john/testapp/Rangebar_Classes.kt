package com.example.john.testapp

import android.widget.TextView
import io.apptik.widget.MultiSlider
import com.example.john.testapp.Filtering_And_sorting.Companion.Filter

//todo try put the set_thumb method in the abstract class

abstract class Abstract_Rangebar(private var rangebar: MultiSlider){

    fun init_thumbs(current_value: Filter.Setting_Range){
        rangebar.getThumb(0).value = current_value.min
        rangebar.getThumb(1).value = current_value.max

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

    abstract fun set_thumb_text(thumb: MultiSlider.Thumb, is_lower_text: Boolean)
}

class Smooth_Rangebar constructor(val rangebar: MultiSlider, val upper_value: TextView,
                                  val rangebar_title: TextView, val lower_value: TextView,
                                  val name: String, min_max: Filter.Setting_Range,
                                  val current_value: Filter.Setting_Range,
                                  val value_name: String = "", val value_name_prepend: Boolean = true
                                  ):Abstract_Rangebar(rangebar) {
    init{
        rangebar.stepsThumbsApart = 0
        rangebar.min = min_max.min
        rangebar.max = min_max.max
        rangebar_title.text = name
        init_thumbs(current_value)
    }
    override fun set_thumb_text(thumb: MultiSlider.Thumb, is_lower_text: Boolean){
        val val_name: String
        if (value_name_prepend){
            val_name = value_name + thumb.value.toString()
        }else{
            val_name = thumb.value.toString() + value_name
        }
        if (is_lower_text){
            lower_value.text =  val_name + ">"
            current_value.min = thumb.value
        }else{
            upper_value.text = "<" + val_name
            current_value.max = thumb.value
        }

    }
}


class Quantized_Rangebar constructor(rangebar: MultiSlider, val upper_value: TextView,
                                     val rangebar_title: TextView, val lower_value: TextView,
                                     val name: String, val points: Array<Int>,
                                     val current_value: Filter.Setting_Range,
                                     val value_name: String = "", val value_name_prepend: Boolean = true
                                     ) :Abstract_Rangebar(rangebar) {
    init{

        rangebar.stepsThumbsApart = 0
        rangebar.min = 0
        rangebar.max = points.size -1
        rangebar.step = 1
        rangebar_title.text = name
        fun get_current_value_as_thumbpos():Filter.Setting_Range{
            // you start min and max both at 0 and try to shift them as high as you can
            var min = 0
            var max = 0
            for (i in 0 until points.size){
                if (current_value.min >= points[i]){
                    min = i
                }
                if (current_value.max >= points[i] ){
                    max = i
                }
            }
            return Filter.Setting_Range(min, max)
        }
        init_thumbs(get_current_value_as_thumbpos())
    }

    override fun set_thumb_text(thumb: MultiSlider.Thumb, is_lower_text: Boolean){
        val val_name: String
        val new_value = points[thumb.value]
        if (value_name_prepend){
            val_name = value_name + new_value.toString()
        }else{
            val_name = new_value.toString() + value_name
        }

        if (is_lower_text){
            lower_value.text =  val_name + ">"
            current_value.min = new_value
        }else{
            upper_value.text = "<" + val_name
            current_value.max = new_value
        }

    }
}