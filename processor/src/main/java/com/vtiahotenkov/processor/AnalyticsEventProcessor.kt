package com.vtiahotenkov.processor

import com.vtiahotenkov.processor.AnalyticsEventProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.vtiahotenkov.processor.poets.EventNamesPoet
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AnalyticsEventProcessor : AbstractProcessor() {

    private val eventNamesPoet by lazy { EventNamesPoet() }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(AnalyticsEvent::class.java.canonicalName)

    override fun process(p0: MutableSet<out TypeElement>?, env: RoundEnvironment): Boolean {
        val elements = env.getElementsAnnotatedWith(AnalyticsEvent::class.java)
        if (elements.isEmpty())
            return false

        // structure is like <platform, <class name, event name>>
        val nameConfigs: MutableMap<String, MutableMap<String, String>> = hashMapOf()
        elements.forEach { e ->

            val annotation = e.getAnnotation(AnalyticsEvent::class.java)
            val metadata: KmClass? = e.getAnnotation(Metadata::class.java).let {
                KotlinClassMetadata.read(
                    KotlinClassHeader(
                        kind = it.kind,
                        metadataVersion = it.metadataVersion,
                        data1 = it.data1,
                        data2 = it.data2,
                        extraString = it.extraString,
                        packageName = it.packageName,
                        extraInt = it.extraInt,
                    )
                ) as? KotlinClassMetadata.Class
            }?.toKmClass()


            metadata?.let {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "===== ${it.name}")
                it.properties.forEach {
                    processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "${it.name}")
                }
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "is object: ${Flag.Class.IS_OBJECT.invoke(it.flags)}")
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "is data: ${Flag.Class.IS_DATA.invoke(it.flags)}")
            }

            if (annotation.eventName.isBlank()) {
                val message = "Event name for ${e.simpleName} is empty"
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
                error(message)
            }

            if (annotation.configs.isEmpty()) {
                val message = "At least one target for ${e.simpleName} must be configured"
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
                error(message)
            }

            annotation.configs.forEach {
                val target: String = it.target
                val overridenName: String = it.overriddenName

                val config = nameConfigs.getOrPut(target) { hashMapOf() }
                config[e.asType().toString()] = overridenName.ifBlank { annotation.eventName }
            }
        }

        val targetDir = getTargetDir()
        if (nameConfigs.isNotEmpty() && targetDir != null) {
            eventNamesPoet.compose(targetDir, nameConfigs)
        }

        return true
    }

    private fun getTargetDir() = processingEnv.options[Processor.KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let { File(it) }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}