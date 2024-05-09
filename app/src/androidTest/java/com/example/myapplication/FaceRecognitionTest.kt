package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.service.FaceRecognitionV2
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

        val faceRecognitionV2 = FaceRecognitionV2();

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
                    // Add the Bitmap to the list
                    imagesList.add(bitmap)
                    faceRecognitionV2.addFace(bitmap, folderName, context)
                    // Close the InputStream
                    inputStream.close()
                }
            }

            faceRecognitionV2.printEmbeddings();
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    // Method to load an image file as Bitmap
    private fun loadImageAsBitmap(imageFile: File): Bitmap {
        return BitmapFactory.decodeFile(imageFile.absolutePath)
    }



    fun loadImagesFromAssets2(context: Context): List<Pair<Bitmap, String>> {
        val assetManager = context.assets
        val imagesList = mutableListOf<Pair<Bitmap, String>>()

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
                    // Add the Bitmap and subfolder name to the list as a pair
                    imagesList.add(Pair(bitmap, folderName))
                    // Close the InputStream
                    inputStream.close()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return imagesList
    }



}
