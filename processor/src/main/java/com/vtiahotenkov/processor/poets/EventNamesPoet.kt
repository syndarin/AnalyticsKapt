package com.vtiahotenkov.processor.poets

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import java.io.File

class EventNamesPoet {

    fun compose(targetDir: File, data: Map<String, Map<String, String>>) {
        val rootObjectBuilder = TypeSpec.objectBuilder(FILE_NAME)
        data.forEach { entry ->
            rootObjectBuilder.addProperty(composeProperty(entry.key, entry.value))
        }

        val fileBuilder = FileSpec.builder("", FILE_NAME)
        fileBuilder.addType(rootObjectBuilder.build())

        fileBuilder.build().writeTo(targetDir)
    }

    private fun composeProperty(
        trackingTargetName: String,
        classToNameMapping: Map<String, String>
    ): PropertySpec {

        val typeName = HashMap::class.java.asClassName()
            .plusParameter(
                Class::class.java.asClassName()
                    .plusParameter(WildcardTypeName.producerOf(Any::class))
            )
            .plusParameter(String::class.asClassName())

        return PropertySpec.builder(trackingTargetName, typeName, KModifier.INTERNAL)
            .initializer(
                buildCodeBlock {
                    add("hashMapOf(\n")
                    classToNameMapping.forEach { entry ->
                        add("%L::class.java to %S,\n", entry.key, entry.value)
                    }
                    add(")\n")
                })
            .build()
    }

    private companion object {
        const val FILE_NAME = "AnalyticsEventsNames"
    }
}