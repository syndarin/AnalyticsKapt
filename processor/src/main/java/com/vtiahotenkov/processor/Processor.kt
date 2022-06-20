package com.vtiahotenkov.processor

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
import com.vtiahotenkov.processor.Processor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

//@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class Processor : AbstractProcessor() {

    private lateinit var messager: Messager

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        hashSetOf(PayloadEvent::class.java.canonicalName)

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
    }

    override fun process(p0: MutableSet<out TypeElement>, p1: RoundEnvironment): Boolean {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "ZZZZZ Process called")
        val elements = p1.getElementsAnnotatedWith(PayloadEvent::class.java).map { element ->

            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes should be annotated")
                return false
            }

//            messager.printMessage(Diagnostic.Kind.NOTE, "${element.javaClass}")
            element
        }

        val targetFile = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let {
            File(it)
        } ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find target directory for generated files")
            return false
        }

        processElements(elements, targetFile)

        return true
    }

    private fun processElements(elements: List<Element>, targetDir: File) {
        if (elements.isEmpty()) {
            return
        }

        val file = FileSpec.builder("", "MapSerializers")
            .addImport("com.vtiahotenkov.processor", "MapSerializer")
            .addFunction(createGetMapperFunction())
            .addProperty(declareSerializersMap(elements))
            .build()

        file.writeTo(targetDir)
    }

    private fun createGetMapperFunction(): FunSpec {
        val genericType = TypeVariableName("T")
        val argName = "t"

        return FunSpec.builder("getMapper")
            .addTypeVariable(genericType)
            .addParameter(ParameterSpec.builder(argName, genericType).build())
            .returns(MapSerializer::class.asClassName().plusParameter(genericType))
            .addStatement("require($argName != null) { %S }", "Null values aren't allowed")
            .addStatement("val clazz = $argName!!::class.java")
            .addStatement("return (mapping[clazz] as? MapSerializer<T>) ?: error( %P )", "Mapper for \${clazz} not found")
            .build()
    }

    private fun declareSerializersMap(elements: List<Element>): PropertySpec {
        val mapTypeName = HashMap::class.asClassName()
            .plusParameter(Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(Any::class)))
            .plusParameter(MapSerializer::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(Any::class)))

        if (elements.isEmpty())
            return PropertySpec.builder("mapping", mapTypeName, KModifier.PRIVATE).initializer("hashMapOf()").build()

        return PropertySpec.builder("mapping", mapTypeName, KModifier.PRIVATE)
            .initializer(
                buildCodeBlock {
                    add("hashMapOf(\n")
                    elements.forEach { element ->
                        add("%L::class.java to %T<%L> { v -> hashMapOf(\n", element.toString(), MapSerializer::class.java, element.toString())
                        processingEnv.elementUtils.getAllMembers(element as TypeElement)
                            .filter { it.kind == ElementKind.FIELD }
                            .forEach {
                                add("\t%S to v.%L.toString(),\n", it.simpleName, it.simpleName)
                            }
                        add(")},\n")
                    }
                    add("\n)")
                }
            )
            .build()
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}