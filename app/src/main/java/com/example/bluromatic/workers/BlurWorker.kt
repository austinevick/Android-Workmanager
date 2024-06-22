package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"
class BlurWorker(ctx:Context,params: WorkerParameters):CoroutineWorker(ctx,params) {
    override suspend fun doWork(): Result {

        val resoureUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL,1)

        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

      return withContext(Dispatchers.IO) {
              delay(DELAY_TIME_MILLIS)
          return@withContext try {
              require(!resoureUri.isNullOrBlank()){
                  val errorMessage =
                      applicationContext.resources.getString(R.string.invalid_input_uri)
                  Log.e(TAG, errorMessage)
              }
              val resolver = applicationContext.contentResolver
              val picture = BitmapFactory.decodeStream(
                  resolver.openInputStream(Uri.parse(resoureUri))
              )
              val output = blurBitmap(picture, blurLevel)
              val outputUri = writeBitmapToFile(applicationContext, output)
              val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
              makeStatusNotification(
                  "Output is $outputUri",
                  applicationContext
              )

              Result.success(outputData)
          } catch (throwable: Throwable) {
              Log.e(TAG, "Error occured", throwable)
              Result.failure()
          }
      }
    }
}