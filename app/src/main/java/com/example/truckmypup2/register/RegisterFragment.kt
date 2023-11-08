package com.example.truckmypup2.register

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.truckmypup2.R
import com.example.truckmypup2.databinding.FragmentRegisterBinding
import com.example.truckmypup2.login.LoginFragment

interface IRegister{
    fun onSuccess()
    fun onFail()
}
class RegisterFragment : Fragment(), IRegister {

    private lateinit var registerViewModel: RegisterViewModel
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null//iz galerije
    private var imageBitmap: Bitmap? = null//iz kamere

    private val GALLERY_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE = 1002
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        var login_button = view.findViewById(R.id.login) as Button
        login_button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                .replace(R.id.fragmentContainerView, LoginFragment())
                .commit()
        }

        binding.fromdevice.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        binding.fromcamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    imageBitmap = null
                    imageUri = data?.data
                    binding.photoFeedback.visibility = View.VISIBLE

                    binding.photoFeedback.text = resources.getText(R.string.valid_photo)
                    binding.slika.setImageURI(imageUri)
                    binding.photoFeedback.setTextColor(resources.getColor(R.color.green))
                    binding.register.isEnabled = true

                }

                CAMERA_REQUEST_CODE -> {
                    imageUri = null
                    imageBitmap = data?.extras?.get("data") as Bitmap

                    binding.photoFeedback.text = resources.getText(R.string.valid_photo)
                    binding.slika.setImageBitmap(imageBitmap)
                    binding.photoFeedback.setTextColor(resources.getColor(R.color.green))
                    binding.register.isEnabled = true

                }
            }
        } else {
            binding.photoFeedback.text = resources.getText(R.string.invalid_photo)
            binding.slika.setImageDrawable(
                requireContext().resources.getDrawable(
                    R.drawable.baseline_person_24,
                    requireContext().theme
                )
            )
            binding.photoFeedback.setTextColor(resources.getColor(R.color.red))
            binding.register.isEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory())
            .get(RegisterViewModel::class.java)
        val firstNameText = binding.firstname
        val lastNameText = binding.lastname
        val phone = binding.phone
        val email = binding.email
        val password = binding.password
        val confPassword = binding.confpassword
        val loadingProgressBar = binding.loading

        val registerBtn = binding.register

        registerViewModel.registerFormState.observe(viewLifecycleOwner,
            Observer { registerFormState ->
                if (registerFormState == null) {
                    return@Observer
                }
                registerBtn.isEnabled = registerFormState.isDataValid
                registerFormState.usernameError?.let {
                    email.error = getString(it)
                }
                registerFormState.passwordError?.let {
                    password.error = getString(it)
                }
                registerFormState.confpassError?.let {
                    confPassword.error = getString(it)
                }
                registerFormState.phoneError?.let {
                    phone.error = getString(it)
                }
                registerFormState.firstnameError?.let {
                    firstNameText.error = getString(it)
                }
                registerFormState.lastnameError?.let {
                    lastNameText.error = getString(it)
                }
            })
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                registerViewModel.registerDataChanged(
                    email.text.toString(),
                    password.text.toString(),
                    confPassword.text.toString(),
                    firstNameText.text.toString(),
                    lastNameText.text.toString(),
                    phone.text.toString()
                )
            }
        }
        firstNameText.addTextChangedListener(afterTextChangedListener)
        lastNameText.addTextChangedListener(afterTextChangedListener)
        phone.addTextChangedListener(afterTextChangedListener)
        email.addTextChangedListener(afterTextChangedListener)
        password.addTextChangedListener(afterTextChangedListener)
        confPassword.addTextChangedListener(afterTextChangedListener)

        registerBtn.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            if (imageUri != null) {
                registerViewModel.register(
                    email.text.toString(),
                    password.text.toString(),
                    confPassword.text.toString(),
                    firstNameText.text.toString(),
                    lastNameText.text.toString(),
                    phone.text.toString(),
                    imageUri!!,
                    this
                )
            } else if (imageBitmap != null) {
                registerViewModel.register(
                    email.text.toString(),
                    password.text.toString(),
                    confPassword.text.toString(),
                    firstNameText.text.toString(),
                    lastNameText.text.toString(),
                    phone.text.toString(),
                    imageBitmap!!,
                    this
                )
            } else {
                onFail()
            }
        }
    }

    private fun showRegisterFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSuccess() {
        binding.firstname.text.clear()
        binding.lastname.text.clear()
        binding.phone.text.clear()
        binding.email.text.clear()
        binding.password.text.clear()
        binding.confpassword.text.clear()
        binding.loading.visibility = View.INVISIBLE
        imageUri = null
        imageBitmap = null
        binding.photoFeedback.visibility = View.INVISIBLE
        binding.errorMsg.visibility = View.INVISIBLE
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(R.id.fragmentContainerView, LoginFragment())
            .commit()
    }

    override fun onFail() {
        binding.loading.visibility = View.INVISIBLE
        binding.errorMsg.visibility = View.VISIBLE
    }
}