package com.duffrecords.godotandroidble

@ConsistentCopyVisibility
@Suppress("unused")
data class BloodPressureMeasurementStatus internal constructor(val measurementStatus: UShort) {
    /**
     * Body Movement Detected
     */
    val isBodyMovementDetected: Boolean = measurementStatus and 0x0001u > 0u

    /**
     * Cuff is too loose
     */
    val isCuffTooLoose: Boolean = measurementStatus and 0x0002u > 0u

    /**
     * Irregular pulse detected
     */
    val isIrregularPulseDetected: Boolean = measurementStatus and 0x0004u > 0u

    /**
     * Pulse is not in normal range
     */
    val isPulseNotInRange: Boolean = measurementStatus and 0x0008u > 0u

    /**
     * Improper measurement position
     */
    val isImproperMeasurementPosition: Boolean = measurementStatus and 0x0020u > 0u
}