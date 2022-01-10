package com.abdulmomin.face_attendance
import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.util.Log
import java.io.FileOutputStream
import com.ttv.face.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.ExecutorService
import android.content.Context
import android.graphics.BitmapFactory
import io.flutter.embedding.engine.plugins.util.GeneratedPluginRegister

class MainActivity: FlutterActivity() {

  private val CHANNEL = "turingtech"
  private var appCtx: Context?= null

  init {
    appCtx = this
  }

  override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)
    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
      call, result ->

      // <--- METHOD: Verify Against All Picture --->
       if(call.method == "setDatabase"){
        val memberList : HashMap<Int, ByteArray>? = call.argument("membersList");
        /// Should Call everytime the app starts
        /// This will set the database of faces

        FaceEngine.getInstance(this).removeFaceFeature(-1);
        for ((key, value) in memberList!!) {
          val faceFeatureInfo =
             FaceFeatureInfo(key, value)

          FaceEngine.getInstance(this).registerFaceFeature(faceFeatureInfo)
        }
        // Do verification and
        // Put the verified user id in this variable
        result.success(true);
      }

      // <--- METHOD: Verify Single Person --->
      else if(call.method == "verifySinglePerson"){
        val personImage : ByteArray? = call.argument("personImage");
        val capturedImage: ByteArray? = call.argument("capturedImage");
        
        // You can verify this 1to1 and put your result in this
        var isThisPersonVerified: Boolean = false;

        val image1 = BitmapFactory.decodeByteArray(personImage!!, 0, personImage!!.size)
        if(image1 != null) {
          val faceResults1:List<FaceResult> = FaceEngine.getInstance(this).detectFace(image1)
          if(faceResults1.count() == 1) {
            FaceEngine.getInstance(this).extractFeature(image1, true, faceResults1)

            val image2 = BitmapFactory.decodeByteArray(capturedImage!!, 0, capturedImage!!.size)
            val faceResults2:List<FaceResult> = FaceEngine.getInstance(this).detectFace(image2)
            if(faceResults2.count() == 1) {
              FaceEngine.getInstance(this).extractFeature(image2, false, faceResults2)
              val face1 = faceResults1[0].feature;
              val face2 = faceResults2[0].feature;
              val score = FaceEngine.getInstance(this).compareFeature(face1, face2);
              if(score > 0.82) {
                Log.e("ddd", "The score is")
                Log.e("ddd",score.toString())
                isThisPersonVerified = true
              }
            }
          }
        }

        result.success(isThisPersonVerified);

      }  else if(call.method == "initSDK") {
        Log.e("ddd", "init SDK!!!!");

        FaceEngine.getInstance(this).setActivation("")
        FaceEngine.getInstance(this).init(1)

        Log.e("ddd", "init ok!!!");

        result.success(true);
      } else if(call.method == "getFeature") {
        Log.e("ddd", "getFeature!!!!");

        var feat:ByteArray? = null;
        val capturedImage: ByteArray? = call.argument("image");
        val mode:Int? = call.argument("mode");
        if(capturedImage != null) {
          val image = BitmapFactory.decodeByteArray(capturedImage!!, 0, capturedImage!!.size)
          val faceResults: List<FaceResult> = FaceEngine.getInstance(this).detectFace(image)
          if (faceResults.count() == 1) {
            FaceEngine.getInstance(this).extractFeature(image, true, faceResults)

            feat = faceResults.get(0).feature

//            val faceFeatureInfo =
//              FaceFeatureInfo(1, feat)
//            FaceEngine.getInstance(this).registerFaceFeature(faceFeatureInfo)
          }
        }

        result.success(feat);
      }
      
      else {
        result.notImplemented()
      }
      
    }
  }
}