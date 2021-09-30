package com.syftapp.codetest.posts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.databinding.ViewPostListItemBinding

class PostsAdapter(
    private val presenter: PostsPresenter
) : RecyclerView.Adapter<PostViewHolder>() {

    private val DIffUtilsCallback = object : DiffUtil.ItemCallback<Post>() {

        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIffUtilsCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ViewPostListItemBinding.inflate(layoutInflater, parent, false)

        return PostViewHolder(view, presenter)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Post>) {
        differ.submitList(list)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }
}

class PostViewHolder(
    private val view: ViewPostListItemBinding,
    private val presenter: PostsPresenter
) : RecyclerView.ViewHolder(view.root) {

    @SuppressLint("SetTextI18n")
    fun bind(item: Post) = with(item){
        view.postTitle.text = "$id $title"
        view.bodyPreview.text = body
        view.root.setOnClickListener { presenter.showDetails(this) }
    }

}