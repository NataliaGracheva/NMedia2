package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity


class PostRepositoryImpl(
    private val postDao: PostDao
) : PostRepository {
    override val data: LiveData<List<Post>> = postDao.getAll().map { it.toDto() }

    override suspend fun getAll() {
        val response = PostsApi.retrofitService.getAll()

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        val posts = response.body() ?: throw RuntimeException("body is null")
        postDao.insert(posts.toEntity())
    }

    override suspend fun save(post: Post) {
        val response = PostsApi.retrofitService.save(post)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val post = response.body() ?: throw RuntimeException("body is null")
        postDao.insert(PostEntity.fromDto(post))
    }

    override suspend fun removeById(id: Long) {
        postDao.removeById(id)
        val response = PostsApi.retrofitService.removeById(id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

    override suspend fun likeById(id: Long) {
        postDao.likeById(id)
        val response = PostsApi.retrofitService.likeById(id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

    override suspend fun unlikeById(id: Long) {
        postDao.likeById(id)
        val response = PostsApi.retrofitService.unlikeById(id)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

}
