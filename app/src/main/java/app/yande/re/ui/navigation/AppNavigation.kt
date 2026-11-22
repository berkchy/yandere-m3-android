package app.yande.re.ui.navigation

object Routes {
    const val GALLERY = "gallery"
    const val DETAIL = "detail/{postId}"
    const val FAVORITES = "favorites"

    fun detail(postId: Long) = "detail/$postId"
}
