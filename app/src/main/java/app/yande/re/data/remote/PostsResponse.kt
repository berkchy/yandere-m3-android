package app.yande.re.data.remote

import app.yande.re.model.Post
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

@Serializable(with = PostsResponseSerializer::class)
data class PostsResponse(
    val posts: List<Post> = emptyList()
)

object PostsResponseSerializer : KSerializer<PostsResponse> {
    private val listSerializer = ListSerializer(Post.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): PostsResponse {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        val posts = when (element) {
            is JsonArray -> jsonDecoder.json.decodeFromJsonElement(listSerializer, element)
            is JsonObject -> {
                val postsElement = element["posts"]
                if (postsElement is JsonArray) {
                    jsonDecoder.json.decodeFromJsonElement(listSerializer, postsElement)
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
        return PostsResponse(posts)
    }

    override fun serialize(encoder: Encoder, value: PostsResponse) {
        encoder.encodeSerializableValue(listSerializer, value.posts)
    }
}
