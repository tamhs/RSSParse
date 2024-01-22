package com.pet.rsspaser.data.remote.api.exception

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import okio.IOException

class ApiException constructor(
    val error: ServerErrorData,
    private val forUrl: String
) : IOException() {

    override val message: String? by lazy {
        return@lazy forUrl + ": [" + error.errorCode + ":" + error.errorMessage + "]"
    }
}

interface ServerErrorData {
    val errorCode: String
    val errorMessage: String
}

@Serializable
data class DefaultServerErrorData(
    @SerialName("result")
    val result: Boolean,
    @SerialName("error")
    val error: ErrorValue
) : ServerErrorData {
    override val errorCode: String
        get() = error.errorCode
    override val errorMessage: String
        get() = error.errorMessage.firstOrNull() ?: ""

}

private object UnwrappingJsonListSerializer :
    JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonArray) {
            element
        } else {
            require(element is JsonPrimitive && element.isString) { "'message' field must be a string" }
            JsonArray(listOf(element))
        }
    }
}

@Serializable
data class ErrorValue(
    @SerialName("code")
    val errorCode: String,
    @SerialName("message")
    @Serializable(with = UnwrappingJsonListSerializer::class) val errorMessage: List<String>
)
