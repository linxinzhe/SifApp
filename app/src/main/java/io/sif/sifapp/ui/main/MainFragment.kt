package io.sif.sifapp.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.sif.sifapp.R

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var messageTextView: TextView
    private lateinit var photoButton: Button
    private lateinit var uploadButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        var view = inflater.inflate(R.layout.main_fragment, container, false)
        messageTextView = view.findViewById<TextView>(R.id.message);

        photoButton = view.findViewById<Button>(R.id.photo_button);
        photoButton.setOnClickListener {
            messageTextView.setText(R.string.take_photo)
        }
        uploadButton = view.findViewById<Button>(R.id.upload_button);
        uploadButton.setOnClickListener {
            messageTextView.setText(R.string.upload_photo)
        }
        return view;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

}
