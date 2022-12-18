package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(AppDb.getInstance(application).postDao())
    val data: LiveData<FeedModel>
        get() = repository.data.map {
            FeedModel(it, it.isEmpty())
        }
    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: MutableLiveData<FeedModelState>
        get() = _dataState
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        // Начинаем загрузку
        _dataState.postValue(FeedModelState.Loading)
        try {
            repository.getAll()
            _dataState.postValue(FeedModelState.Idle)
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState.Error)
        }
    }

    fun refresh() = viewModelScope.launch {
        _dataState.postValue(FeedModelState.Refreshing)
        try {
            repository.getAll()
            _dataState.postValue(FeedModelState.Idle)
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState.Error)
        }
    }

    fun save() = viewModelScope.launch {
        _dataState.postValue(FeedModelState.Loading)
        try {
            edited.value?.let {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
            edited.value = empty
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState.Error)
        }

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

    fun removeById(id: Long) = viewModelScope.launch {
        _dataState.postValue(FeedModelState.Loading)
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState.Error)
        }
    }

    fun likeById(post: Post) = viewModelScope.launch {
        _dataState.postValue(FeedModelState.Loading)
        try {
            if (post.likedByMe) repository.unlikeById(post.id)
            else repository.likeById(post.id)
        } catch (e: Exception) {
            _dataState.postValue(FeedModelState.Error)
        }
    }
}
