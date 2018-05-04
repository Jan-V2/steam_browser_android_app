package john.vanderholt.sale_selector
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.io.*
import java.net.URL
import android.support.design.widget.Snackbar
import android.text.Html
import john.vanderholt.john.sale_selector.R
import java.net.UnknownHostException


typealias Consumer<T> = (T) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var json: JSONObject
    private lateinit var result_list: List<JSONObject>
    private lateinit var pages: Array<Array<JSONObject>>
    private lateinit var filter_and_settings : Filtering_And_sorting.Companion.Filter_and_settings
    private lateinit var result_containers : Array<Result_Container>
    private lateinit var settings_defaults: Filtering_And_sorting.Companion.Filter_and_settings.Defaults
    private lateinit var currency_symbol :String
    private val FILTER_RQ_CODE = 0
    private val sort_comparators = Keys.Companion.Sort_Comparators()
    private val sorter = Filtering_And_sorting.Companion.Sorter()
    private val zipped_json_url = "https://s3.eu-central-1.amazonaws.com/steamfilterapp/data.zip"
    private var current_region = "EU"
    private var current_page = 0
    private var np_active = false
    private var sort_from_high_to_low = true
    private var sort_key = sort_comparators.discount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_main)

        //json = load_json() as JSONObject //load_json(download_json()) as JSONObject
        result_containers = get_result_container_array()
        startup.get_json_and_start(this)
        add_nav_and_picker_listeners()

    }

    fun load_json_from_disk(){
        fun load_json_file(_dir: File, filename :String): JSONObject{
            val file = File(_dir, filename)
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
        val dir = applicationContext.cacheDir
        json = load_json_file(dir, Keys.region_to_filename[current_region]!!)
        currency_symbol = Keys.currency_map[current_region]!!

        val json_items = json.getJSONArray("items")
        val mutable_result_list = mutableListOf<JSONObject>()
        for (i in 0 until json_items.length()){
            mutable_result_list .add(i, json_items.getJSONObject(i))
        }
        result_list = mutable_result_list.toList()
        filter_and_settings = Filtering_And_sorting.Companion.Filter_and_settings(result_list,
                Keys.region_to_filename.keys.toTypedArray(), current_region)
        settings_defaults = filter_and_settings.min_max_ranges
    }

    private fun filter_results_and_display_ui(){
        pages = get_new_sorted_pages()
        current_page = 0
        init_numberPicker()
        load_page()
        linear_cont.visibility = View.VISIBLE
    }

    private fun get_new_sorted_pages(): Array<Array<JSONObject>>{
        return sorter.sort(filter_and_settings.filter_list(result_list), sort_key, sort_from_high_to_low)
    }

    private fun init_numberPicker(){
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

    private fun add_nav_and_picker_listeners(){
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
            //Log.e("start", "start")
            val intent = Intent(this, FilterActivity::class.java)
            intent.putExtra("filter_and_settings", filter_and_settings as Serializable)
            intent.putExtra("currency_symbol", currency_symbol)


            startActivityForResult(intent, FILTER_RQ_CODE)
        }
    }

    private fun switch_scrollview_np(){
        if (np_active){
            picker_listener.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
        } else{
            scrollView.visibility = View.GONE
            picker_listener.visibility = View.VISIBLE
        }
        np_active = !np_active
    }

    private fun page_back(){
        if (current_page == 0){
            current_page = pages.size-1
        }else{
            current_page--
        }
        load_page()
    }

    private fun page_forward(){
        if (current_page < pages.size -1){
            current_page++
        }else{
            current_page = 0
        }
        load_page()
    }

    private fun load_page(){

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
                            applicationContext, currency_symbol)
                } else {
                    result_containers[i].container.visibility = View.GONE
                }
            } else {
                result_containers[i].container.visibility = View.GONE
            }
        }
        middle_button.text = "page ${(current_page + 1)}/${pages.size}"
        scrollView.fullScroll(ScrollView.FOCUS_UP)
    }

    private fun get_result_container_array(): Array<Result_Container>{
        fun get_view_from_string(id:String): View {
            return findViewById(resources.getIdentifier(id, "id", packageName))
        }
        val ret = mutableListOf<Result_Container>()
        for (i in 0 until 20){
            ret.add(
                    Result_Container(
                            get_view_from_string("result_container$i") as ConstraintLayout,
                            get_view_from_string("result_title$i") as TextView,
                            get_view_from_string("discount_percentage$i") as TextView,
                            get_view_from_string("new_price$i") as TextView,
                            get_view_from_string("old_price$i") as TextView,
                            get_view_from_string("user_rating_label$i") as TextView,
                            get_view_from_string("result_thumbnail$i") as ImageView
                    )
            )
        }
        return ret.toTypedArray()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, return_intent: Intent) {
        // Handle the logic for the requestCode, resultCode and return_intent returned.
        if (requestCode == FILTER_RQ_CODE && resultCode == Activity.RESULT_OK){
            val intent_keys = Keys.Companion.Serializable_Keys()

            filter_and_settings = return_intent.getSerializableExtra(intent_keys.filter) as Filtering_And_sorting.Companion.Filter_and_settings
            filter_and_settings.min_max_ranges = settings_defaults// This is a hack to fix a bug with serialisation see todos (Filter_and_settings min_max_ranges hack)


            sort_key = Keys.Companion.Sort_By_Setting(currency_symbol).get_setting(return_intent.getStringExtra(intent_keys.sort_by))!!
            sort_from_high_to_low = return_intent.getBooleanExtra(intent_keys.sort_order, true)

            if (filter_and_settings.current_region != current_region) {
                current_region = filter_and_settings.current_region
                load_json_from_disk()
            }
            filter_results_and_display_ui()
        }
    }

    class Result_Container constructor(
            val container: ConstraintLayout,
            private val title_: TextView,
            private val discount: TextView,
            private val new_price: TextView,
            private val old_price: TextView,
            private val rating: TextView,
            private val thumbnail: ImageView
    ){

        fun load_data(data: JSONObject, listener: View.OnClickListener,
                      applicationContext: Context, currency_symbol: String
        ){
            val thumbnail_url = url_builder.get_thumbnail_url(data)
            val keys = Keys.Companion.Filter_Keys()
            container.setOnClickListener(listener)
            title_.text = data[keys.title].toString()
            discount.text = " -${data[keys.discount]}% "
            new_price.text = "${data[keys.new_price]}$currency_symbol"
            old_price.text = "${data[keys.old_price]}$currency_symbol"
            rating.text = "Rating ${data[keys.rating]}%"
            //val url = get_thumbnail_url(data)
            Picasso.with(applicationContext).load(thumbnail_url).into(thumbnail)

            old_price.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    fun snackbar_message(msg :String){
        val mySnackbar = Snackbar.make(findViewById(android.R.id.content),
                Html.fromHtml("<font color=\"#000000\">$msg</font>"), Snackbar.LENGTH_LONG)
        val view = mySnackbar.view
        view.setBackgroundColor(Color.LTGRAY)
        view.alpha = 1f
        mySnackbar.show()
    }

    private class startup{
        companion object {

            private const val skip_cache_refresh = false
            private const val force_cache_refresh = false

            private fun cache_log(logline:String){/*Log.i("cache", logline)*/}

            private fun get_json_file(context: Context, url: String ): File =
                    Uri.parse(url)?.lastPathSegment?.let { filename ->
                        File(context.cacheDir, filename)
                        // i don't quite understand why this method needs the non null assertion
                    }!!

            private fun cache_refresh_needed(file :File):Boolean{
                if (skip_cache_refresh){
                    cache_log("not refreshing cache. turned of right now.")
                    return false
                }


                val max_cache_age: Long = (3600 * 4) * 1000 // .LastModified returns the unix time im ms
                if (force_cache_refresh){
                    return true
                }else{
                    return if (file.exists()){
                        if (file.lastModified() < System.currentTimeMillis()- max_cache_age){
                            cache_log("jsonfile older than two hours refreshing cache.")
                            true
                        }else{
                            cache_log("jsonfile less than two hours old. loading file from cache.")
                            false
                        }
                    }else{
                        cache_log("jsonfile not found in cache. downloading json.")
                        true
                    }
                }
            }

            fun get_json_and_start(_this: MainActivity){
                val url = _this.zipped_json_url
                val zip_file = get_json_file(_this.applicationContext, url)

                fun start_app(){
                    _this.load_json_from_disk()
                    _this.filter_results_and_display_ui()
                }

                val sucsess_msg = "Congratulations! You have the latest sales data."

                if (cache_refresh_needed(zip_file)){
                    var do_start_app = true
                    doAsyncResult {
                        fun download_json(){
                            val cn = URL(_this.zipped_json_url).openConnection()
                            cn.connect()
                            val stream = cn.getInputStream()
                            val out = FileOutputStream(zip_file)
                            val buf = ByteArray(16384)
                            while (true) {
                                val numread = stream.read(buf)
                                if (numread <= 0) break
                                out.write(buf, 0, numread)
                            }
                        }
                        fun extract_zip(){
                            Java_Utils.UnzipUtility.unzip_file(zip_file.toString(),
                                    _this.applicationContext.cacheDir.toString())
                        }

                        var dl_failed = false
                        try {
                            download_json()
                        }catch (except: UnknownHostException){
                            dl_failed = true
                        }
                        var msg = ""
                        if (dl_failed){
                            fun old_files_are_present(): Boolean{
                                val files = Keys.region_to_filename.values.toTypedArray()
                                val cachedir = _this.applicationContext.cacheDir
                                fun has_old_files(i:Int = 0) :Boolean{
                                    if (i >= files.size){
                                        return true
                                    }
                                    if (File(cachedir, files[i]).exists()){
                                        return has_old_files(i+1)
                                    }else{
                                        return false
                                    }
                                }
                                return has_old_files()
                            }
                            if (old_files_are_present()){
                                msg = "Download failed, loaded the latest available data."
                            }else{
                                msg = "Download failed, and no older data available." +
                                        " Please connect to the internet and retry"
                                do_start_app = false
                            }
                        }else{
                            extract_zip()
                            msg = sucsess_msg
                        }
                        uiThread {
                            if (do_start_app){
                                start_app()
                            }
                            _this.snackbar_message(msg)
                        }
                    }
                }else{
                    start_app()
                    _this.snackbar_message(sucsess_msg)
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

            private fun get_app_url_element(url: Url_Builder, data: JSONObject, is_image: Boolean): Url_Builder {
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


