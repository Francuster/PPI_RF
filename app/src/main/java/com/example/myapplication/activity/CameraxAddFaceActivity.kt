
package com.example.myapplication.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.model.ImagenModel
import com.example.myapplication.service.FaceRecognition
import com.example.myapplication.service.RetrofitClient
import com.example.myapplication.utils.GraphicOverlay
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.nio.ReadOnlyBufferException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.experimental.inv

class CameraxAddFaceActivity : AppCompatActivity() {
    private var previewView: PreviewView? = null
    private var cameraSelector: CameraSelector? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var previewImg: ImageView? = null
    private var detectionTextView: TextView? = null

    private var flipX = false
    private var start = true
    private var embeddings: FloatArray? = null

    private var faceRecognition: FaceRecognition? = null

    private var loading = false
    private var userId: String = ""
    private var fromActivity: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.camerax)
        previewView = findViewById(R.id.previewView)
        previewView?.scaleType = PreviewView.ScaleType.FIT_CENTER
        graphicOverlay = findViewById(R.id.graphic_overlay)
        detectionTextView = findViewById(R.id.detection_text)

        val switchCamBtn = findViewById<ImageButton>(R.id.switch_camera)
        switchCamBtn.setOnClickListener { switchCamera() }

        // Crear un ShapeDrawable con un OvalShape
        val shapeDrawable = ShapeDrawable(OvalShape()).apply {
            // Configurar el color del borde
            paint.color = ContextCompat.getColor(applicationContext, R.color.white)
            // Configurar el grosor del borde
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 8f // Puedes ajustar el grosor del borde aquí
        }

        val ovalOverlay: View = findViewById(R.id.oval_overlay)


        // Asignar el ShapeDrawable como fondo del View
        ovalOverlay.background = shapeDrawable

        userId = intent.getStringExtra("userId").toString()
        fromActivity = intent.getStringExtra("fromActivity")

        faceRecognition = FaceRecognition()
    }

    fun onAddButtonClick(view: View) {
        if (embeddings != null) {
            val imgModel = ImagenModel("", embeddings!!, userId)
            RetrofitClient.imagenApiService.postImagenes(imgModel).enqueue(object :
                Callback<ImagenModel> {
                override fun onResponse(call: Call<ImagenModel>, response: Response<ImagenModel>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CameraxAddFaceActivity, "Se registro la imagen exitosamente.", Toast.LENGTH_SHORT).show()
                        navigateBack()
                    } else {
                        Toast.makeText(this@CameraxAddFaceActivity, "No se pudo registrar la imagen.", Toast.LENGTH_SHORT).show()
                        navigateBack()
                    }
                }

                override fun onFailure(call: Call<ImagenModel>, t: Throwable) {
                    println("Error sending Embeddings: ${t.message}")
                }
            })
        }
    }

    private fun navigateBack() {
        val fromActivity = intent.getStringExtra("fromActivity")
        val intent = when (fromActivity) {
            "RegistroUsuarioActivity" -> Intent(this, RegistroUsuarioActivity::class.java)
            "ModificacionUsuarioActivity" -> Intent(this, ModificacionUsuarioActivity::class.java)
            else -> Intent(this, RegistroUsuarioActivity::class.java) // Fallback
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    private val permissions: Unit
        get() {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION), PERMISSION_CODE)
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        for (r in grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (requestCode == PERMISSION_CODE) {
            setupCamera()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setupCamera()
        } else {
            permissions
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindAllCameraUseCases()
            } catch (e: ExecutionException) {
                Log.e(TAG, "cameraProviderFuture.addListener Error", e)
            } catch (e: InterruptedException) {
                Log.e(TAG, "cameraProviderFuture.addListener Error", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindAllCameraUseCases() {
        cameraProvider?.unbindAll()
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) return

        cameraProvider?.unbind(previewUseCase)

        val builder = Preview.Builder()
        builder.setTargetAspectRatio(AspectRatio.RATIO_4_3)
        builder.setTargetRotation(rotation)

        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(previewView?.surfaceProvider)

        try {
            cameraProvider?.bindToLifecycle(this, cameraSelector!!, previewUseCase)
        } catch (e: Exception) {
            Log.e(TAG, "Error when bind preview", e)
        }
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) return

        cameraProvider?.unbind(analysisUseCase)

        val cameraExecutor: Executor = Executors.newSingleThreadExecutor()

        val builder = ImageAnalysis.Builder()
        builder.setTargetAspectRatio(AspectRatio.RATIO_4_3)
        builder.setTargetRotation(rotation)

        analysisUseCase = builder.build()
        analysisUseCase?.setAnalyzer(cameraExecutor) { image: ImageProxy -> analyze(image) }

        try {
            cameraProvider?.bindToLifecycle(this, cameraSelector!!, analysisUseCase)
        } catch (e: Exception) {
            Log.e(TAG, "Error when bind analysis", e)
        }
    }

    @get:Throws(NullPointerException::class)
    protected val rotation: Int
        get() = previewView!!.display.rotation

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        flipX = !flipX

        cameraProvider?.unbindAll()
        startCamera()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val faceDetector = FaceDetection.getClient()

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces: List<Face> -> onSuccessListener(faces, mediaImage) }
            .addOnFailureListener { e: Exception? -> Log.e(TAG, "Face detection failure", e) }
            .addOnCompleteListener { image.close() }
    }

    // Función para mostrar la imagen en un Toast
    private fun showToastWithImage(bitmap: Bitmap) {
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)

        val toast = Toast(this)
        toast.duration = Toast.LENGTH_LONG
        toast.view = imageView
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    // Llamada a la función dentro de onSuccessListener
    // Función onSuccessListener sin rotación de la imagen
    // Dentro de onSuccessListener cuando se detecta un rostro
    private fun onSuccessListener(faces: List<Face>, mediaImage: Image) {
        if (faces.isNotEmpty()) {
            // Detener la captura de imágenes para evitar que se procesen más cuadros después de la detección de la cara.
            analysisUseCase?.clearAnalyzer()

            val imageByteArray = getImageByteArray(mediaImage)
            val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            val rotatedBitmap = rotateBitmap(bitmap, -90f) // Rotar la imagen 90 grados antihorario


            //showToastWithImage(rotatedBitmap)

            // Pasar la imagen rotada al intent anterior
            val resultIntent = Intent()
            resultIntent.putExtra("imageByteArray", bitmapToByteArray(rotatedBitmap))
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // Función para convertir Bitmap a ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
    // Función para convertir Bitmap a ByteArray


    private fun getImageByteArray(mediaImage: Image): ByteArray {
        val nv21 = YUV_420_888toNV21(mediaImage)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            mediaImage.width,
            mediaImage.height,
            null
        )

        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, mediaImage.width, mediaImage.height), 100, outputStream)
        return outputStream.toByteArray()
    }

    companion object {
        private const val TAG = "CameraxAddFaceActivity"
        private const val PERMISSION_CODE = 1001
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    private fun YUV_420_888toNV21(image: Image): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4

        val nv21 = ByteArray(ySize + uvSize * 2)

        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        var rowStride = image.planes[0].rowStride
        assert(image.planes[0].pixelStride == 1)
        var pos = 0

        if (rowStride == width) {
            yBuffer.get(nv21, 0, ySize)
            pos += ySize
        } else {
            var yBufferPos = -rowStride // not an actual position
            while (pos < ySize) {
                yBufferPos += rowStride
                yBuffer.position(yBufferPos)
                yBuffer.get(nv21, pos, width)
                pos += width
            }
        }

        rowStride = image.planes[2].rowStride
        val pixelStride = image.planes[2].pixelStride

        assert(rowStride == image.planes[1].rowStride)
        assert(pixelStride == image.planes[1].pixelStride)
        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            val savePixel = vBuffer.get(1)
            try {
                vBuffer.put(1, savePixel.inv())
                if (uBuffer.get(0) == savePixel.inv()) {
                    vBuffer.put(1, savePixel)
                    vBuffer.position(0)
                    uBuffer.position(0)
                    vBuffer.get(nv21, ySize, 1)
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining())

                    return nv21 // shortcut
                }
            } catch (ex: ReadOnlyBufferException) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            vBuffer.put(1, savePixel)
        }

        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer.get(vuPos)
                nv21[pos++] = uBuffer.get(vuPos)
            }
        }

        return nv21
    }
    }
}
