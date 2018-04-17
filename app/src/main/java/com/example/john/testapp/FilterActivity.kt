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
import android.widget.RemoteViews
import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.swipeable_setting_fragment.view.*
import java.io.Serializable
import com.example.john.testapp.Filtering_And_sorting.Companion.Filter


class FilterActivity : AppCompatActivity() {

    private lateinit var filter : Filter
    private lateinit var rangebars: Rangebar_Collection
    private lateinit var currency_symbol: String
    private var sort_comparators = Keys.Companion.Sort_Comparators()
    private var mSectionsPagerAdapter1: SectionsPagerAdapter? = null
    private var mSectionsPagerAdapter2: SectionsPagerAdapter? = null
    private var mSectionsPagerAdapter3: SectionsPagerAdapter? = null
    private var mSectionsPagerAdapter4: SectionsPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        filter =  intent.getSerializableExtra("filter") as Filter
        currency_symbol = intent.getStringExtra("currency_symbol")
        rangebars = get_rangebar_collection()

        view_pager_test()


        //this.window.attributes.height
        //filter_settings_container.height
        Log.e("done", "done")
    }

    fun view_pager_test(){
        val setting1 = intent.getStringArrayExtra("sort_by_setting")
        val setting2 = intent.getStringArrayExtra("reverse_sort_order_setting")
        val setting3 = intent.getStringArrayExtra("bundles_only_setting")
        //val setting4 = arrayOf("setting4.1", "setting4.2")
        mSectionsPagerAdapter1 = SectionsPagerAdapter(supportFragmentManager,setting1, container1)
        mSectionsPagerAdapter2 = SectionsPagerAdapter(supportFragmentManager,setting2, container2)
        mSectionsPagerAdapter3 = SectionsPagerAdapter(supportFragmentManager,setting3, container3)
        //mSectionsPagerAdapter4 = SectionsPagerAdapter(supportFragmentManager,setting4, container4)
        // Set up the ViewPager with the sections adapter.
        val wrapped_adapter1 = InfinitePagerAdapter(mSectionsPagerAdapter1)
        val wrapped_adapter2 = InfinitePagerAdapter(mSectionsPagerAdapter2)
        val wrapped_adapter3 = InfinitePagerAdapter(mSectionsPagerAdapter3)
        //val wrapped_adapter4 = InfinitePagerAdapter(mSectionsPagerAdapter4)
        container1.adapter = wrapped_adapter1
        container2.adapter = wrapped_adapter2
        container3.adapter = wrapped_adapter3
        //container4.adapter = wrapped_adapter4

        // this bit restores the previous settings
        container1.currentItem += filter.last_sort_by_idx
        if (filter.sort_order_offset){container2.currentItem += 1}
        container3.currentItem += filter.bundles_only
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

    /**
     * A placeholder fragment containing a simple view.
     */
    class Swipable_Setting_Fragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.swipeable_setting_fragment, container, false)
            rootView.section_label.text = arguments.getString("arg_string")

            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
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
    fun get_rangebar_collection(): Rangebar_Collection{

        return Rangebar_Collection(
                Smooth_Rangebar(rangebar0, upper_value0, rangebar_name0, lower_value0,
                            "Discount", filter.defaults.def_discount, filter.discount, "%", false),
                Smooth_Rangebar(rangebar1, upper_value1, rangebar_name1, lower_value1,
                            "Discounted price", filter.defaults.def_new_price, filter.new_price, currency_symbol),
                Smooth_Rangebar(rangebar2, upper_value2, rangebar_name2, lower_value2,
                            "Total discount", filter.defaults.def_absolute_discount, filter.absolute_discount, currency_symbol),
                Smooth_Rangebar(rangebar3, upper_value3, rangebar_name3, lower_value3,
                            "Original price", filter.defaults.def_old_price, filter.old_price, currency_symbol),
                Quantized_Rangebar(rangebar4, upper_value4, rangebar_name4, lower_value4,
                            "Number of reviews", filter.defaults.n_reviews_quant_points, filter.reviews),
                Smooth_Rangebar(rangebar5, upper_value5, rangebar_name5, lower_value5,
                            "Rating", filter.defaults.def_rating, filter.rating, "%", false)
                )
    }

    override fun onBackPressed() {
        return_to_main_activity()
    }

    fun return_to_main_activity() {
        val ret_intent = Intent(this, MainActivity::class.java)
        filter.discount = rangebars.discount.current_value
        filter.new_price = rangebars.new_price.current_value
        filter.old_price = rangebars.old_price.current_value
        filter.rating = rangebars.rating.current_value
        filter.reviews = rangebars.reviews.current_value
        filter.absolute_discount = rangebars.absolute_discount.current_value

        filter.bundles_only = Keys.Companion.Bundles_Only_Setting().get_setting(mSectionsPagerAdapter3!!.get_current_string())
        filter.last_sort_by_idx = container1.currentItem
        var return_order = false

        if ((container2.currentItem % 2 ) > 0){return_order = !return_order}
        filter.sort_order_offset = return_order

        val keys = Keys.Companion.Serialisable_Keys()
        ret_intent.putExtra(keys.filter, filter as Serializable)
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
