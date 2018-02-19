package com.example.john.testapp

import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.content.Intent
import android.net.Uri
import android.view.View
import java.lang.StringBuilder


// todo middle button text so it says x/n
// todo middle button menu where you can select the page
// todo sorting menu
// todo backend


class MainActivity : AppCompatActivity() {

    var current_page = 0
    lateinit var json: JSONObject
    lateinit var pages: Array<JSONArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        json = load_test_json() as JSONObject

        pages = split_into_pages(json.getJSONArray("items"))

        add_button_listeners()
        load_page(pages[current_page])
    }


    fun add_button_listeners(){

        prev_page_button.setOnClickListener( { page_back()})

        next_page_button.setOnClickListener({page_forward()})

        middle_button.setOnClickListener({page_back()})
    }

    fun page_back(){
        if (current_page - 1 > -1){
            current_page--
            load_page(pages[current_page])
        }
    }

    fun page_forward(){
        if (current_page + 1 < pages.size){
            current_page++
            load_page(pages[current_page])
        }
    }


    fun split_into_pages(array: JSONArray): Array<JSONArray>{

        fun slice_jsonarray(array: JSONArray, start: Int, end: Int): JSONArray{
            var ret = JSONArray()
            var j = start
            while (j < end){
                ret.put(array.getJSONObject(j))
                j++
            }
            return ret
        }

        val pagelength = 10
        var i = 0
        var temp = ArrayList<JSONArray>()

        while (i < array.length()){

            if (i + pagelength < array.length()){
                temp.add(slice_jsonarray(array, i, i + pagelength))
            } else {
                temp.add(slice_jsonarray(array, i, array.length()))
            }
            i += pagelength
        }

        val ret = temp.mapToTypedArray { it as JSONArray }

        return ret
    }

    inline fun <T, reified R> List<T>.mapToTypedArray(transform: (T) -> R): Array<R> {
        return when (this) {
            is RandomAccess -> Array(size) { index -> transform(this[index]) }
            else -> with(iterator()) { Array(size) { transform(next()) } }
        }
    }

    fun load_test_json(): JSONObject? {
        var json: String? = null
        try {
            val `is` = assets.open("steamsale_data_small.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, charset("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return JSONObject(json)
    }

    fun build_url(data: JSONObject): String{
        val app_string = "/app/"
        val old_bundle_string = "/bundle/"
        val new_bundle_string = "/sub/"
        val url  = StringBuilder()
        url.append("http://store.steampowered.com/")
        if (data.getBoolean("is_bundle")){
            if (data.getBoolean("is_old_bundle")){
                url.append(old_bundle_string)
            }
            url.append(new_bundle_string)
        } else{
            url.append(app_string)
        }
        url.append(data.getString("appids"))

        return url.toString()
    }


    fun load_page(json: JSONArray){
        if (0 < json.length()){
            init_element_0(json.getJSONObject(0))
        }else {
            result_container0.visibility = View.GONE
        }
        if (1 < json.length()){
            init_element_1(json.getJSONObject(1))
        }else {
            result_container1.visibility = View.GONE
        }
        if (2 < json.length()){
            init_element_2(json.getJSONObject(2))
        }else {
            result_container2.visibility = View.GONE
        }
        if (3 < json.length()){
            init_element_3(json.getJSONObject(3))
        }else {
            result_container3.visibility = View.GONE
        }
        if (4 < json.length()){
            init_element_4(json.getJSONObject(4))
        }else {
            result_container4.visibility = View.GONE
        }
        if (5 < json.length()){
            init_element_5(json.getJSONObject(5))
        }else {
            result_container5.visibility = View.GONE
        }
        if (6 < json.length()){
            init_element_6(json.getJSONObject(6))
        }else {
            result_container6.visibility = View.GONE
        }
        if (7 < json.length()){
            init_element_7(json.getJSONObject(7))
        }else {
            result_container7.visibility = View.GONE
        }
        if (8 < json.length()){
            init_element_8(json.getJSONObject(8))
        }else {
            result_container8.visibility = View.GONE
        }
        if (9 < json.length()){
            init_element_9(json.getJSONObject(9))
        }else {
            result_container9.visibility = View.GONE
        }
        middle_button.text = (current_page +1).toString()

    }

    fun init_element_0(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container0.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title0.text = data["titles"].toString()
        discount_percentage0.text = " -" + data["discount_percents"].toString() + "% "
        new_price0.text = data["new_price"].toString() + currency_symbol
        old_price0.text = data["old_price"].toString() + currency_symbol
        user_rating_label0.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail0)

        old_price0.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_1(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container1.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title1.text = data["titles"].toString()
        discount_percentage1.text = " -" + data["discount_percents"].toString() + "% "
        new_price1.text = data["new_price"].toString() + currency_symbol
        old_price1.text = data["old_price"].toString() + currency_symbol
        user_rating_label1.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail1)

        old_price1.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_2(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container2.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title2.text = data["titles"].toString()
        discount_percentage2.text = " -" + data["discount_percents"].toString() + "% "
        new_price2.text = data["new_price"].toString() + currency_symbol
        old_price2.text = data["old_price"].toString() + currency_symbol
        user_rating_label2.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail2)

        old_price2.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_3(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container3.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title3.text = data["titles"].toString()
        discount_percentage3.text = " -" + data["discount_percents"].toString() + "% "
        new_price3.text = data["new_price"].toString() + currency_symbol
        old_price3.text = data["old_price"].toString() + currency_symbol
        user_rating_label3.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail3)

        old_price3.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_4(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container4.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title4.text = data["titles"].toString()
        discount_percentage4.text = " -" + data["discount_percents"].toString() + "% "
        new_price4.text = data["new_price"].toString() + currency_symbol
        old_price4.text = data["old_price"].toString() + currency_symbol
        user_rating_label4.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail4)

        old_price4.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_5(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container5.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title5.text = data["titles"].toString()
        discount_percentage5.text = " -" + data["discount_percents"].toString() + "% "
        new_price5.text = data["new_price"].toString() + currency_symbol
        old_price5.text = data["old_price"].toString() + currency_symbol
        user_rating_label5.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail5)

        old_price5.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_6(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container6.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title6.text = data["titles"].toString()
        discount_percentage6.text = " -" + data["discount_percents"].toString() + "% "
        new_price6.text = data["new_price"].toString() + currency_symbol
        old_price6.text = data["old_price"].toString() + currency_symbol
        user_rating_label6.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail6)

        old_price6.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_7(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container7.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title7.text = data["titles"].toString()
        discount_percentage7.text = " -" + data["discount_percents"].toString() + "% "
        new_price7.text = data["new_price"].toString() + currency_symbol
        old_price7.text = data["old_price"].toString() + currency_symbol
        user_rating_label7.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail7)

        old_price7.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_8(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container8.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title8.text = data["titles"].toString()
        discount_percentage8.text = " -" + data["discount_percents"].toString() + "% "
        new_price8.text = data["new_price"].toString() + currency_symbol
        old_price8.text = data["old_price"].toString() + currency_symbol
        user_rating_label8.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail8)

        old_price8.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_9(data: JSONObject){
        // todo add elipsis to title if id doesn't fit inside the text_view

        val currency_symbol = "€"
        result_container9.setOnClickListener( {
            val url = build_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title9.text = data["titles"].toString()
        discount_percentage9.text = " -" + data["discount_percents"].toString() + "% "
        new_price9.text = data["new_price"].toString() + currency_symbol
        old_price9.text = data["old_price"].toString() + currency_symbol
        user_rating_label9.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(data["thumbnail"].toString()).into(result_thumbnail9)

        old_price9.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

}

