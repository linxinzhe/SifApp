package io.sif.sifapp.ui.main

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.sif.sifapp.AppConfig
import io.sif.sifapp.R
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class MainFragment : Fragment() {

    val TAG = MainFragment.javaClass.simpleName

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_PICK = 2

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var resetButton: Button
    private lateinit var photoButton: Button
    private lateinit var selectButton: Button
    private lateinit var uploadButton: Button
    private lateinit var viewMapsButton: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var imageList: MutableList<Bitmap> = mutableListOf()

    private val client = OkHttpClient()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        resetButton = view.findViewById<Button>(R.id.reset_button);
        resetButton.setOnClickListener {
            imageList.clear()
            adapter.notifyDataSetChanged()
        }

        photoButton = view.findViewById<Button>(R.id.photo_button);
        photoButton.setOnClickListener {
            takePhotos()
        }
        selectButton = view.findViewById<Button>(R.id.select_button);
        selectButton.setOnClickListener {
            selectPhotots();
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView);
        recyclerView.setLayoutManager(GridLayoutManager(activity, 3));
        adapter = ImageAdapter(activity!!.baseContext, imageList)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL));

        uploadButton = view.findViewById<Button>(R.id.upload_button);
        uploadButton.setOnClickListener {
            uploadPhotos()
        }

        viewMapsButton = view.findViewById<Button>(R.id.view_maps_button);
        viewMapsButton.setOnClickListener {
            viewMaps()
        }


        return view;
    }

    private fun takePhotos() {
        val takePictureIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun selectPhotots() {
        Toast.makeText(activity, R.string.multi_select, Toast.LENGTH_SHORT).show()

        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun uploadPhotos() {
        for ((index, image) in imageList.withIndex()) {

            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val toByteArray = baos.toByteArray()

            MultipartBody.create(MediaType.parse("image/jpeg"), toByteArray)

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)

            requestBody.addFormDataPart("file", index.toString() + ".jpg", RequestBody.create(MediaType.parse("image/*"), toByteArray))

            val request = Request.Builder()
                    .url(AppConfig.ENDPOINT + "/map/imgUpload")
                    .post(requestBody.build())
                    .build();

            val response = client.newCall(request).enqueue(object : Callback {
                @SuppressLint("CheckResult")
                override fun onFailure(call: Call, e: IOException) {
                    Observable.just("").observeOn(AndroidSchedulers.mainThread()).subscribe {
                        Log.e(TAG, e.message)
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

                @SuppressLint("CheckResult")
                override fun onResponse(call: Call, response: Response) {
                    val str = response.body()?.string()
                    Observable.just("").observeOn(AndroidSchedulers.mainThread()).subscribe {
                        Log.d(TAG, str)
                        Toast.makeText(activity, str, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }


    }

    private fun viewMaps() {
        val url = AppConfig.ENDPOINT + "/reconstruction.html#file=/data/berlin/reconstruction.meshed.json"
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
    }


    class ImageAdapter(private var context: Context, private var imageList: MutableList<Bitmap>) : RecyclerView.Adapter<ImageAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false))
        }

        override fun getItemCount(): Int {
            return imageList.size;
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.iv.setImageBitmap(imageList.get(position));
        }

        class MyViewHolder(view: View) : ViewHolder(view) {
            var iv: ImageView

            init {
                iv = view.findViewById(R.id.imageView) as ImageView
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && null != data) {
            Log.d("", "")
            if (data.data != null) {
                val uri = data.data
                val bitmap = getBitmapFromUri(uri)
                imageList.add(bitmap)
            } else {
                val clipData = data.clipData
                val count = clipData.itemCount
                for (i in 0 until count) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri

                    val bitmap = getBitmapFromUri(uri)
                    imageList.add(bitmap)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }


    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = activity?.contentResolver?.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor

        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)

        val resized = Bitmap.createScaledBitmap(image, 3 * 400, 4 * 400, true)

        parcelFileDescriptor?.close()

        return resized
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

}
