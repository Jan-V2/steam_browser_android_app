package com.example.john.testapp

import android.app.Activity
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.IOException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import java.io.Serializable
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var json: JSONObject
    private lateinit var result_list: List<JSONObject>
    private lateinit var pages: Array<List<JSONObject>>
    private lateinit var filter : Filtering_And_sorting.Filter
    private val FILTER_RQ_CODE = 0
    private val sort_comparators = Filtering_And_sorting.Sort_Comparators()
    private val sorter = Filtering_And_sorting.Sorter()
    private var current_page = 0
    private var np_active = false
    private var sort_from_high_to_low = false
    private var sort_key = sort_comparators.new_price

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        json = load_test_json() as JSONObject

        val json_items = json.getJSONArray("items")
        val mutable_result_list = mutableListOf(JSONObject())
        mutable_result_list.clear()

        for (i in 0 until json_items.length()){
            mutable_result_list .add(i, json_items.getJSONObject(i))
        }
        result_list = mutable_result_list.toList()
        filter = Filtering_And_sorting.Filter(result_list)
        pages = get_new_pages()
        add_listeners()
        init_numberPicker()
        load_page()

    }

    fun get_new_pages(): Array<List<JSONObject>>{
        return sorter.sort(filter.filter_list(result_list), sort_key, sort_from_high_to_low)
    }

    fun init_numberPicker(){
        numberPicker.minValue = 1
        numberPicker.maxValue = pages.size
        numberPicker.wrapSelectorWheel = true
    }


    fun add_listeners(){
        prev_page_button.setOnClickListener( { page_back()})
        middle_button.setOnClickListener({switch_scrollview_np()})
        next_page_button.setOnClickListener({page_forward()})
        fun number_picker_listener(){
            current_page = numberPicker.value -1
            load_page()
            switch_scrollview_np()
        }

        picker_listener.setOnClickListener({
            number_picker_listener()
        })
        filter_switch_button.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            filter.new_price.min = 69
            intent.putExtra("filter", filter as Serializable)
            startActivityForResult(intent, FILTER_RQ_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Handle the logic for the requestCode, resultCode and data returned.
        if (requestCode == FILTER_RQ_CODE && resultCode == Activity.RESULT_OK){
            val filter = data.getSerializableExtra("filter") as Filtering_And_sorting.Filter
            Log.e("returned int", filter.new_price.min.toString())
        }
    }

    fun switch_scrollview_np(){
        if (np_active){
            picker_listener.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
        } else{
            scrollView.visibility = View.GONE
            picker_listener.visibility = View.VISIBLE
        }
        np_active = !np_active
    }

    fun page_back(){
        if (current_page - 1 > -1){
            current_page--
            load_page()
        }
    }

    fun page_forward(){
        if (current_page + 1 < pages.size){
            current_page++
            load_page()
        }
    }

    fun load_test_json(): JSONObject? {
        var json: String? = null
        try {
            //val `is` = assets.open("steamsale_data_small.json")
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

    fun build_link_url(data: JSONObject): String{
        var url  = StringBuilder()
        url.append("http://store.steampowered.com/")
        url = get_app_url_element(url, data, false)
        return url.toString()
    }

    fun get_app_url_element(url: StringBuilder, data: JSONObject, is_image: Boolean): StringBuilder{
        val app_string = "/app"
        val old_bundle_string = "/bundle"
        val new_bundle_string = "/sub"

        if (data.getBoolean("is_bundle")){
            if (data.getBoolean("is_old_bundle")){
                if (!is_image){
                    url.append(old_bundle_string)
                }
            }
            url.append(new_bundle_string)
        } else{
            url.append(app_string)
        }
        if (is_image){
            url.append("s")
        }
        url.append("/")
        url.append(data.getString("appids"))
        return url
    }

    fun get_thumbnail_url(data: JSONObject): String{
        var url  = StringBuilder()
        url.append("http://cdn.edgecast.steamstatic.com/steam")
        url = get_app_url_element(url, data, true)
        url.append("/capsule_184x69.jpg")
        return url.toString()
    }

    fun load_page(){
        val json = pages[current_page]
        if (0 < json.size){
            result_container0.visibility = View.VISIBLE
            init_element_0(json[0])
        }else {
            result_container0.visibility = View.GONE
        }
        if (1 < json.size){
            result_container1.visibility = View.VISIBLE
            init_element_1(json[1])
        }else {
            result_container1.visibility = View.GONE
        }
        if (2 < json.size){
            result_container2.visibility = View.VISIBLE
            init_element_2(json[2])
        }else {
            result_container2.visibility = View.GONE
        }
        if (3 < json.size){
            result_container3.visibility = View.VISIBLE
            init_element_3(json[3])
        }else {
            result_container3.visibility = View.GONE
        }
        if (4 < json.size){
            result_container4.visibility = View.VISIBLE
            init_element_4(json[4])
        }else {
            result_container4.visibility = View.GONE
        }
        if (5 < json.size){
            result_container5.visibility = View.VISIBLE
            init_element_5(json[5])
        }else {
            result_container5.visibility = View.GONE
        }
        if (6 < json.size){
            result_container6.visibility = View.VISIBLE
            init_element_6(json[6])
        }else {
            result_container6.visibility = View.GONE
        }
        if (7 < json.size){
            result_container7.visibility = View.VISIBLE
            init_element_7(json[7])
        }else {
            result_container7.visibility = View.GONE
        }
        if (8 < json.size){
            result_container8.visibility = View.VISIBLE
            init_element_8(json[8])
        }else {
            result_container8.visibility = View.GONE
        }
        if (9 < json.size){
            result_container9.visibility = View.VISIBLE
            init_element_9(json[9])
        }else {
            result_container9.visibility = View.GONE
        }

        middle_button.text = "page " + (current_page +1).toString() + "/" + pages.size.toString()

    }

    fun init_element_0(data: JSONObject){
        val currency_symbol = "€"
        result_container0.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title0.text = data["titles"].toString()
        discount_percentage0.text = " -" + data["discount_percents"].toString() + "% "
        new_price0.text = data["new_price"].toString() + currency_symbol
        old_price0.text = data["old_price"].toString() + currency_symbol
        user_rating_label0.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail0)

        old_price0.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_1(data: JSONObject){
        val currency_symbol = "€"
        result_container1.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title1.text = data["titles"].toString()
        discount_percentage1.text = " -" + data["discount_percents"].toString() + "% "
        new_price1.text = data["new_price"].toString() + currency_symbol
        old_price1.text = data["old_price"].toString() + currency_symbol
        user_rating_label1.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail1)

        old_price1.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_2(data: JSONObject){
        val currency_symbol = "€"
        result_container2.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title2.text = data["titles"].toString()
        discount_percentage2.text = " -" + data["discount_percents"].toString() + "% "
        new_price2.text = data["new_price"].toString() + currency_symbol
        old_price2.text = data["old_price"].toString() + currency_symbol
        user_rating_label2.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail2)

        old_price2.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_3(data: JSONObject){
        val currency_symbol = "€"
        result_container3.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title3.text = data["titles"].toString()
        discount_percentage3.text = " -" + data["discount_percents"].toString() + "% "
        new_price3.text = data["new_price"].toString() + currency_symbol
        old_price3.text = data["old_price"].toString() + currency_symbol
        user_rating_label3.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail3)

        old_price3.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_4(data: JSONObject){
        val currency_symbol = "€"
        result_container4.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title4.text = data["titles"].toString()
        discount_percentage4.text = " -" + data["discount_percents"].toString() + "% "
        new_price4.text = data["new_price"].toString() + currency_symbol
        old_price4.text = data["old_price"].toString() + currency_symbol
        user_rating_label4.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail4)

        old_price4.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_5(data: JSONObject){
        val currency_symbol = "€"
        result_container5.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title5.text = data["titles"].toString()
        discount_percentage5.text = " -" + data["discount_percents"].toString() + "% "
        new_price5.text = data["new_price"].toString() + currency_symbol
        old_price5.text = data["old_price"].toString() + currency_symbol
        user_rating_label5.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail5)

        old_price5.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_6(data: JSONObject){
        val currency_symbol = "€"
        result_container6.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title6.text = data["titles"].toString()
        discount_percentage6.text = " -" + data["discount_percents"].toString() + "% "
        new_price6.text = data["new_price"].toString() + currency_symbol
        old_price6.text = data["old_price"].toString() + currency_symbol
        user_rating_label6.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        val url = get_thumbnail_url(data)
        Log.e("bla", url)
        Picasso.with(applicationContext).load(url).into(result_thumbnail6)

        old_price6.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_7(data: JSONObject){
        val currency_symbol = "€"
        result_container7.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title7.text = data["titles"].toString()
        discount_percentage7.text = " -" + data["discount_percents"].toString() + "% "
        new_price7.text = data["new_price"].toString() + currency_symbol
        old_price7.text = data["old_price"].toString() + currency_symbol
        user_rating_label7.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail7)

        old_price7.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_8(data: JSONObject){
        val currency_symbol = "€"
        result_container8.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title8.text = data["titles"].toString()
        discount_percentage8.text = " -" + data["discount_percents"].toString() + "% "
        new_price8.text = data["new_price"].toString() + currency_symbol
        old_price8.text = data["old_price"].toString() + currency_symbol
        user_rating_label8.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail8)

        old_price8.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun init_element_9(data: JSONObject){
        val currency_symbol = "€"
        result_container9.setOnClickListener( {
            val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })

        result_title9.text = data["titles"].toString()
        discount_percentage9.text = " -" + data["discount_percents"].toString() + "% "
        new_price9.text = data["new_price"].toString() + currency_symbol
        old_price9.text = data["old_price"].toString() + currency_symbol
        user_rating_label9.text = "rating " + data["percent_reviews_positive"].toString() + "%"
        Picasso.with(applicationContext).load(get_thumbnail_url(data)).into(result_thumbnail9)

        old_price9.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    }

}

