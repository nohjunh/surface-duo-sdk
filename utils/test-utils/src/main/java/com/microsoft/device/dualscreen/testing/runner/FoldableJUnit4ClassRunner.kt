/*
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 */

package com.microsoft.device.dualscreen.testing.runner

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.internal.util.AndroidRunnerParams
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.microsoft.device.dualscreen.testing.filters.DualScreenTest
import com.microsoft.device.dualscreen.testing.filters.SingleScreenTest
import com.microsoft.device.dualscreen.testing.filters.TargetDevices
import com.microsoft.device.dualscreen.testing.getDeviceModel
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod

/**
 * A specialized [AndroidJUnit4ClassRunner] that can be used together with the
 * [SingleScreenTest], [DualScreenTest] and [TargetDevices] annotations
 * to filter the tests that will run on the specified devices.
 * For example, if a test method is annotated with @TargetDevice(device = DeviceModel.SurfaceDuo),
 * that test method will run only on SurfaceDuo device or emulator, otherwise will be skipped.
 */
class FoldableJUnit4ClassRunner : AndroidJUnit4ClassRunner {
    constructor(klass: Class<*>?) : super(klass)

    constructor(klass: Class<*>?, runnerParams: AndroidRunnerParams) : super(klass, runnerParams)

    override fun runChild(method: FrameworkMethod?, notifier: RunNotifier?) {
        val description = describeChild(method)
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val currentDeviceModel = uiDevice.getDeviceModel()
        val targetDevices: TargetDevices? = getAnnotation(description, TargetDevices::class.java)
        if (targetDevices == null || targetDevices.devices.any { it == currentDeviceModel }) {
            runLeaf(methodBlock(method), description, notifier)
        } else {
            notifier?.fireTestIgnored(description)
        }
    }

    override fun validateTestMethods(errors: MutableList<Throwable>?) {
        super.validateTestMethods(errors)
        val methods = testClass.getAnnotatedMethods(Test::class.java)
        methods.forEach { method ->
            method.validateFoldableTestAnnotations(errors)
        }
    }

    private fun FrameworkMethod.validateFoldableTestAnnotations(errors: MutableList<Throwable>?) {
        val singleScreenTestAnnotation = method.getAnnotation(SingleScreenTest::class.java)
        val dualScreenTestAnnotation = method.getAnnotation(DualScreenTest::class.java)
        if (singleScreenTestAnnotation != null && dualScreenTestAnnotation != null) {
            errors?.add(
                Exception(
                    "Method " + method.name + " should be annotated with only " +
                        "@${SingleScreenTest::class.java.simpleName} or @${DualScreenTest::class.java.simpleName}"
                )
            )
        }
    }

    private fun <T : Annotation> getAnnotation(description: Description, annotationClass: Class<T>): T? {
        return description.testClass.getAnnotation(annotationClass)
            ?: description.getAnnotation(annotationClass)
    }
}