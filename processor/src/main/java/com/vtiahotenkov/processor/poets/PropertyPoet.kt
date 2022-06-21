package com.vtiahotenkov.processor.poets

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.vtiahotenkov.processor.serializers.MapSerializer
import java.io.File
import kotlinx.metadata.KmClass

class PropertiesPoet {

    fun compose(targetDir: File, classesMetadata: List<KmClass>) {

        val file = FileSpec.builder("", "MapSerializers")
            .addFunction(createGetMapperFunction())
            .addProperty(declareSerializersMap(classesMetadata))
            .build()

        file.writeTo(targetDir)
    }

    private fun createGetMapperFunction(): FunSpec {
        val genericType = TypeVariableName("T")
        val argName = "t"

        return FunSpec.builder("getMapper")
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class.java)
                    .addMember("%S, %S", "UNCHECKED_CAST", "UNNECESSARY_NOT_NULL_ASSERTION")
                    .build()
            )
            .addTypeVariable(genericType)
            .addParameter(ParameterSpec.builder(argName, genericType).build())
            .returns(MapSerializer::class.asClassName().plusParameter(genericType).copy(nullable = true))
            .addStatement("require($argName != null) { %S }", "Null values aren't allowed")
            .addStatement("val clazz = $argName!!::class.java")
            .addStatement("return mapping[clazz] as? %T<T>", MapSerializer::class.java)
            .build()
    }

    private fun declareSerializersMap(classesMetadata: List<KmClass>): PropertySpec {
        val mapTypeName = HashMap::class.asClassName()
            .plusParameter(Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(Any::class)))
            .plusParameter(MapSerializer::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(Any::class)))

        return PropertySpec.builder("mapping", mapTypeName, KModifier.PRIVATE)
            .initializer(
                buildCodeBlock {
                    add("hashMapOf(\n")
                    classesMetadata.forEach { kmClass ->
                        val fullName = kmClass.name.replace('/', '.')
                        add("%L::class.java to %T<%L> { v -> hashMapOf(\n", fullName, MapSerializer::class.java, fullName)
                        kmClass.properties.forEach {
                            add("\t%S to v.%L.toString(),\n", it.name, it.name)
                        }
                        add(")},\n")
                    }
                    add("\n)")
                }
            )
            .build()
    }

    /*metadata?.let {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "===== ${it.name}")
                it.properties.forEach {
                    processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "${it.name}")
                }
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "is object: ${Flag.Class.IS_OBJECT.invoke(it.flags)}")
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "is data: ${Flag.Class.IS_DATA.invoke(it.flags)}")
            }*/
}