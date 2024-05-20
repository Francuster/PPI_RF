package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.service.FaceRecognition
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class FaceRecognitionTest{

    lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("com.example.myapplication", context.packageName)

    }
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2)
    }
    @Ignore
    @Test
    fun testAddFacesFromPhotosFolder() {

        val faceRecognition = FaceRecognition();

        val assetManager = context.assets
        val imagesList = mutableListOf<Bitmap>()

        try {
            // List all files in the "photos" folder
            val folderList = assetManager.list("photos") ?: emptyArray()

            // Iterate over each subfolder
            for (folderName in folderList) {
                // Get the list of files in the subfolder
                val files = assetManager.list("photos/$folderName") ?: emptyArray()

                // Iterate over each file in the subfolder
                for (fileName in files) {
                    // Open the file as InputStream
                    val inputStream = assetManager.open("photos/$folderName/$fileName")
                    // Decode the InputStream into a Bitmap
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    // Create an InputImage from the bitmap
                    val inputImage = InputImage.fromBitmap(bitmap, 0);
                    //get face from input
                    val cropedBitmap = faceDetection(inputImage)
                    if(cropedBitmap != null){
                        // Add the Bitmap to the list
                        imagesList.add(cropedBitmap)
                        faceRecognition.addFace(cropedBitmap, folderName, context)
                    }

                    // Close the InputStream
                    inputStream.close()
                }
            }

            faceRecognition.printEmbeddings();
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    // Method to load an image file as Bitmap
    private fun loadImageAsBitmap(imageFile: File): Bitmap {
        return BitmapFactory.decodeFile(imageFile.absolutePath)
    }

    private fun faceDetection(inputImage: InputImage): Bitmap? {
        var waiting = true;
        var bitmap: Bitmap? = null
        val faceDetector = FaceDetection.getClient()
        val task: Task<List<Face>> = faceDetector.process(inputImage).addOnSuccessListener {
            faces ->
            run {
                if (faces.size > 0) {
                    val face = faces[0]
                    val boundingBox = face.boundingBox

                    // convert img to bitmap & crop img
                    val faceBitmap = Bitmap.createBitmap(boundingBox.width(), boundingBox.height(), Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(faceBitmap)
                    val matrix = Matrix()
                    matrix.postTranslate(-boundingBox.left.toFloat(), -boundingBox.top.toFloat())
                    canvas.drawBitmap(inputImage.bitmapInternal!!, matrix, null)
                    bitmap = faceBitmap

                    waiting = false;


                }
                waiting = false;

            }
        }
        while (waiting){
            Thread.sleep(500)
        }

        return bitmap


    }



}
