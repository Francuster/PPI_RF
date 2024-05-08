package com.example.myapplication.service

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer

public class FaceRecognitionV2{

    // Define the input image size expected by the model
    private val INPUT_IMAGE_SIZE = 112
    private val OUTPUT_IMAGE_SIZE = 192

    private val THRESHOLD = 0.5

    private var knownEmbeddingsList: List<FloatArray> = listOf()
    private var labelsList: List<String> = listOf()




    public fun addFace(inputImage: Bitmap,label: String, context: Context){

        val recognizedFaceEmbeddings = getFaceEmbeddings(inputImage, context)

        knownEmbeddingsList = knownEmbeddingsList.plus(recognizedFaceEmbeddings)
        labelsList = labelsList.plus(label)

    }

    public fun addEmbedding(embeddings: FloatArray,label: String){

        knownEmbeddingsList = knownEmbeddingsList.plus(embeddings)
        labelsList = labelsList.plus(label)

    }

    public fun faceRecognition(inputImage: Bitmap, context: Context): LabelEmbeddingsTuple {

        val recognizedFaceEmbeddings = getFaceEmbeddings(inputImage, context)

        // 6. Match the embeddings with known faces or perform clustering to identify faces
        val label = matchFaceEmbeddings(recognizedFaceEmbeddings)
        //Label can be "Unknown"
        val embeddingsTuple =  LabelEmbeddingsTuple(label, recognizedFaceEmbeddings)
        return embeddingsTuple
    }

    private fun getFaceEmbeddings(inputImage: Bitmap, context: Context): FloatArray {
        // 1. Load the TensorFlow Lite model for face recognition
        val tfliteModel = loadModelFile("your_model.tflite", context)

        // 2. Initialize the interpreter
        val interpreter = Interpreter(tfliteModel, Interpreter.Options())

        // 3. Preprocess the input image (convert to grayscale, resize, normalize, etc.)
        val processedinputImage = preprocessImage(inputImage)

        // 4. Run inference
        val output = Array(1) { FloatArray(OUTPUT_IMAGE_SIZE) }

        interpreter.run(processedinputImage, output)

        // 5. Post-process the output (get the recognized face embeddings, labels, etc.)
        val recognizedFaceEmbeddings = output[0]

        return recognizedFaceEmbeddings
    }



    // Function to load the TensorFlow Lite model file
    private fun loadModelFile(modelPath: String, context: Context): MappedByteBuffer {
        return FileUtil.loadMappedFile(context!!,"mobile_face_net.tflite")
    }

    // Function to preprocess the input image
    private fun preprocessImage(inputImage: Bitmap): ByteBuffer {
        // Resize the input image to the expected size
        val resizedImage = Bitmap.createScaledBitmap(inputImage, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)

        // Convert the Bitmap to a ByteBuffer
        val byteBuffer = ByteBuffer.allocateDirect(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE * 3 * 4) // 3 channels (RGB) and 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE)
        resizedImage.getPixels(pixels, 0, INPUT_IMAGE_SIZE, 0, 0, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE)

        // Normalize the pixel values to range [0, 1] and write to the ByteBuffer
        for (pixelValue in pixels) {
            // Extract RGB components
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

            // Normalize and write to ByteBuffer (in RGB order)
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        // Rewind the buffer for reading
        byteBuffer.rewind()

        return byteBuffer
    }


    // Example function to match embeddings with known faces
    private fun matchFaceEmbeddings(embeddings: FloatArray): String {
        // Assuming you have a list of known face embeddings and their corresponding labels
        val knownEmbeddings: List<FloatArray> = knownEmbeddingsList
        val labels: List<String> = labelsList

        // Calculate similarity (e.g., cosine similarity) between the input embeddings and each known embedding
        val similarities = FloatArray(knownEmbeddings.size)
        for (i in knownEmbeddings.indices) {
            similarities[i] = calculateCosineSimilarity(embeddings, knownEmbeddings[i])
        }

        // Find the index of the most similar known embedding
        val maxSimilarityIndex = similarities.indices.maxByOrNull { similarities[it] } ?: -1

        // Return the label corresponding to the most similar known face
        return if (maxSimilarityIndex != -1 && similarities[maxSimilarityIndex] > THRESHOLD) {
            labels[maxSimilarityIndex]
        } else {
            "Unknown"
        }
    }

    // Example function to calculate cosine similarity between two embeddings
    private fun calculateCosineSimilarity(embeddings1: FloatArray, embeddings2: FloatArray): Float {
        var dotProduct = 0.0
        var norm1 = 0.0
        var norm2 = 0.0
        for (i in embeddings1.indices) {
            dotProduct += embeddings1[i] * embeddings2[i]
            norm1 += embeddings1[i] * embeddings1[i]
            norm2 += embeddings2[i] * embeddings2[i]
        }
        val cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2))
        return cosineSimilarity.toFloat()
    }

}



