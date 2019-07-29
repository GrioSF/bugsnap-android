package com.grio.lib.features.recorder

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build

import android.view.Surface

import java.io.File
import java.io.IOException

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class RecordingEncoder(var width: Int = -1,
                       var height: Int = -1,
                       var outputFile: File? = null) {
    /**
     * An instance of [MediaCodec] for accessing low level codecs.
     */
    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var isMuxerStarted = false
    private var trackIndex = -1

    var surface: Surface? = null

    fun start() {
        if (width == -1 || height == -1) {
            throw RuntimeException("Output size has not been set.")
        }

        val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_ENCODING_BITRATE)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_MEDIA_CODEC_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, DEFAULT_MEDIA_CODEC_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / DEFAULT_MEDIA_CODEC_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 0)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        try {
            encoder = MediaCodec.createEncoderByType(MIME_TYPE)
            encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder!!.setCallback(object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {}
                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    val data = codec.getOutputBuffer(index) ?: throw RuntimeException("Unable to fetch buffer for index: $index")

                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        info.size = 0
                    }

                    if (info.size != 0) {
                        if (isMuxerStarted) {
                            data.position(info.offset)
                            data.limit(info.offset + info.size)
                            muxer!!.writeSampleData(trackIndex, data, info)
                        }
                    }

                    codec.releaseOutputBuffer(index, false)
                }

                override fun onError(codec: MediaCodec, error: MediaCodec.CodecException) {
                    error.printStackTrace()
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    if (trackIndex >= 0) {
                        throw RuntimeException("Format cannot be changed.")
                    }
                    if (muxer != null) {
                        trackIndex = muxer!!.addTrack(codec.outputFormat)
                        if (trackIndex >= 0) {
                            if (!isMuxerStarted) {
                                muxer!!.start()
                                isMuxerStarted = true
                            }
                        }
                    }
                }
            })

            surface = encoder!!.createInputSurface()
            muxer = MediaMuxer(outputFile!!.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (encoder != null) {
            encoder!!.start()
            trackIndex = -1
        }
    }

    fun stop() {
        if (isMuxerStarted) {
            muxer!!.stop()
            isMuxerStarted = false
        }
        encoder?.apply {
            stop()
            release()
            encoder = null
        }
        surface?.apply {
            release()
            surface = null
        }
        muxer?.apply {
            release()
            muxer = null
        }
    }

    companion object {
        private const val MIME_TYPE = "video/avc"
        private const val DEFAULT_MEDIA_CODEC_FRAME_RATE = 30
        private const val VIDEO_ENCODING_BITRATE = 1 * 1000 * 1000
    }
}
