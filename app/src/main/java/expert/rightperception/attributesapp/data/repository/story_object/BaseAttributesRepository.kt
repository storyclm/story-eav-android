package expert.rightperception.attributesapp.data.repository.story_object

import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import ru.rightperception.storyattributes.domain.model.AttributeModel
import ru.rightperception.storyattributes.domain.model.ValidatedAttributeModel
import ru.rightperception.storyattributes.external_api.StoryAttributesService

open class BaseAttributesRepository(
    var storyAttributes: StoryAttributesService,
    private val licenseRepository: LicenseRepository
) {

    protected suspend fun set(parentId: String, attrs: List<ValidatedAttributeModel>, pathKeys: List<String>, value: Any?) {
        if (pathKeys.isNotEmpty()) {
            var lastParentId = parentId
            var i = 0
            var attr = attrs.get(pathKeys[i])
            while (attr != null && i < pathKeys.size - 1) {
                lastParentId = attr.id
                attr = attr.get(pathKeys[++i])
            }
            if (attr == null) {
                val root = mutableMapOf<String, Any?>()
                var lastObject = root
                pathKeys.subList(i, pathKeys.size - 1).forEach { key ->
                    val obj = mutableMapOf<String, Any?>()
                    lastObject[key] = obj
                    lastObject = obj
                }
                lastObject[pathKeys.last()] = value
                storyAttributes.getStorageApi().putMap(lastParentId, root)
            } else {
                val attributeModel = AttributeModel(
                    key = attr.key,
                    parentId = attr.parentId,
                    value = value
                )
                storyAttributes.getStorageApi().putAttributes(listOf(attributeModel))
            }
        }
    }

    protected suspend fun <T : Any> withLicenseId(block: suspend (attributeModel: String) -> T?): T? {
        return licenseRepository.getLicense()?.id?.let { rootParentId ->
            block(rootParentId)
        }
    }

    protected fun List<ValidatedAttributeModel>.get(key: String): ValidatedAttributeModel? {
        return firstOrNull { it.key == key }
    }
}