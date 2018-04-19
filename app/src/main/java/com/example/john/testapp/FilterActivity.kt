package com.example.john.testapp

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.swipeable_setting_fragment.view.*
import java.io.Serializable
import com.example.john.testapp.Filtering_And_sorting.Companion.Filter_and_settings


class FilterActivity : AppCompatActivity() {

    private lateinit var filter_and_settings : Filter_and_settings
    private lateinit var rangebars: Rangebar_Collection
    private lateinit var currency_symbol: String
    private var mSectionsPagerAdapter1: SectionsPagerAdapter? = null
    private var mSectionsPagerAdapter2: SectionsPagerAdapter? = null
    private var mSectionsPagerAdapter3: SectionsPagerAdapter? = null
    private var mSectionsPagerAdapter4: SectionsPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        filter_and_settings =  intent.getSerializableExtra("filter_and_settings") as Filter_and_settings
        currency_symbol = intent.getStringExtra("currency_symbol")
        rangebars = get_rangebar_collection()

        init_view_pagers()


        //this.window.attributes.height
        //filter_settings_container.height
        Log.e("done", "done")
    }

    private fun init_view_pagers(){
        //todo cleanup
        val sort_by_strings = Keys.Companion.Sort_By_Setting().strings
        val sort_order_strings = Keys.Companion.Sort_Order_Setting().strings
        val bundles_only_strings = Keys.Companion.Bundles_Only_Setting().strings
        val region_strings = Keys.Companion.Region_Setting().strings
        mSectionsPagerAdapter1 = SectionsPagerAdapter(supportFragmentManager,sort_by_strings, container1)
        mSectionsPagerAdapter2 = SectionsPagerAdapter(supportFragmentManager,sort_order_strings, container2)
        mSectionsPagerAdapter3 = SectionsPagerAdapter(supportFragmentManager,bundles_only_strings, container3)
        mSectionsPagerAdapter4 = SectionsPagerAdapter(supportFragmentManager,region_strings, container4)
        // Set up the ViewPager with the sections adapter.
        val wrapped_adapter1 = InfinitePagerAdapter(mSectionsPagerAdapter1)
        val wrapped_adapter2 = InfinitePagerAdapter(mSectionsPagerAdapter2)
        val wrapped_adapter3 = InfinitePagerAdapter(mSectionsPagerAdapter3)
        val wrapped_adapter4 = InfinitePagerAdapter(mSectionsPagerAdapter4)
        container1.adapter = wrapped_adapter1
        container2.adapter = wrapped_adapter2
        container3.adapter = wrapped_adapter3
        container4.adapter = wrapped_adapter4

        // this bit restores the previous settings
        container1.currentItem += filter_and_settings.last_sort_by_idx
        if (filter_and_settings.sort_order_offset){
            container2.currentItem += 1
        }
        container3.currentItem += filter_and_settings.bundles_only_int
        container4.currentItem += Keys.Companion.Region_Setting().get_setting(
                filter_and_settings.current_region
        )
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager, private val strings: Array<String>, private val pager: ViewPager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a Swipable_Setting_Fragment (defined as a static inner class below).
            return if (strings.size > 3){
                Swipable_Setting_Fragment.newInstance(strings[position])
            } else {
                Swipable_Setting_Fragment.newInstance(strings[position % strings.size])
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return if (strings.size > 3){
                strings.size
            } else {
                strings.size * 2
            }
        }

        fun get_current_string(): String{
            return if (strings.size > 3){
                strings[pager.currentItem]
            } else {
                strings[pager.currentItem % strings.size]
            }
        }
    }


    class Swipable_Setting_Fragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.swipeable_setting_fragment, container, false)
            rootView.section_label.text = arguments.getString("arg_string")

            return rootView
        }

        companion object {
            fun newInstance(display_string: String): Swipable_Setting_Fragment {
                val fragment = Swipable_Setting_Fragment()
                val args = Bundle()
                args.putString("arg_string", display_string)
                fragment.arguments = args
                return fragment
            }
        }
    }


    //_rangebar code
    private fun get_rangebar_collection(): Rangebar_Collection{

        return Rangebar_Collection(
                Smooth_Rangebar(rangebar0, upper_value0, rangebar_name0, lower_value0,
                            "Discount", filter_and_settings.min_max_ranges.def_discount, filter_and_settings.discount, "%", false),
                Smooth_Rangebar(rangebar1, upper_value1, rangebar_name1, lower_value1,
                            "Discounted price", filter_and_settings.min_max_ranges.def_new_price, filter_and_settings.new_price, currency_symbol),
                Smooth_Rangebar(rangebar2, upper_value2, rangebar_name2, lower_value2,
                            "Total discount", filter_and_settings.min_max_ranges.def_absolute_discount, filter_and_settings.absolute_discount, currency_symbol),
                Smooth_Rangebar(rangebar3, upper_value3, rangebar_name3, lower_value3,
                            "Original price", filter_and_settings.min_max_ranges.def_old_price, filter_and_settings.old_price, currency_symbol),
                Quantized_Rangebar(rangebar4, upper_value4, rangebar_name4, lower_value4,
                            "Number of reviews", filter_and_settings.min_max_ranges.n_reviews_quant_points, filter_and_settings.reviews),
                Smooth_Rangebar(rangebar5, upper_value5, rangebar_name5, lower_value5,
                            "Rating", filter_and_settings.min_max_ranges.def_rating, filter_and_settings.rating, "%", false)
                )
    }

    override fun onBackPressed() {
        return_to_main_activity()
    }

    private fun return_to_main_activity() {
        val ret_intent = Intent(this, MainActivity::class.java)
        filter_and_settings.discount = rangebars.discount.current_value
        filter_and_settings.new_price = rangebars.new_price.current_value
        filter_and_settings.old_price = rangebars.old_price.current_value
        filter_and_settings.rating = rangebars.rating.current_value
        filter_and_settings.reviews = rangebars.reviews.current_value
        filter_and_settings.absolute_discount = rangebars.absolute_discount.current_value

        filter_and_settings.bundles_only_int = Keys.Companion.Bundles_Only_Setting().get_setting(mSectionsPagerAdapter3!!.get_current_string())
        filter_and_settings.last_sort_by_idx = container1.currentItem
        filter_and_settings.current_region = mSectionsPagerAdapter4!!.get_current_string()
        var return_order = false

        if ((container2.currentItem % 2 ) > 0){return_order = !return_order}
        filter_and_settings.sort_order_offset = return_order

        val keys = Keys.Companion.Serializable_Keys()
        ret_intent.putExtra(keys.filter, filter_and_settings as Serializable)
        ret_intent.putExtra(keys.sort_by, mSectionsPagerAdapter1!!.get_current_string())
        ret_intent.putExtra(keys.sort_order, mSectionsPagerAdapter2!!.get_current_string())

        setResult(Activity.RESULT_OK, ret_intent)
        finish()
    }

    class Rangebar_Collection constructor(
            val discount: Smooth_Rangebar,
            val new_price: Smooth_Rangebar,
            val absolute_discount: Smooth_Rangebar,
            val old_price: Smooth_Rangebar,
            val reviews: Quantized_Rangebar,
            val rating: Smooth_Rangebar
    )
}
