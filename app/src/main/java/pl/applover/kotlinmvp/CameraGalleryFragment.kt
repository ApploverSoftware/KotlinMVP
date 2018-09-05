package pl.applover.kotlinmvp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 09/03/2018.
 */
abstract class CameraGalleryFragment<in V : BaseMvpView, P : BaseMvpPresenter<V>> : BaseFragment<V, P>(), BaseMvpView {

    private var mCurrentPhotoPath: String? = null
    private var mCurrentPhotoUri: Uri? = null
    private var mCurrentPhotoFile: File? = null
    abstract fun processChosenImage(imageFile: File?)
    private val mExecutor = Executors.newCachedThreadPool()


    fun chooseCameraOrGallery() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.choose_photo_from_header)
                .setMessage(R.string.choose_photo_message)
                .setPositiveButton(R.string.camera) { p0, _ ->
                    //Dismiss the dialog, open camera
                    //dispatchOpenCamera()
                    checkPermissionsAndOpenCamera()
                    p0.dismiss()
                }
                .setNegativeButton(R.string.gallery) { p0, _ ->
                    //Dismiss the dialog, open gallery
                    p0.dismiss()
                    checkPermissionsAndOpenGallery()
                }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mExecutor.isShutdown)
            mExecutor.shutdown()
    }

    private fun checkPermissionsAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionList = ArrayList<String>()
            val permissionReadCheck = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permissionReadCheck == PackageManager.PERMISSION_DENIED)
                permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWriteCheck = ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionWriteCheck == PackageManager.PERMISSION_DENIED)
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionList.isNotEmpty())
                requestPermissions(permissionList.toTypedArray(), REQUEST_GALLERY_PERMISSION_GALLERY)
            else dispatchOpenGallery()
        } else dispatchOpenGallery()
    }

    private fun checkPermissionsAndOpenCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionList = ArrayList<String>()

            val permissionReadCheck = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permissionReadCheck == PackageManager.PERMISSION_DENIED)
                permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)

            val permissionWriteCheck = ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionWriteCheck == PackageManager.PERMISSION_DENIED)
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            val permissionCameraCheck = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
            if (permissionCameraCheck == PackageManager.PERMISSION_DENIED)
                permissionList.add(Manifest.permission.CAMERA)

            if (permissionList.isNotEmpty())
                requestPermissions(permissionList.toTypedArray(), REQUEST_CAMERA_PERMISSIONS)
            else dispatchOpenCamera()
        } else dispatchOpenCamera()
    }

    private fun dispatchOpenGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, getString(R.string.chose_picture)), REQUEST_OPEN_GALLERY)
    }

    private fun dispatchOpenCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
            val file = createImageFile()
            file?.let {
                val photoURI = FileProvider.getUriForFile(context!!, "pl.applover.amino.provider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivityForResult(takePictureIntent, REQUEST_OPEN_CAMERA)
            }
        }
    }

    private fun createImageFile(): File? {
        val timestamp = "${System.currentTimeMillis()}"
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mCurrentPhotoFile = File.createTempFile(timestamp, ".jpg", storageDirectory)
        //mCurrentPhotoPath = image.absolutePath
        mCurrentPhotoUri = Uri.fromFile(mCurrentPhotoFile)
        mCurrentPhotoPath = Uri.fromFile(mCurrentPhotoFile).path
        return mCurrentPhotoFile
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_GALLERY_PERMISSION_GALLERY) {
            if (grantResults.isNotEmpty() && grantResults.filterNot { equals(PackageManager.PERMISSION_GRANTED) }.isEmpty())
                dispatchOpenGallery()
            else
                checkPermissionsAndOpenGallery()
        } else if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.filterNot { equals(PackageManager.PERMISSION_GRANTED) }.isEmpty())
                dispatchOpenCamera()
            else
                checkPermissionsAndOpenCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OPEN_GALLERY && resultCode == Activity.RESULT_OK) {
            mExecutor.execute(PhotoProcessor(data))
            //PhotoConverterTask().execute(data)
        } else if (requestCode == REQUEST_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {
            mExecutor.execute(CameraProcessor())
            //CameraHandlerTask().execute(mCurrentPhotoUri)
        }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSIONS = 6
        const val REQUEST_OPEN_CAMERA = 7
        const val REQUEST_GALLERY_PERMISSION_GALLERY = 5
        const val REQUEST_OPEN_GALLERY = 4
    }

    @SuppressLint("StaticFieldLeak")
    inner class PhotoConverterTask : AsyncTask<Intent, Unit, File>() {

        override fun onPostExecute(result: File?) {
            super.onPostExecute(result)
            processChosenImage(result)
        }

        override fun doInBackground(vararg p0: Intent?): File {
            p0[0]?.data.let {
                val file = createTempFile(suffix = ".jpeg")
                val bitmap = it?.getPositionedBitmap(activity!!.contentResolver)
                val fos = FileOutputStream(file)
                val stream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                fos.write(stream.toByteArray())
                fos.close()
                return file
            }
        }
    }

    inner class PhotoProcessor(private val intent: Intent?) : Runnable {
        override fun run() {
            intent?.data.let {
                val file = createTempFile(suffix = ".jpeg")
                val bitmap = it?.getPositionedBitmap(activity!!.contentResolver)
                val fos = FileOutputStream(file)
                val stream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                fos.write(stream.toByteArray())
                fos.close()
                processChosenImage(file)
            }
        }
    }

    inner class CameraProcessor : Runnable {
        override fun run() {
            val bitmap = mCurrentPhotoUri?.getPositionedCameraPhoto(activity!!.contentResolver)
            val fos = FileOutputStream(mCurrentPhotoFile)
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            fos.write(stream.toByteArray())
            fos.close()
            processChosenImage(mCurrentPhotoFile)
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class CameraHandlerTask : AsyncTask<Uri, Unit, File>() {
        override fun doInBackground(vararg p0: Uri?): File {
            val bitmap = mCurrentPhotoUri?.getPositionedCameraPhoto(activity!!.contentResolver)
            val fos = FileOutputStream(mCurrentPhotoFile)
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            fos.write(stream.toByteArray())
            fos.close()
            return mCurrentPhotoFile!!
        }

        override fun onPostExecute(result: File?) {
            super.onPostExecute(result)
            processChosenImage(result)
        }
    }
}
