package com.esmaeel.moviesapp.ui.PopularPersonsPage


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esmaeel.moviesapp.R
import com.esmaeel.moviesapp.Utils.*
import com.esmaeel.moviesapp.data.models.PopularPersonsResponse
import com.esmaeel.moviesapp.data.models.Results
import com.esmaeel.moviesapp.databinding.ActivityPopularPersonsBinding
import com.esmaeel.moviesapp.di.POPULAR_ADAPTER
import com.esmaeel.moviesapp.ui.PersonDetailsPage.PersonDetailsActivity
import com.esmaeel.moviesapp.ui.PopularPersonsPage.Adapter.PopularPersonsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PopularPersonsActivity : AppCompatActivity() {

    @Inject
    @POPULAR_ADAPTER
    lateinit var popularPersonsAdapter: PopularPersonsAdapter
    private val viewModel: PopularsViewModel by viewModels()
    private lateinit var binder: ActivityPopularPersonsBinding
    private lateinit var paginationListener: EndlessRecyclerOnScrollListener
    private var personsList: MutableList<Results?>? = null
    private var pageNumber = Constants.FirstPage
    private var hasMorePages: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityPopularPersonsBinding.inflate(layoutInflater)
        setContentView(binder.root)

        initData()
        initViews()
        requestPageNumber(pageNumber)
    }


    private fun requestPageNumber(pageNumber: Int) {
        if (isNetworkAvailable())
            viewModel.getPersonsData(pageNumber)
        else showSnackMessage(getString(R.string.network_error), binder.root)
    }

    private fun initViews() {
        binder.swipe.apply {
            setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark
            )
            setOnRefreshListener {
                this.isRefreshing = false
            }
        }

        paginationListener = object : EndlessRecyclerOnScrollListener() {
            override fun onLoadMore(currentPage: Int, lastVisibleItemPosition: Int) {
                pageNumber = currentPage
                requestPageNumber(pageNumber)
            }
        }

        binder.recycler.apply {
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            adapter = popularPersonsAdapter
            addOnScrollListener(paginationListener)
        }

        popularPersonsAdapter.clickEvent.observe(this, Observer {
            PersonDetailsActivity.startActivity(
                context = this,
                person = it.data
            )
        })


    }


    private fun initData() {
        viewModel.personsData.observe(this, Observer { contract ->
            when (contract.status) {
                Status.SUCCESS -> {
                    hideLoader()
                    bindDataToUi(contract.data)
                }
                Status.LOADING -> {
                    showLoader()
                }
                Status.ERROR -> {
                    hideLoader()
                    showSnackMessage(contract.message ?: "", binder.root)
                }
            }
        })
    }

    private fun bindDataToUi(data: PopularPersonsResponse?) {

        data?.let {
            UiTestUtils.waiT()
            hasMorePages = data.hasMorePages()

            if (!personsList.isNullOrEmpty())
                personsList!!.addAll(data.results!!)
            else personsList =
                data.results

            /*
            *  this is for the DiffUtil because it calculates
            * the differences in the background thread and may take time.
            * so we make a call back to tell Espresso to stop waiting
            * */

            val dataBindedCallBack = Runnable {
                UiTestUtils.release()
            }

            popularPersonsAdapter.submitList(personsList, dataBindedCallBack)

        }

    }


    private fun showLoader() {
        binder.swipe.isRefreshing = true
    }

    private fun hideLoader() {
        binder.swipe.isRefreshing = false
    }
}