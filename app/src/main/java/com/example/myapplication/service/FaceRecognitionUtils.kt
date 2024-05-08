package com.example.myapplication.service

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.media.Image
import android.text.InputType
import android.util.Pair
import android.widget.EditText
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.myapplication.utils.SimilarityClassifier.Recognition
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ReadOnlyBufferException
import kotlin.experimental.inv
import kotlin.math.sqrt


class FaceRecognitionUtils(conte: Context) {


    private var context: Context? = conte

    init {
        loadModel()
    }

    private var tfLite: Interpreter? = null
    private val registered = HashMap<String, Recognition>() //saved Faces
    private val flipX = false
    private var embeddings: Array<FloatArray> = arrayOf<FloatArray>()
    private val IMAGE_MEAN = 128.0f
    private val IMAGE_STD = 128.0f
    private val INPUT_SIZE = 112
    private val OUTPUT_SIZE = 192

    //    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }


    /** Face detection processor  */

    @OptIn(ExperimentalGetImage::class)
    public fun analizeImage(image: ImageProxy, previewWidth: Float, previewHeigth: Float) {
        if (image.image == null) return
        val inputImage = InputImage.fromMediaImage(
            image.image!!,
            image.imageInfo.rotationDegrees
        )
        val faceDetector = FaceDetection.getClient()
        faceDetector.process(inputImage)
            .addOnSuccessListener { faces: List<Face> ->
                faceFoundListener(
                    faces,
                    inputImage,
                    previewWidth,
                    previewHeigth
                )
            }
            .addOnFailureListener { e: Exception? ->
//                Log.e(
//                    CameraxActivity.TAG,"Barcode process failure",e
//                )
            }
            .addOnCompleteListener { task: Task<List<Face?>?>? -> image.close() }
    }

    private fun faceFoundListener(faces: List<Face>, inputImage: InputImage,
                                  previewWidth: Float, previewHeigth: Float) {

        var boundingBox: Rect? = null
        var name: String? = null
        val scaleX: Float = previewWidth / inputImage.height.toFloat()
        val scaleY: Float = previewHeigth / inputImage.width.toFloat()
        if (faces.size > 0) {
            // get first face detected
            val face = faces[0]

            // get bounding box of face;
            boundingBox = face.boundingBox

            // convert img to bitmap & crop img
            val bitmap = mediaImgToBmp(
                inputImage.mediaImage,
                inputImage.rotationDegrees,
                boundingBox
            )
            name = recognizeImage(bitmap)
//            if (name != null) detectionTextView.setText(name)
        } else {
        }
//        graphicOverlay.draw(boundingBox, scaleX, scaleY, name)
    }

    /** Recognize Processor  */
    private fun addFace() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Enter Name")

        // Set up the input
        val input = EditText(context!!)
        input.setInputType(InputType.TYPE_CLASS_TEXT)
        input.setMaxWidth(200)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("ADD") { dialog: DialogInterface?, which: Int ->
            //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

            //Create and Initialize new object with Face embeddings and Name.

            //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

            //Create and Initialize new object with Face embeddings and Name.
            val result = Recognition(
                "0", "", -1f
            )
            result.extra = embeddings

            registered.put(input.getText().toString(), result)
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
        }
        builder.show()
    }

    fun recognizeImage(bitmap: Bitmap): String? {

        //Create ByteBuffer to store normalized image
        val imgData =
            ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(
            intValues,
            0,
            bitmap.getWidth(),
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight()
        )
        imgData.rewind()
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixelValue = intValues[i * INPUT_SIZE + j]
                imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        //imgData is input to our model
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: MutableMap<Int, Any> = HashMap()
        embeddings =
            Array<FloatArray>(1) { FloatArray(OUTPUT_SIZE) } //output of model will be stored in this variable
        outputMap[0] = embeddings
        tfLite?.runForMultipleInputsOutputs(inputArray, outputMap) //Run model
        val distance: Float

        //Compare new face with saved Faces.
        if (registered.size > 0) {
            val nearest = findNearest(embeddings.get(0)) //Find closest matching face
            if (nearest != null) {
                val name = nearest.first
                distance = nearest.second
                return if (distance < 1.000f) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    name else "unknown"
            }
        }
        return null
    }

    //Compare Faces by distance between face embeddings
    private fun findNearest(emb: FloatArray): Pair<String, Float>? {
        var ret: Pair<String, Float>? = null
        for ((name, value) in registered.entries) {
            val knownEmb = (value.extra as Array<FloatArray>)[0]
            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = sqrt(distance.toDouble()).toFloat()
            if (ret == null || distance < ret.second) {
                ret = Pair(name, distance)
            }
        }
        return ret
    }

    /** Bitmap Converter  */
    private fun mediaImgToBmp(image: Image?, rotation: Int, boundingBox: Rect): Bitmap {
        //Convert media image to Bitmap
        val frame_bmp = toBitmap(image)

        //Adjust orientation of Face
        val frame_bmp1 = rotateBitmap(frame_bmp, rotation, flipX)

        //Crop out bounding box from whole Bitmap(image)
        val padding = 0.0f
        val adjustedBoundingBox = RectF(
            boundingBox.left - padding,
            boundingBox.top - padding,
            boundingBox.right + padding,
            boundingBox.bottom + padding
        )
        val cropped_face = getCropBitmapByCPU(frame_bmp1, adjustedBoundingBox)

        //Resize bitmap to 112,112
        return getResizedBitmap(cropped_face)
    }

    private fun getResizedBitmap(bm: Bitmap): Bitmap {
        val width = bm.getWidth()
        val height = bm.getHeight()
        val scaleWidth = 112f / width
        val scaleHeight = 112f / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    private fun getCropBitmapByCPU(source: Bitmap?, cropRectF: RectF): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            cropRectF.width().toInt(),
            cropRectF.height().toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)

        // draw background
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        paint.setColor(Color.WHITE)
        canvas.drawRect( //from  w w  w. ja v  a  2s. c  om
            RectF(0f, 0f, cropRectF.width(), cropRectF.height()),
            paint
        )
        val matrix = Matrix()
        matrix.postTranslate(-cropRectF.left, -cropRectF.top)
        canvas.drawBitmap(source!!, matrix, paint)
        if (source != null && !source.isRecycled) {
            source.recycle()
        }
        return resultBitmap
    }

    private fun rotateBitmap(
        bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean
    ): Bitmap {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, 1.0f)
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    private fun YUV_420_888toNV21(image: Image?): ByteArray {
        val width = image!!.width
        val height = image!!.height
        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)
        val yBuffer = image!!.planes[0].buffer // Y
        val uBuffer = image!!.planes[1].buffer // U
        val vBuffer = image!!.planes[2].buffer // V
        var rowStride = image!!.planes[0].rowStride
        assert(image!!.planes[0].pixelStride == 1)
        var pos = 0
        if (rowStride == width) { // likely
            yBuffer[nv21, 0, ySize]
            pos += ySize
        } else {
            var yBufferPos = -rowStride.toLong() // not an actual position
            while (pos < ySize) {
                yBufferPos += rowStride.toLong()
                yBuffer.position(yBufferPos.toInt())
                yBuffer[nv21, pos, width]
                pos += width
            }
        }
        rowStride = image!!.planes[2].rowStride
        val pixelStride = image!!.planes[2].pixelStride
        assert(rowStride == image!!.planes[1].rowStride)
        assert(pixelStride == image!!.planes[1].pixelStride)
        if (pixelStride == 2 && rowStride == width && uBuffer[0] == vBuffer[1]) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            val savePixel = vBuffer[1]
            try {
                vBuffer.put(1, savePixel.inv() as Byte)
                if (uBuffer[0] == savePixel.inv() as Byte) {
                    vBuffer.put(1, savePixel)
                    vBuffer.position(0)
                    uBuffer.position(0)
                    vBuffer[nv21, ySize, 1]
                    uBuffer[nv21, ySize + 1, uBuffer.remaining()]
                    return nv21 // shortcut
                }
            } catch (ex: ReadOnlyBufferException) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel)
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer[vuPos]
                nv21[pos++] = uBuffer[vuPos]
            }
        }
        return nv21
    }

    private fun toBitmap(image: Image?): Bitmap {
        val nv21 = YUV_420_888toNV21(image)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image!!.width, image!!.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /** Model loader  */
    private fun loadModel() {
        try {
            //model name

            tfLite = Interpreter(FileUtil.loadMappedFile(context!!,"mobile_face_net.tflite"), Interpreter.Options())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

//    @Throws(IOException::class)
//    private fun loadModelFile(activity: Activity, MODEL_FILE: String): MappedByteBuffer {
//        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        val startOffset = fileDescriptor.startOffset
//        val declaredLength = fileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
}

