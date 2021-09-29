package com.syftapp.codetest.posts

import android.os.Bundle
import android.view.View
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.Constants.TOTAL_BLOG_POSTS
import com.syftapp.codetest.Navigation
import com.syftapp.codetest.R
import com.syftapp.codetest.data.model.domain.Post
import kotlinx.android.synthetic.main.activity_posts.*
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import timber.log.Timber

class PostsActivity : AppCompatActivity(), PostsView, KoinComponent {

    private val presenter: PostsPresenter by inject()
    private lateinit var navigation: Navigation

    private lateinit var adapter: PostsAdapter
    private var pageScrolled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        navigation = Navigation(this)

        listOfPosts.layoutManager = LinearLayoutManager(this)
        val separator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        listOfPosts.addItemDecoration(separator)

        loadPostsOnPageEnd()

        presenter.bind(this)
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun render(state: PostScreenState) {
        when (state) {
            is PostScreenState.Loading -> showLoading()
            is PostScreenState.DataAvailable -> showPosts(state.posts)
            is PostScreenState.Error -> showError(getString(R.string.load_posts_error_message))
            is PostScreenState.FinishedLoading -> hideLoading()
            is PostScreenState.PostSelected -> navigation.navigateToPostDetail(state.post.id)
        }
    }

    private fun showLoading() {
        error.visibility = View.GONE
        listOfPosts.visibility = View.GONE
        loading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loading.visibility = View.GONE
    }

    private fun showPosts(posts: List<Post>) {
        // this is a fairly crude implementation, if it was Flowable, it would
        // be better to use DiffUtil and consider notifyRangeChanged, notifyItemInserted, etc
        // to preserve animations on the RecyclerView
        adapter = PostsAdapter(posts, presenter)
        listOfPosts.adapter = adapter
        listOfPosts.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        error.visibility = View.VISIBLE
        error.setText(message)
    }

    private fun loadPostsOnPageEnd() {
        val linearLayoutManager = listOfPosts.layoutManager as LinearLayoutManager

        listOfPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                //prevent bottom of page from been reached if recyclerView was not scrolled
                if (newState == SCROLL_STATE_TOUCH_SCROLL) pageScrolled = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount: Int = linearLayoutManager.childCount
                val totalItemCount: Int = linearLayoutManager.itemCount
                val pastVisibleItems: Int = linearLayoutManager.findFirstVisibleItemPosition()

                if (pageScrolled && pastVisibleItems + visibleItemCount >= totalItemCount) {
                    //reached bottom of recyclerView.
                    if (totalItemCount != TOTAL_BLOG_POSTS){
                        Timber.d("loading more posts ...")
                        presenter.loadMorePosts()
                        pageScrolled = false
                    }
                }
            }
        })
    }
}
