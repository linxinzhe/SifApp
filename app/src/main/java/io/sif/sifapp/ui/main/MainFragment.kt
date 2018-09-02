package io.sif.sifapp.ui.main

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import io.sif.sifapp.R


class MainFragment : Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_PICK = 2

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var photoButton: Button
    private lateinit var selectButton: Button
    private lateinit var uploadButton: Button

    private lateinit var imageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        photoButton = view.findViewById<Button>(R.id.photo_button);
        photoButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
        selectButton = view.findViewById<Button>(R.id.select_button);
        selectButton.setOnClickListener {
            Toast.makeText(activity, R.string.multi_select, Toast.LENGTH_SHORT).show()

            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        }

        imageView = view.findViewById<ImageView>(R.id.imageView);
        uploadButton = view.findViewById<Button>(R.id.upload_button);
        uploadButton.setOnClickListener {

        }

        return view;
    }

    private var imageEncoded: String = ""
    var imagesEncodedList: MutableList<String>? = mutableListOf()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && null != data) {
            Log.d("", "")
            if (data.data != null) {
                val uri = data.data
                val bitmap = getBitmapFromUri(uri)
                imageView.setImageBitmap(bitmap)
            } else {
                val clipData = data.clipData
                val count = clipData.itemCount
                for (i in 0 until count) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri

                    val bitmap = getBitmapFromUri(uri)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = activity?.contentResolver?.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor

        //TODO Resize BitMap
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()

        return image
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

}
