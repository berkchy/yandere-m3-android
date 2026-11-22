package app.yande.re.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface YandereApi {
    @GET("post.json")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("tags") tags: String? = null,
        @Query("api_version") apiVersion: Int = 2,
        @Query("include_tags") includeTags: Int = 1
    ): PostsResponse
}
