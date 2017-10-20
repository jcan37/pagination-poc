package com.viacom.playplexpagination

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.*
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.viacom.playplexpagination.databinding.ActivityMainBinding

object PaginationAPI {

    private val remoteItems = mutableListOf(
            "Jeff", "Abdul", "Emmy", "Mike", "Cameron",
            "Jamie", "Adam", "Noah", "Darren", "Jen",
            "Ethan", "Eric", "Edward", "Jordan", "Matt",
            "Brian", "Lindsay", "Diana", "Corey", "Larry",
            "Janine", "Natalie", "Chris", "Dave", "John",
            "Anibal", "Danny", "Jacek", "Greg", "Shane",
            "Paul", "Kevin", "Dana", "Brittany", "Ebby",
            "Mohammed", "Zelimir", "Liz", "Jane", "Jack",
            "Boris", "Tess", "Ben", "Frank", "Henry",
            "Jake", "Lily", "Peter", "Marshall", "Ted")

    fun fetch(page: Int, pageSize: Int): List<Series> =
            ((page - 1) * pageSize).let { startPosition ->
                Thread.sleep(5000L)
                return remoteItems.subList(startPosition, minOf(startPosition + pageSize, remoteItems.size))
                        .mapIndexed { index, title -> Series("$index", title) }
            }
}

data class Series(val mgid: String, val title: String) {
    override fun equals(other: Any?) = title == (other as? Series)?.title
    override fun hashCode(): Int = super.hashCode()
}

class AllShowsDataSource : TiledDataSource<Series>() {

    override fun countItems() = DataSource.COUNT_UNDEFINED

    override fun loadRange(startPosition: Int, count: Int): MutableList<Series> =
            PaginationAPI.fetch((startPosition / count) + 1, count).toMutableList()
}

class AllShowsPagedListProvider : LivePagedListProvider<Int, Series>() {
    override fun createDataSource() = AllShowsDataSource()
}

const val PAGE_SIZE = 10
const val PREFETCH_DISTANCE = PAGE_SIZE / 2

class AllShowsViewModel : ViewModel() {
    val seriesList: LiveData<PagedList<Series>> = AllShowsPagedListProvider().create(0,
            PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPageSize(PAGE_SIZE)
                    .setInitialLoadSizeHint(PAGE_SIZE)
                    .setPrefetchDistance(PREFETCH_DISTANCE)
                    .build())
}

class SeriesViewHolder(val titleView: TextView) : RecyclerView.ViewHolder(titleView)

class SeriesDiffCallback : DiffCallback<Series>() {
    override fun areItemsTheSame(oldItem: Series, newItem: Series) = oldItem.mgid == newItem.mgid
    override fun areContentsTheSame(oldItem: Series, newItem: Series) = oldItem == newItem
}

class SeriesAdapter : PagedListAdapter<Series, SeriesViewHolder>(SeriesDiffCallback()) {

    override fun onBindViewHolder(holder: SeriesViewHolder?, position: Int) {
        val series = getItem(position)
        holder?.titleView?.text = series?.title ?: ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SeriesViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.series_item, parent, false) as TextView)
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.list.layoutManager =
                LinearLayoutManager(this).apply { orientation = LinearLayout.VERTICAL }
        binding.list.adapter = SeriesAdapter().apply {
            ViewModelProviders.of(this@MainActivity).get(AllShowsViewModel::class.java).apply {
                seriesList.observe(this@MainActivity, Observer { setList(it) })
            }
        }
    }
}
