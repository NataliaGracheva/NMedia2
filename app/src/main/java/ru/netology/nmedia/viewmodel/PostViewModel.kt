package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "Me",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        // Начинаем загрузку
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(value: List<Post>) {
                _data.postValue(FeedModel(posts = value, empty = value.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        _data.postValue(FeedModel(loading = true))
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(value: Post) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })

        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun removeById(id: Long) {
        _data.postValue(FeedModel(loading = true))
        val old = data.value?.posts.orEmpty()
        repository.removeById(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(value: Unit) {
                val new = old.filter { it.id != id }
                _data.postValue(_data.value?.copy(posts = new))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun likeById(post: Post) {
        _data.postValue(FeedModel(loading = true))
        val old = data.value?.posts.orEmpty()
        if (post.likedByMe) repository.unlikeById(post.id, object : PostRepository.Callback<Post> {
            override fun onSuccess(value: Post) {
                val new = old.map { if (it.id == post.id) value else it }
                _data.postValue(_data.value?.copy(posts = new))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
        else repository.likeById(post.id, object : PostRepository.Callback<Post> {
            override fun onSuccess(value: Post) {
                val new = old.map { if (it.id == post.id) value else it }
                _data.postValue(_data.value?.copy(posts = new))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}
