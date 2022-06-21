package com.vtiahotenkov.processor

import com.vtiahotenkov.processor.AnalyticsEventProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.vtiahotenkov.processor.annotations.AnalyticsEvent
import com.vtiahotenkov.processor.poets.EventNamesPoet
import com.vtiahotenkov.processor.poets.PropertiesPoet
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlinx.metadata.KmClass

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AnalyticsEventProcessor : AbstractProcessor() {

    private val eventNamesPoet by lazy { EventNamesPoet() }
    private val propertiesPoet by lazy { PropertiesPoet() }

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(AnalyticsEvent::class.java.canonicalName)

    override fun process(p0: MutableSet<out TypeElement>?, env: RoundEnvironment): Boolean {
        val elements = env.getElementsAnnotatedWith(AnalyticsEvent::class.java)
        if (elements.isEmpty())
            return false

        // structure is like <platform, <class name, event name>>
        val nameConfigs: MutableMap<String, MutableMap<String, String>> = hashMapOf()
        val classesMetadata = arrayListOf<KmClass>()

        elements.forEach { e ->

            e.getAnnotation(Metadata::class.java).toKmClass()?.let {
                classesMetadata.add(it)
            } ?: run {
                processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Metadata not found for ${e.asType()}")
            }

            val annotation = e.getAnnotation(AnalyticsEvent::class.java)
            val errorMessage = validateAnnotation(annotation, e)
            if (errorMessage != null) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, errorMessage)
                error(errorMessage)
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

        if (classesMetadata.isNotEmpty() && targetDir != null) {
            propertiesPoet.compose(targetDir, classesMetadata)
        }

        return true
    }

    private fun validateAnnotation(annotation: AnalyticsEvent, annotatedElement: Element): String? =
        when {
            annotation.eventName.isBlank() ->
                "Event name for ${annotatedElement.simpleName} is blank"
            annotation.configs.isEmpty() ->
                "At least one tracking target for class ${annotatedElement.simpleName} must be configured"
            else -> null
        }

    private fun getTargetDir() = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let { File(it) }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}