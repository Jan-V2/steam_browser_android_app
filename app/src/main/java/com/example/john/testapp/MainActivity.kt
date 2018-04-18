package com.example.john.testapp
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.content.Intent
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.lang.StringBuilder
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.io.*
import java.net.URL

typealias Consumer<T> = (T) -> Unit

class MainActivity : AppCompatActivity() {


    private lateinit var json: JSONObject
    private lateinit var result_list: List<JSONObject>
    private lateinit var pages: Array<Array<JSONObject>>
    private lateinit var filter : Filtering_And_sorting.Companion.Filter
    private lateinit var result_containers : Array<Result_Container>
    private lateinit var filter_defaults: Filtering_And_sorting.Companion.Filter.Defaults
    private val json_url = "https://s3.eu-central-1.amazonaws.com/steamfilterapp/EU.json"
    private val currency_symbol = "€"
    private val FILTER_RQ_CODE = 0
    private val sort_comparators = Keys.Companion.Sort_Comparators()
    private val sorter = Filtering_And_sorting.Companion.Sorter()
    private var current_page = 0
    private var np_active = false
    private var sort_from_high_to_low = true
    private var sort_key = sort_comparators.number_user_reviews

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //json = load_json() as JSONObject //load_json(download_json()) as JSONObject

        startup.get_json_and_start(this)
        add_nav_and_picker_listeners()
    }

    fun start_activity(){
        val json_items = json.getJSONArray("items")
        val mutable_result_list = mutableListOf(JSONObject())
        mutable_result_list.clear()

        for (i in 0 until json_items.length()){
            mutable_result_list .add(i, json_items.getJSONObject(i))
        }
        result_list = mutable_result_list.toList()
        filter = Filtering_And_sorting.Companion.Filter(result_list)
        filter_defaults = filter.defaults
        pages = get_new_pages()
        result_containers = get_result_container_array()
        init_numberPicker()
        load_page()
        linear_cont.visibility = View.VISIBLE
        //val listener = OnSwipeTouchListener(applicationContext)

    }

    fun get_new_pages(): Array<Array<JSONObject>>{
        return sorter.sort(filter.filter_list(result_list), sort_key, sort_from_high_to_low)
    }

    fun init_numberPicker(){
        numberPicker.minValue = 1
        numberPicker.maxValue = pages.size
        numberPicker.wrapSelectorWheel = true
    }
/*
    fun get_swipe_listener(onclick: Runnable? = null): OnSwipeTouchListener{
        val left = Runnable { this.page_forward() }
        val right = Runnable { this.page_back() }
        return OnSwipeTouchListener(applicationContext, left, right, onclick)
    }*/

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
            Log.e("start", "start")
            val intent = Intent(this, FilterActivity::class.java)
            intent.putExtra("filter", filter as Serializable)
            intent.putExtra("currency_symbol", currency_symbol)

            val sort_by_setting = Keys.Companion.Sort_By_Setting()
            intent.putExtra("sort_by_setting", sort_by_setting.strings)
            val reverse_sort_order_setting = Keys.Companion.Sort_Order_Setting()
            intent.putExtra("reverse_sort_order_setting", reverse_sort_order_setting.strings)
            val bundles_only_setting = Keys.Companion.Bundles_Only_Setting()
            intent.putExtra("bundles_only_setting", bundles_only_setting.strings)

            startActivityForResult(intent, FILTER_RQ_CODE)
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
        if (current_page == 0){
            current_page = pages.size-1
        }else{
            current_page--
        }
        load_page()
    }

    fun page_forward(){
        if (current_page < pages.size -1){
            current_page++
        }else{
            current_page = 0
        }
        load_page()
    }

    fun load_page(){

        fun get_result_listener(url: String): View.OnClickListener{
            return View.OnClickListener  {
                //val url = build_link_url(data)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }

        for (i in 0 until result_containers.size) {
            if (pages.isNotEmpty()){
                val page = pages[current_page]
                if (i < page.size) {
                    val data = page[i]
                    result_containers[i].container.visibility = View.VISIBLE

                    result_containers[i].load_data(data, get_result_listener(url_builder.build_link_url(data)),
                            url_builder.get_thumbnail_url(data), applicationContext)
                } else {
                    result_containers[i].container.visibility = View.GONE
                }
            } else {
                result_containers[i].container.visibility = View.GONE
            }
        }
        middle_button.text = "page " + (current_page +1).toString() + "/" + pages.size.toString()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, return_intent: Intent) {
        // Handle the logic for the requestCode, resultCode and return_intent returned.
        if (requestCode == FILTER_RQ_CODE && resultCode == Activity.RESULT_OK){
            val keys = Keys.Companion.Serialisable_Keys()

            val extras = return_intent.extras

            filter = return_intent.getSerializableExtra(keys.filter) as Filtering_And_sorting.Companion.Filter
            filter.defaults = filter_defaults// This is a hack to fix a bug with serialisation see todos (Filter defaults hack)

            sort_key = Keys.Companion.Sort_By_Setting().get_comparator(return_intent.getStringExtra(keys.sort_by))!!
            sort_from_high_to_low = Keys.Companion.Sort_Order_Setting().get_setting(return_intent.getStringExtra(keys.sort_order))!!

            pages = get_new_pages()
            current_page = 0
            load_page()
            init_numberPicker()
        }
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
            val keys = Keys.Companion.Filter_Keys()
            container.setOnClickListener(listener)
            title_.text = data[keys.title].toString()
            discount.text = " -" + data[keys.discount].toString() + "% "
            new_price.text = data[keys.new_price].toString() + currency_symbol
            old_price.text = data[keys.old_price].toString() + currency_symbol
            rating.text = "Rating " + data[keys.rating].toString() + "%"
            //val url = get_thumbnail_url(data)
            Picasso.with(applicationContext).load(thumbnail_url).into(thumbnail)

            old_price.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    private class startup{
        companion object {
            val skip_refresh = true

            private val force_cache_refresh = false;
            private fun cache_log(logline:String){Log.i("cache", logline)}

            private fun get_json_file(context: Context, url: String ): File =
                    Uri.parse(url)?.lastPathSegment?.let { filename ->
                        File(context.cacheDir, filename)
                        // i don't quite understand why this method needs the non null assertion
                    }!!

            private fun load_json(file: File): JSONObject{
                var json: String? = null
                try {
                    //val `is` = assets.open("steamsale_data_small.json")
                    //val `is`= assets.open("steamsale_data_large.json")
                    val `is` = FileInputStream(file)
                    val size = `is`.available()
                    val buffer = ByteArray(size)
                    `is`.read(buffer)
                    `is`.close()
                    json = String(buffer, charset("UTF-8"))
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

                return JSONObject(json)
            }

            private fun cache_refresh_needed(file :File):Boolean{
                if (!skip_refresh){
                    cache_log("not refreshing cache. turned of right now.")
                    return false
                }


                val max_cache_age: Long = (3600 * 4) * 1000 // .LastModified returns the unix time im ms
                if (force_cache_refresh){
                    return true
                }else{
                    if (file.exists()){
                        if (file.lastModified() < System.currentTimeMillis()- max_cache_age){
                            cache_log("jsonfile older than two hours refreshing cache.")
                            return true
                        }else{
                            cache_log("jsonfile less than two hours old. loading file from cache.")
                            return false
                        }
                    }else{
                        cache_log("jsonfile not found in cache. downloading json.")
                        return true
                    }
                }
            }


            fun get_json_and_start(_this:MainActivity){
                val url = _this.json_url
                val media_file = get_json_file(_this.applicationContext, url)

                fun load_and_start(){
                    _this.json = load_json(media_file)
                    _this.start_activity()
                }

                if (cache_refresh_needed(media_file)){
                    doAsyncResult {
                        fun download_json(){
                            val cn = URL(_this.json_url).openConnection()
                            cn.connect()
                            val stream = cn.getInputStream()
                            val out = FileOutputStream(media_file);
                            val buf = ByteArray(16384)
                            while (true) {
                                val numread = stream.read(buf)
                                if (numread <= 0) break
                                out.write(buf, 0, numread)
                            }
                        }
                        download_json()
                        uiThread {
                            load_and_start()
                        }
                    }
                }else{
                    load_and_start()
                }
            }
        }
    }

    private class url_builder{
        companion object {

            val keys = Keys.Companion.Filter_Keys()

            fun build_link_url(data: JSONObject): String{
                var url  = Url_Builder()
                url.add("http://store.steampowered.com")
                url = get_app_url_element(url, data, false)
                return url.toString()
            }

            private fun get_app_url_element(url: Url_Builder, data: JSONObject, is_image: Boolean): Url_Builder{
                val app_string = "app"
                val old_bundle_string = "sub"
                val new_bundle_string = "bundle"

                if (data.getBoolean(keys.is_bundle)){
                    if (data.getBoolean(keys.is_old_bundle)){
                        url.add(old_bundle_string)
                    }else{
                        url.add(new_bundle_string)
                    }

                } else{
                    url.add(app_string)
                }
                if (is_image){
                    url.append_to_last_item("s")
                }
                url.add(data.getString("appids"))

                return url
            }

            fun get_thumbnail_url(data: JSONObject): String{
                var url  = Url_Builder()
                if (data.getBoolean(keys.is_bundle)){
                    url.add("https://steamcdn-a.akamaihd.net/steam")
                }else{
                    url.add("http://cdn.edgecast.steamstatic.com/steam")

                }
                url = get_app_url_element(url, data, true)
                val new_cnd_id = data.getString(keys.new_cnd_imgid)
                if (new_cnd_id != ""){
                    url.add(new_cnd_id)
                }
                url.add("capsule_184x69.jpg")
                return url.toString()
            }
        }
    }

}


