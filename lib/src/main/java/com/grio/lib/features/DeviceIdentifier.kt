package com.grio.lib.features

import android.app.Activity
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.os.Build
import android.util.DisplayMetrics


class DeviceIdentifier {

    companion object {

        /**
         * Approx. bytes in one Mb.
         */
        private const val BYTES_IN_MB = 1048576L

        @JvmStatic
        fun getDeviceInformation(activity: Activity): DeviceInformation {

            // Screen resolution.
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            val screenResolution = "$width x $height"

            // Available heap.
            val runtime = Runtime.getRuntime()
            val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_IN_MB
            val maxHeapSizeInMB = runtime.maxMemory() / BYTES_IN_MB
            val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB

            // Battery
            val bm = activity.getSystemService(BATTERY_SERVICE) as BatteryManager?
            val batLevel = bm!!.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            return DeviceInformation(
                manufacturer = Build.MANUFACTURER,
                brand = Build.BRAND,
                model = Build.MODEL,
                board = Build.BOARD,
                hardware = Build.HARDWARE,
                // This will return an incorrect value above API 27
                // as getSerial() should be used, but requires permissions
                serial = Build.SERIAL,
                screenResolution = screenResolution,
                androidVersion = Build.VERSION.SDK_INT,
                androidVersionRelease = Build.VERSION.RELEASE,
                fingerprint = Build.FINGERPRINT,
                product = Build.PRODUCT,
                availableHeapInMb = availHeapSizeInMB.toInt(),
                batteryLevel = batLevel
            )
        }

    }
}

/**
 * A data class containing various information about a device.
 *
 * @property manufacturer the manufacturer of this device.
 * @property brand the brand of this device.
 * @property model the phone model.
 * @property board the device's board.
 * @property hardware information regarding kernel config.
 * @property serial the device's serial number, or "unknown" if it is not able to be retrieved.
 * @property screenResolution the devices screen resolution, in pixels, represented as a String (ex. 1080 x 1600).
 * @property androidVersion the android version the device is running.
 * @property androidVersionRelease the android version the device is running, formatted for user visibility.
 * @property fingerprint a fingerprint of the device.
 * @property product the product name.
 * @property availableHeapInMb the amount of memory available to the current app, in megabytes.
 * @property batteryLevel the current battery level as a percentage.
 */
data class DeviceInformation(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val board: String,
    val hardware: String,
    val serial: String,
    val screenResolution: String,
    val androidVersion: Int,
    val androidVersionRelease: String,
    val fingerprint: String,
    val product: String,
    val availableHeapInMb: Int,
    val batteryLevel: Int

) {

    /**
     * Formats the device information into a String with each
     * property on a new line.
     */
    fun format(): String =
        "Manufacturer: $manufacturer\n" +
                "Brand: $brand\n" +
                "Model: $model\n" +
                "Board: $board\n" +
                "Hardware: $hardware\n" +
                "Resolution: $screenResolution\n" +
                "Android Version: Android $androidVersionRelease (API $androidVersion)\n" +
                "Product: $product\n" +
                "Available Heap: $availableHeapInMb\n" +
                "Battery Percentage: $batteryLevel"
}