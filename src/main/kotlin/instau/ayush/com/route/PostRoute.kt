package instau.ayush.com.route

import instau.ayush.com.model.PostResponse
import instau.ayush.com.model.PostTextParams
import instau.ayush.com.repository.post.PostRepository
import instau.ayush.com.repository.post.connectedClients
import instau.ayush.com.util.Constants
import instau.ayush.com.util.getLongParameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.ktor.ext.inject

fun Routing.postRouting(){

    val postRepository by inject<PostRepository>()

    authenticate {
        route(path = "/post") {
            post(path = "/create") {
//                var fileName = ""
//                var postTextParams: PostTextParams? = null
//                val multiPartData = call.receiveMultipart()
//
//                multiPartData.forEachPart { partData ->
//                    when (partData) {
//                        is PartData.FileItem -> {
//                            fileName = partData.saveFile(folderPath = Constants.POST_IMAGES_FOLDER_PATH)
//                        }
//
//                        is PartData.FormItem -> {
//                            if (partData.name == "post_data") {
//                                postTextParams = Json.decodeFromString(partData.value)
//                            }
//                        }
//
//                        else -> {}
//                    }
//                    partData.dispose()
//                }

//                val imageUrl = "${Constants.BASE_URL}${Constants.POST_IMAGES_FOLDER}$fileName"
                val postTextParams = call.receiveNullable<PostTextParams>()


                if (postTextParams == null) {
//                    File("${Constants.POST_IMAGES_FOLDER_PATH}/$fileName").delete()

                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = PostResponse(
                            success = false,
                            message = "Could not parse post data"
                        )
                    )
                } else {
                    val result = postRepository.createPost(postTextParams)
                    call.respond(result.code, message = result.data)
                }
            }

            get(path = "/{postId}") {
                try {
                    val postId = call.getLongParameter("postId")
                    val currentUserId = call.getLongParameter("currentUserId", true)

                    val result = postRepository.getPost(postId, currentUserId)
                    call.respond(
                        status = result.code,
                        message = result.data
                    )
                } catch (e: BadRequestException) {
                    return@get
                } catch (e: Throwable) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = PostResponse(
                            success = false,
                            message = "An unexpected error has occurred, try again!"
                        )
                    )
                }
            }

            delete(path = "/{postId}") {
                try {
                    val postId = call.getLongParameter("postId")
                    val result = postRepository.deletePost(postId)
                    call.respond(
                        status = result.code,
                        message = result.data
                    )
                } catch (e: BadRequestException) {
                    return@delete
                } catch (e: Throwable) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = PostResponse(
                            success = false,
                            message = "An unexpected error has occurred, try again!"
                        )
                    )
                }
            }

        }

        route(path = "/posts") {
            get(path = "/feed") {
                try {
                    val currentUserId = call.getLongParameter("currentUserId", true)
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE
                    val result = postRepository.getFeedsPost(currentUserId, page, limit)
                    call.respond(
                        status = result.code,
                        message = result.data
                    )
                } catch (e: BadRequestException) {
                    return@get
                } catch (e: Throwable) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = PostResponse(
                            success = false,
                            message = "An unexpected error has occurred, try again!"
                        )
                    )
                }
            }

            get(path = "/{userId}") {
                try {
                    val postOwnerId = call.getLongParameter("userId")
                    val currentUserId = call.getLongParameter("currentUserId", true)
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE
                    val result = postRepository.getPostByUser(
                        postOwnerId = postOwnerId,
                        currentUserId = currentUserId,
                        pageNumber = page,
                        pageSize = limit
                    )
                    call.respond(
                        status = result.code,
                        message = result.data
                    )
                } catch (e: BadRequestException) {
                    return@get
                } catch (e: Throwable) {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = PostResponse(
                            success = false,
                            message = "An unexpected error has occurred, try again!"
                        )
                    )
                }
            }
        }


    }
}
fun Route.ChangeInPost()
{
    webSocket("/ws/posts") {
        connectedClients += this
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val receivedText = frame.readText()
                    // Handle received messages if necessary
                }
            }
        } finally {
            connectedClients -= this
        }
    }
}