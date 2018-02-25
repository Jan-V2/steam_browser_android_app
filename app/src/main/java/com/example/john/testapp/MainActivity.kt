package com.example.john.testapp

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.IOException
import android.content.Intent
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.io.Serializable
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var json: JSONObject
    private lateinit var result_list: List<JSONObject>
    private lateinit var pages: Array<List<JSONObject>>
    private lateinit var filter : Filtering_And_sorting.Filter
    private val currency_symbol = "€"
    private val FILTER_RQ_CODE = 0
    private val sort_comparators = Filtering_And_sorting.Sort_Comparators()
    private val sorter = Filtering_And_sorting.Sorter()
    private var current_page = 0
    private var np_active = false
    private var sort_from_high_to_low = true
    private var sort_key = sort_comparators.number_user_reviews
    private lateinit var result_containers : Array<Result_Container>
    private lateinit var filter_defaults: Filtering_And_sorting.Filter.Defaults

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
        filter_defaults = filter.defaults
        pages = get_new_pages()
        result_containers = get_result_container_array()
        add_nav_and_picker_listeners()
        init_numberPicker()
        load_page()

    }

    fun get_result_container_array(): Array<Result_Container>{
        return arrayOf(
                Result_Container(result_container0, result_title0, discount_percentage0, new_price0,
                        old_price0, user_rating_label0, result_thumbnail0, currency_symbol),
                Result_Container(result_container1, result_title1, discount_percentage1, new_price1,
                        old_price1, user_rating_label1, result_thumbnail1, currency_symbol),
                Result_Container(result_container2, result_title2, discount_percentage2, new_price2,
                        old_price2, user_rating_label2, result_thumbnail2, currency_symbol),
                Result_Container(result_container3, result_title3, discount_percentage3, new_price3,
                        old_price3, user_rating_label3, result_thumbnail3, currency_symbol),
                Result_Container(result_container4, result_title4, discount_percentage4, new_price4,
                        old_price4, user_rating_label4, result_thumbnail4, currency_symbol),
                Result_Container(result_container5, result_title5, discount_percentage5, new_price5,
                        old_price5, user_rating_label5, result_thumbnail5, currency_symbol),
                Result_Container(result_container6, result_title6, discount_percentage6, new_price6,
                        old_price6, user_rating_label6, result_thumbnail6, currency_symbol),
                Result_Container(result_container7, result_title7, discount_percentage7, new_price7,
                        old_price7, user_rating_label7, result_thumbnail7, currency_symbol),
                Result_Container(result_container8, result_title8, discount_percentage8, new_price8,
                        old_price8, user_rating_label8, result_thumbnail8, currency_symbol),
                Result_Container(result_container9, result_title9, discount_percentage9, new_price9,
                        old_price9, user_rating_label9, result_thumbnail9, currency_symbol)
                )
    }

    fun get_new_pages(): Array<List<JSONObject>>{
        return sorter.sort(filter.filter_list(result_list), sort_key, sort_from_high_to_low)
    }

    fun init_numberPicker(){
        numberPicker.minValue = 1
        numberPicker.maxValue = pages.size
        numberPicker.wrapSelectorWheel = true
    }


    fun add_nav_and_picker_listeners(){
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
            intent.putExtra("filter", filter as Serializable)
            intent.putExtra("currency_symbol", currency_symbol)
            startActivityForResult(intent, FILTER_RQ_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Handle the logic for the requestCode, resultCode and data returned.
        if (requestCode == FILTER_RQ_CODE && resultCode == Activity.RESULT_OK){
            filter = data.getSerializableExtra("filter") as Filtering_And_sorting.Filter
            filter.defaults = filter_defaults// This is a hack see todos (Filter defaults hack)
            pages = get_new_pages()
            current_page = 0
            load_page()
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

    fun get_result_listener(url: String): View.OnClickListener{
        return View.OnClickListener  {
            //val url = build_link_url(data)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    fun load_page(){

        for (i in 0 until result_containers.size) {
            if (pages.isNotEmpty()){
                val page = pages[current_page]
                        if (i < page.size) {
                    val data = page[i]
                    result_containers[i].container.visibility = View.VISIBLE

                    result_containers[i].load_data(data, get_result_listener(build_link_url(data)),
                            get_thumbnail_url(data), applicationContext)
                } else {
                    result_containers[i].container.visibility = View.GONE
                }
            } else {
                result_containers[i].container.visibility = View.GONE
            }
        }
        middle_button.text = "page " + (current_page +1).toString() + "/" + pages.size.toString()
    }

    class Result_Container constructor(
                                val container: ConstraintLayout,
                                val title_: TextView,
                                val discount: TextView,
                                val new_price: TextView,
                                val old_price: TextView,
                                val rating: TextView,
                                val thumbnail: ImageView,
                                val currency_symbol: String){

        fun load_data(data: JSONObject, listener: View.OnClickListener,
                      thumbnail_url: String, applicationContext: Context){
            val keys = Filtering_And_sorting.Keys()
            container.setOnClickListener(listener)
            title_.text = data[keys.title].toString()
            discount.text = " -" + data[keys.discount].toString() + "% "
            new_price.text = data[keys.new_price].toString() + currency_symbol
            old_price.text = data[keys.old_price].toString() + currency_symbol
            rating.text = "def_rating " + data[keys.rating].toString() + "%"
            //val url = get_thumbnail_url(data)
            Picasso.with(applicationContext).load(thumbnail_url).into(thumbnail)

            old_price.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

}

