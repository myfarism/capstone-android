package com.example.capstonebangkitpawers.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.view.HistoryActivity
import com.example.capstonebangkitpawers.view.MapsActivity
import com.example.capstonebangkitpawers.view.ScanActivity
import java.io.File

class BerandaFragment : Fragment() {

    private lateinit var imageUri: Uri
    private val captureImage = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            val intent = Intent(activity, ScanActivity::class.java).apply {
                putExtra("imageUri", imageUri)
                activity?.grantUriPermission(
                    "com.example.capstonebangkitpawers",
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            startActivity(intent)
        }
    }

            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        val pindai: LinearLayout = view.findViewById(R.id.pindai)
        val riwayat: LinearLayout = view.findViewById(R.id.riwayat)
        val map: ImageView = view.findViewById(R.id.map)

        pindai.setOnClickListener {
            imageUri = createImageUri()
            captureImage.launch(imageUri)
        }

        riwayat.setOnClickListener {
            val intent = Intent(activity, HistoryActivity::class.java)
            startActivity(intent)
        }
        map.setOnClickListener {
            val intent = Intent(activity, MapsActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun createImageUri(): Uri {
        val file = File(requireContext().filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.capstonebangkitpawers.fileprovider",
            file
        )
    }


}
