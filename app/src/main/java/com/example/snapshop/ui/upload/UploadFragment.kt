package com.example.snapshop.ui.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.snapshop.R
import com.example.snapshop.databinding.FragmentUploadBinding
import com.example.snapshop.ui.adapter.SelectedImageAdapter
import com.example.snapshop.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val uploadViewModel: UploadViewModel by viewModels()
    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var selectedImageAdapter: SelectedImageAdapter

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImageUris.clear()
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        selectedImageUris.add(data.clipData!!.getItemAt(i).uri)
                    }
                } else {
                    data?.data?.let { selectedImageUris.add(it) }
                }
                selectedImageAdapter.submitList(selectedImageUris.toList())
                uploadViewModel.setSelectedImages(selectedImageUris.toList())
                binding.tvImageCount.text = "${selectedImageUris.size} image(s) selected"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageRecycler()
        setupClickListeners()
        observeUploadState()
    }

    private fun setupImageRecycler() {
        selectedImageAdapter = SelectedImageAdapter()
        binding.rvSelectedImages.adapter = selectedImageAdapter
    }

    private fun setupClickListeners() {
        binding.btnSelectImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            imagePickerLauncher.launch(intent)
        }

        binding.btnUpload.setOnClickListener {
            val title            = binding.etTitle.text.toString().trim()
            val shortDescription = binding.etShortDescription.text.toString().trim()
            val description      = binding.etDescription.text.toString().trim()
            val priceStr         = binding.etPrice.text.toString().trim()
            val uploaderName     = binding.etUploaderName.text.toString().trim()
            val uploaderContact  = binding.etUploaderContact.text.toString().trim()

            if (title.isEmpty() || shortDescription.isEmpty() || description.isEmpty() ||
                priceStr.isEmpty() || uploaderName.isEmpty() || uploaderContact.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPhoneNumber(uploaderContact)) {
                Toast.makeText(requireContext(), "Enter Valid Contact", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null) {
                Toast.makeText(requireContext(), "Enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadViewModel.uploadProduct(
                title, shortDescription, description, price, uploaderName, uploaderContact
            )
        }
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.trim().matches(Regex("^[0-9]{10}$"))
    }

    private fun observeUploadState() {
        uploadViewModel.uploadState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnUpload.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpload.isEnabled = true
                    clearForm()

                    Toast.makeText(
                        requireContext(),
                        "✅ Product uploaded successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAdded && _binding != null) {
                            findNavController().navigate(
                                R.id.action_uploadFragment_to_homeFragment
                            )
                        }
                    }, 1500)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnUpload.isEnabled = true
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etShortDescription.text?.clear()
        binding.etDescription.text?.clear()
        binding.etPrice.text?.clear()
        binding.etUploaderName.text?.clear()
        binding.etUploaderContact.text?.clear()
        selectedImageUris.clear()
        selectedImageAdapter.submitList(emptyList())
        binding.tvImageCount.text = "0 image(s) selected"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}