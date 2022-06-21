package com.vtiahotenkov.processor

import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata

fun Metadata.toKmClass(): KmClass? =
    (KotlinClassMetadata.read(
        KotlinClassHeader(
            kind = kind,
            metadataVersion = metadataVersion,
            data1 = data1,
            data2 = data2,
            extraString = extraString,
            packageName = packageName,
            extraInt = extraInt,
        )
    ) as? KotlinClassMetadata.Class)?.toKmClass()