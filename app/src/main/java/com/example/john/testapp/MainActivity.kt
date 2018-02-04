package com.example.john.testapp

import android.content.Context
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.result_container.view.*
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*


// todo double tapping a result opens op a link to the store page

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //strike_through_text_view(result_container.layout_container.old_price)
        //strike_through_text_view(result_container2.layout_container.old_price)

        val viewGroup = main_cont.linear_cont as ViewGroup


        val popup = View.inflate(viewGroup.context, R.layout.result_container, viewGroup)
        val popup2 = View.inflate(viewGroup.context, R.layout.result_container, viewGroup)

        val results = test_results()
        init_ui_result(popup, results[0])
        init_ui_result(popup2, results[1])



    }

    fun init_ui_result(result_view: View, data: HashMap<String, Any>){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val layout_cont = result_view.result_container.layout_container
        val currency_symbol = "€"

        layout_cont.result_title.text = data["titles"].toString()
        layout_cont.discount_percentage.text = " -" + data["discount_percents"].toString() + "% "
        layout_cont.new_price.text = data["new_price"].toString() + currency_symbol
        layout_cont.old_price.text = data["old_price"].toString() + currency_symbol
        layout_cont.user_rating_label.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(layout_cont.result_thumbnail)

        strike_through_text_view(layout_cont.old_price)
    }

    fun strike_through_text_view (text_view: TextView) {
        text_view.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun test_results (): Array<HashMap<String, Any>> {
        val test1 = HashMap<String, Any>()
        test1.put("is_old_bundle", false)
        test1.put("is_bundle", false)
        test1.put("percent_reviews_positive", 96)
        test1.put("new_price", 2.54)
        test1.put("old_price", 16.99)
        test1.put("appids", "35720")
        test1.put("n_user_reviews", 10297)
        test1.put("thumbnail", "http://cdn.edgecast.steamstatic.com/steam/apps/35720/capsule_sm_120.jpg?t=1477041084")
        test1.put("titles", "Trine 2: Complete Story")
        test1.put("discount_percents", 85)

        val test2 = HashMap<String, Any>()
        test2.put("is_old_bundle", false)
        test2.put("is_bundle", false)
        test2.put("percent_reviews_positive", 93)
        test2.put("new_price", 3.99)
        test2.put("old_price", 19.99)
        test2.put("appids", "47810")
        test2.put("n_user_reviews", 7837)
        test2.put("thumbnail", "http://cdn.edgecast.steamstatic.com/steam/apps/47810/capsule_sm_120.jpg?t=1447353666")
        test2.put("titles", "Dragon Age: Origins - Ultimate Edition")
        test2.put("discount_percents", 80)

        val ret = arrayOf(test1, test2)
        return ret
    }
}



