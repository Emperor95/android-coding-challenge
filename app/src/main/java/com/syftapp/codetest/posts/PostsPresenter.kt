package com.syftapp.codetest.posts

import com.syftapp.codetest.Constants.POSTS_PER_PAGE
import com.syftapp.codetest.Constants.TOTAL_BLOG_POSTS
import com.syftapp.codetest.data.model.domain.Post
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import timber.log.Timber

class PostsPresenter(private val getPostsUseCase: GetPostsUseCase) : KoinComponent {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var view: PostsView
    private var currentPage = 1

    fun bind(view: PostsView) {
        this.view = view
        compositeDisposable.add(loadPosts())
    }

    fun unbind() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    fun showDetails(post: Post) {
        view.render(PostScreenState.PostSelected(post))
    }

    private fun loadPosts(pageToLoad: Int = 1): Disposable {
        Timber.d("pageToLoad: $pageToLoad")
        return getPostsUseCase.execute(pageToLoad)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.render(PostScreenState.Loading) }
            .doAfterTerminate { view.render(PostScreenState.FinishedLoading) }
            .subscribe(
                { view.render(PostScreenState.DataAvailable(it)) },
                { view.render(PostScreenState.Error(it)) }
            )
    }

    fun loadMorePosts() {
        if (currentPage * POSTS_PER_PAGE >= TOTAL_BLOG_POSTS) return
        loadPosts(++currentPage)
    }
}