package com.hasanbilgin.artbookkotlin.view

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.room.Database
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.hasanbilgin.artbookkotlin.view.ArtFragmentDirections
import com.hasanbilgin.artbookkotlin.R
import com.hasanbilgin.artbookkotlin.databinding.FragmentArtBinding
import com.hasanbilgin.artbookkotlin.model.ArtModel
import com.hasanbilgin.artbookkotlin.roomdb.ArtDao
import com.hasanbilgin.artbookkotlin.roomdb.ArtDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class ArtFragment : Fragment() {

    var selectedPicture: Uri? = null
    var selectedBitmap: Bitmap? = null
    private var _binding: FragmentArtBinding? = null

    //Bu özellik yalnızca onCreateView ve onDestroyView arasında geçerlidir.
    private val binding get() = _binding!!

    //galeri açma intenti
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    //izin için
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val mDisposable = CompositeDisposable()
    private lateinit var artDatabase: ArtDatabase
    private lateinit var artDao: ArtDao
    var artFromMain: ArtModel? = null


    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentArtBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        artDatabase = Room.databaseBuilder(requireContext(), ArtDatabase::class.java, "Arts")
            .build()
        artDao = artDatabase.artDao()

        getData()
        registerLauncher()
        buttonOnClick()
        //bölede verilebiliyo
        binding.deleteButton.setOnClickListener { delete(view) }

    }

    private fun getData() {
//        val info = ArtFragmentArgs.fromBundle(arguments!!).info;//aynı
        arguments?.let {
            val info = ArtFragmentArgs.fromBundle(it).info

            if (info.equals("new")) {
                binding.saveButton.visibility = View.VISIBLE
                binding.artNameEdittext.setText("")
                binding.artistNameEdittext.setText("")
                binding.yearEdittext.setText("")

                val selectedImageBackground = BitmapFactory.decodeResource(context?.resources, R.drawable.image)
                binding.imageView.setImageBitmap(selectedImageBackground)

                //kaynakta ise almakktır
                //binding.imageView.setImageResource(R.drawable.image)
            } else if (info.equals("old")) {
                binding.deleteButton.visibility = View.VISIBLE
                binding.updateButton.visibility = View.VISIBLE

                val selectedId = ArtFragmentArgs.fromBundle(it).id
                mDisposable.add(artDao.getArtById(selectedId).
                subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseWithOldArt))
            }
        }
    }

    private fun handleResponseWithOldArt(artModel: ArtModel) {
        artFromMain = artModel
        binding.artNameEdittext.setText(artModel.artname)
        binding.artistNameEdittext.setText(artModel.artistname)
        binding.yearEdittext.setText(artModel.year)
        artModel.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.imageView.setImageBitmap(bitmap)
        }
    }


    private fun buttonOnClick() {
        saveOnClick()
        updateOnClick()
        selectImageOnClick()
    }

    //boyutunu width ve height küçüklççek
    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) { //Landscape//yatay
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else { //portrait//dikey
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun saveOnClick() {
        binding.saveButton.setOnClickListener {
            var artName = binding.artNameEdittext.text.toString()
            var artistName = binding.artistNameEdittext.text.toString()
            var year = binding.yearEdittext.text.toString()

            if (selectedBitmap != null) {
                val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

                val outputStream = ByteArrayOutputStream()
                smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                val byteArray = outputStream.toByteArray()

                var artModel = ArtModel(artName, artistName, year, byteArray)

                mDisposable.add(artDao.insert(artModel)
                    //abone oluncak olan yeri söliyoruz yani ıo thread ulaşçaz
                    .subscribeOn(Schedulers.io())
                    //gözlemnen yeri giriyoruz
                    .observeOn(AndroidSchedulers.mainThread())
                    //sonunda ne yapılcak
                    .subscribe(this::handleResponse))

            }
        }
    }

    //gelen cevabı ele genelde bu terim kullanılırmış
    private fun handleResponse() {
        val action = ArtFragmentDirections.actionArtFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }


    private fun updateOnClick() {
        binding.updateButton.setOnClickListener {
            artFromMain?.let {
                var artName = binding.artNameEdittext.text.toString()
                var artistName = binding.artistNameEdittext.text.toString()
                var year = binding.yearEdittext.text.toString()

                if (selectedBitmap != null) {
                    val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

                    val outputStream = ByteArrayOutputStream()
                    smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                    val byteArray = outputStream.toByteArray()

                    mDisposable.add(artDao.updateQuery(it.id, artName, artistName, year, byteArray)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse))
                } else {
                    mDisposable.add(artDao.updateQuery(it.id, artName, artistName, year, it.image)
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse))
                }
            }
        }
    }

    private fun delete(view: View) {

        artFromMain?.let {
            mDisposable.add(artDao.delete(it).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
        }
    }

    private fun selectImageOnClick() {
        binding.imageView.setOnClickListener {
            //fragmentte toast kullanımı
//           Toast.makeText(activity, "Its a toast!", Toast.LENGTH_SHORT).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //android 33+ READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) { //izin alma mantığını kullanııya göstereyimmi ? android kendi belirler
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, android.Manifest.permission.READ_MEDIA_IMAGES)) { //rationale
                        Snackbar.make(view!!, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give Permission", View.OnClickListener { //request permission
                                permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                            }).show()
                    } else { //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery) ///intent
                }
            } else { //android 33+ READ_EXTERNAL_STOREAGE
                //Manifest->androidden
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //izin alma mantığını kullanııya göstereyimmi ? android kendi belirler
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)) { //rationale
                        Snackbar.make(view!!, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give Permission", View.OnClickListener { //request permission
                                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()
                    } else { //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery) ///intent
                }
            }
        }
    }

    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)
                    // küçüklte işlemi yapçağımız için bitmapten ilerlicez
                    if (imageData != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
//                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageData)
                                val source = ImageDecoder.createSource(requireActivity().contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else { //contentResolver ile this@ArtActivity.contentResolver aynı kullanılabiliyo
                                selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result -> //izin verildiyse
            if (result) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else { //permission denied
                Toast.makeText(activity, "Permission needed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //ekran kapandığında çalışan method
    override fun onDestroyView() {
        super.onDestroyView()
        //bunu notalmak lazım
        _binding = null
        //temizleme yapıcaktır //ram temizliği
        mDisposable.clear()
    }

}
