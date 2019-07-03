package com.grio.lib.features.editor

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Required to pass large bitmaps between components (in this case, a bitmap).
 */
enum class DataHolder {
    INSTANCE;

    private var bitmap: Bitmap? = null

    companion object {

        fun hasData(): Boolean {
            return INSTANCE.bitmap != null
        }

        var data: Bitmap?
            get() {
                val obj = INSTANCE.bitmap
                INSTANCE.bitmap = null
                return obj
            }
            set(bitmap) {
                INSTANCE.bitmap = bitmap
            }

        fun toFile(context: Context): File {
            // Create file.
            val f =  File(context.cacheDir, "screenshot")
            f.createNewFile()

            // Convert.
            var bitmap = this.data!!
            var bos =  ByteArrayOutputStream()
            // PNGs ignore quality setting. Use 0.
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            var bitmapdata = bos.toByteArray()

            // Write.
            var fos = FileOutputStream(f)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            return f
        }
    }
}