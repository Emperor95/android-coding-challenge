package com.syftapp.codetest.posts

import android.os.Bundle
import android.view.View
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.Constants.TOTAL_BLOG_POSTS
import com.syftapp.codetest.Navigation
import com.syftapp.codetest.R
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ActivityPostsBinding
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent

class PostsActivity : AppCompatActivity(), PostsView, KoinComponent {

    private val presenter: PostsPresenter by inject()
    private lateinit var navigation: Navigation

    private val binding by lazy { ActivityPostsBinding.inflate(layoutInflater) }
    private val postAdapter by lazy { PostsAdapter(presenter) }
    private var pageScrolled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        navigation = Navigation(this)

        with(binding.listOfPosts){
            val separator = DividerItemDecoration(this@PostsActivity, DividerItemDecoration.VERTICAL)
            addItemDecoration(separator)
            adapter = postAdapter
            loadPostsOnPageEnd()
        }

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

    private fun showLoading() = with(binding) {
        error.visibility = View.GONE
        listOfPosts.visibility = View.GONE
        loading.visibility = View.VISIBLE
    }

    private fun hideLoading() = with(binding) {
        loading.visibility = View.GONE
    }

    private fun showPosts(posts: List<Post>) = with(binding) {
        // this is a fairly crude implementation, if it was Flowable, it would
        // be better to use DiffUtil and consider notifyRangeChanged, notifyItemInserted, etc
        // to preserve animations on the RecyclerView
        postAdapter.submitList(posts)
        listOfPosts.visibility = View.VISIBLE
    }

    private fun showError(message: String) = with(binding) {
        error.visibility = View.VISIBLE
        error.text = message
    }

    private fun RecyclerView.loadPostsOnPageEnd() {
        val linearLayoutManager = layoutManager as LinearLayoutManager
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    if (totalItemCount != TOTAL_BLOG_POSTS){
                        presenter.loadMorePosts()
                        pageScrolled = false
                    }
                }
            }
        })
    }
}
