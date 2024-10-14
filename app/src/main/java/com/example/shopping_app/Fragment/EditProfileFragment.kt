package com.example.shopping_app.Fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.shopping_app.ApiHelper.MemberInfoHelper
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.containsCharacter
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.containsSpecialCharacter
import com.example.shopping_app.Constants.REQUEST_CODE_STORAGE_PERMISSION
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.EncryptionUtils
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.MEMBER_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.ItemClickSupport
import com.example.shopping_app.Model
import com.example.shopping_app.MyApiManager
import com.example.shopping_app.R
import com.example.shopping_app.RecyclerViewHelper.BottomRecyclerViewAdapter
import com.example.shopping_app.onItemClick
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.util.Calendar

class EditProfileFragment: Fragment() {
    private val myApiService = MyApiManager.myApiService
    private lateinit var memberInfoHelper: MemberInfoHelper
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var fragmentShift: FragmentShift
    /*  About ToolBar */
    private lateinit var toolbar: Toolbar
    /* EditText */
    private lateinit var nicknameTextInputLayout: TextInputLayout
    private lateinit var editNickname: EditText
    private lateinit var phoneTextInputLayout: TextInputLayout
    private lateinit var editPhone: EditText
    private lateinit var addressTextInputLayout: TextInputLayout
    private lateinit var editAddress: EditText
    /* TextView */
    private lateinit var genderTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var bornYearTextView: TextView
    private lateinit var bornDateTextView: TextView
    /* LinearLayout */
    private lateinit var genderLinearLayout: LinearLayout
    private lateinit var countryLinearLayout: LinearLayout
    private lateinit var bornLinearLayout: LinearLayout
    /* Button */
    private lateinit var saveProfileButton: Button
    private lateinit var saveProfileButtonCardView: CardView
    /* String */
    private lateinit var currentNickname: String
    private lateinit var currentPhone: String
    private lateinit var currentGender: String
    private lateinit var currentCountry: String
    private lateinit var currentAddress: String
    private lateinit var currentBornDay: String
    private lateinit var currentBornYear: String
    private lateinit var currentBornDate: String

    /* ImageView */
    private lateinit var uploadImage: CircleImageView
    private lateinit var uploadIcon: ImageView
    private var imageUri: Uri? = null
    private var imageType: String? = null
    private var imagePath: String? = null
    private var imageName: String? = null
    /* List */
    private val countryList = listOf<String>(
        "未選択", "アイスランド", "アイルランド", "アメリカ", "イギリス", "イタリア",
        "ウクライナ", "オーストラリア", "オーストリア", "カナダ", "サウジアラビア",
        "ジョージア", "シンガポール", "スイス", "韓国", "中国", "台湾",
        "日本", "ニュージーランド", "フィンランド", "南アフリカ", "ロシア",
        "其他"
    )
    private lateinit var genderList: List<String>
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            uploadImage.setImageURI(imageUri)
            /* prepare imageName*/
            val userMail = encryptionUtils.getDecryptedData(SharedPreferencesEmailKey).toString()
            imageName = userMail.substring(0, userMail.indexOf("@"))+ userMail.substring(
                userMail.indexOf("@")+1, userMail.indexOf("."))+ "HeadImage."
            imageUri?.let {
                // 使用 contentResolver.openInputStream() 來獲取 InputStream
                requireContext().contentResolver.openInputStream(it)?.use { inputStream ->
                    // STEP 1: Create a tempFile for storing the image from scoped storage.
                    imageType = requireContext().contentResolver.getType(data?.data!!)
                    val extension = imageType!!.substring(imageType?.indexOf("/")!! + 1)
                    /* add extension name */
                    imageName += extension
//                    val directory = requireContext().externalCacheDir
//                    val tempFile = File.createTempFile(imageName!!, extension, directory)
                    val tempFile = myCreateTempFile(requireContext(), imageName!!, extension)
                    // STEP 2: copy inputStream into the tempFile
                    copyStreamToFile(inputStream, tempFile)
                    // STEP 3: get file path from tempFile for further upload process.
                    imagePath = tempFile.absolutePath
                    //showToast(imagePath!!)
                }
            }
        } else {
            CustomSnackbar.showSnackbar(view, requireContext(), "No Image Selected")
        }
        handler.post(checkProfileChange)
    }
    /* checkProfileChange Loop */
    private val handler = Handler(Looper.myLooper()!!)
    private val checkProfileChange = Runnable {
        if((editNickname.text.toString() == currentNickname || editNickname.text.isEmpty()) &&
            (editPhone.text.toString() == currentPhone || editPhone.text.isEmpty()) &&
            editAddress.text.toString() == currentAddress &&
            genderTextView.text.toString() == currentGender &&
            countryTextView.text.toString() == currentCountry &&
            bornYearTextView.text.toString() == currentBornYear &&
            bornDateTextView.text.toString() == currentBornDate &&
            imageUri == null) {
            saveProfileButton.isEnabled = false
            saveProfileButtonCardView.visibility = View.VISIBLE
        } else {
            saveProfileButton.isEnabled = true
            saveProfileButtonCardView.visibility = View.GONE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.GONE
        )
        genderList = listOf<String>(
            getString(R.string.unchoose), getString(R.string.gender_male), getString(R.string.gender_female)
        )
        findView(view)
        setToolbar()
        dataInit()
        imageClickListener()
        buttonClickListener()
        textChangedListener()
        layoutClickListener()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        nicknameTextInputLayout = view.findViewById(R.id.nicknameTextInputLayout)
        phoneTextInputLayout = view.findViewById(R.id.phoneTextInputLayout)
        addressTextInputLayout = view.findViewById(R.id.addressTextInputLayout)
        editNickname = view.findViewById(R.id.editNickname)
        editPhone = view.findViewById(R.id.editPhone)
        editAddress = view.findViewById(R.id.editAddress)
        genderTextView = view.findViewById(R.id.genderTextView)
        countryTextView = view.findViewById(R.id.countryTextView)
        bornYearTextView = view.findViewById(R.id.bornYearTextView)
        bornDateTextView = view.findViewById(R.id.bornDateTextView)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        saveProfileButtonCardView = view.findViewById(R.id.saveProfileButtonCardView)
        uploadImage = view.findViewById(R.id.uploadImage)
        uploadIcon = view.findViewById(R.id.uploadIcon)
        genderLinearLayout = view.findViewById(R.id.genderLinearLayout)
        countryLinearLayout = view.findViewById(R.id.countryLinearLayout)
        bornLinearLayout = view.findViewById(R.id.bornLinearLayout)
    }
    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.editProfile)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
        }
    }
    private fun dataInit() {
        encryptionUtils = EncryptionUtils(requireContext())
        memberInfoHelper = MemberInfoHelper()
        currentNickname = MemberInfoHelper.memberInfoMap[Model.cNickName].toString()
        currentPhone = memberInfoHelper.replaceMapString(MemberInfoHelper.memberInfoMap, Model.cPhone, "")
        currentAddress = memberInfoHelper.replaceMapString(MemberInfoHelper.memberInfoMap, Model.cAddr, "")
        currentGender = memberInfoHelper.replaceMapString(MemberInfoHelper.memberInfoMap, Model.cSex, getString(R.string.unselect))
        currentCountry = memberInfoHelper.replaceMapString(MemberInfoHelper.memberInfoMap, Model.cCountry, getString(R.string.unselect))
        currentBornDay = memberInfoHelper.replaceMapString(MemberInfoHelper.memberInfoMap, Model.cBirthday, getString(R.string.unselect))
        editNickname.setText(currentNickname)
        editPhone.setText(currentPhone)
        editAddress.setText(currentAddress)
        genderTextView.text = currentGender
        countryTextView.text = currentCountry
        if (currentBornDay == getString(R.string.unselect)) {
            currentBornYear = currentBornDay
            currentBornDate = currentBornDay
        } else {
            currentBornYear = currentBornDay.substring(0, minOf(currentBornDay.length, 4))
            currentBornDate = currentBornDay.substring(5, currentBornDay.length)
        }
        bornYearTextView.text = currentBornYear
        bornDateTextView.text = currentBornDate
        saveProfileButtonCardView.visibility = View.VISIBLE
        saveProfileButton.isEnabled = false
        if(MemberInfoHelper.memberInfoMap[Model.cImage] != null) {
            val image = MemberInfoHelper.memberInfoMap[Model.cImage].toString()
            val imageUrl = MyApiManager.baseUrl + image.substring(1, image.length)
            Glide.with(requireContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁碟緩存
                .skipMemoryCache(true) // 禁用內存緩存
                .placeholder(R.drawable.ic_person_circle_bg) // 設置占位符，當圖片加載時顯示
                .error(R.drawable.ic_person_circle_bg) // 設置加載錯誤時顯示的圖片
                .into(uploadImage)
        }
    }
    private fun buttonClickListener() {
        saveProfileButton.setOnClickListener{
            handler.removeCallbacksAndMessages(null)
            updateMemberInfo()
        }
    }
    /**  About Image Upload **/
    private fun imageClickListener() {
        uploadImage.setOnClickListener(imageOnClick())
        uploadIcon.setOnClickListener(imageOnClick())
    }
    private fun imageOnClick(): View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            when(view?.id) {
                R.id.uploadImage -> {
                    imageClickAction()
                }
                R.id.uploadIcon -> {
                    imageClickAction()
                }
            }
        }
    }
    private fun imageClickAction() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION)
        } else {

        }
        val photoPicker = Intent()
        photoPicker.action = Intent.ACTION_GET_CONTENT
        photoPicker.type = "image/*"
        photoPicker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activityResultLauncher.launch(photoPicker)
    }

    @Throws(IOException::class)
    fun myCreateTempFile(context: Context, fileName: String?, extension: String?): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return File(storageDir, "$fileName.$extension")
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    private fun prepareImgUpload(email: String) {
        val requestFile: RequestBody = File(imagePath!!).asRequestBody(imageType?.toMediaTypeOrNull())
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("image", imageName, requestFile)
        val emailRequestBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val call = myApiService.uploadImage(body, emailRequestBody)
        call.enqueue(object : Callback<Model.MemberResponse> {
            override fun onResponse(call: Call<Model.MemberResponse>, response: Response<Model.MemberResponse>) {
                if (response.isSuccessful) {
                    //上傳成功
                    CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.dataUpdateSuccess))
                    MemberInfoHelper.memberInfoMap.clear()
                    val infoList = response.body()
                    if (infoList != null) {
                        memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                            infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                    }
                    fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                } else {
                    // 上傳失敗
//                    val error = response.body()?.error
//                    val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.dataUpdateFailed))
                }
            }
            override fun onFailure(call: Call<Model.MemberResponse>, t: Throwable) {
                // 失敗
                val message: String = when (t) {
                    is SocketTimeoutException -> getString(R.string.connectTimeOut)
                    is IOException -> getString(R.string.connectFailed)
                    else -> getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, requireContext(), message)
                call.cancel()
            }
        })
    }
    /* update Information */
    private fun updateMemberInfo() {
        /** Show progress indicators **/
        val builder = AlertDialog.Builder(requireContext())
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val memberEmail =  encryptionUtils.getDecryptedData(SharedPreferencesEmailKey).toString()

        if(editPhone.text.isNotEmpty() ) {

        }
        //Prepare Data
        val newNickname = editNickname.text.toString()
        val newPhone = editPhone.text.toString()
        val newAddress = if(editAddress.text.isNotEmpty()) {
            editAddress.text.toString()
        } else {
            "Unknown"
        }
        val newGender = genderTextView.text.toString()
        val newCountry = countryTextView.text.toString()
        val requestMap: MutableMap<String, String?>

        if(bornYearTextView.text.toString() == getString(R.string.unselect)) {
            requestMap = mutableMapOf(
                Model.cEmail to memberEmail,
                Model.cNickName to newNickname,
                Model.cPhone to newPhone,
                Model.cAddr to newAddress,
                Model.cSex to newGender,
                Model.cCountry to newCountry
            )
        } else {
            val newBornDay = bornYearTextView.text.toString()+"-"+bornDateTextView.text.toString()
            requestMap = mutableMapOf(
                Model.cEmail to memberEmail,
                Model.cNickName to newNickname,
                Model.cPhone to newPhone,
                Model.cAddr to newAddress,
                Model.cSex to newGender,
                Model.cCountry to newCountry,
                Model.cBirthday to newBornDay
            )
        }
        val call  = myApiService.editMemberInfo(requestMap as Map<String, String?>)
        call.enqueue(object : Callback<Model.MemberResponse> {
            override fun onResponse(
                call: Call<Model.MemberResponse>,
                response: Response<Model.MemberResponse>
            ) {
                if (response.isSuccessful) {
                    if(imageUri != null) {
                        dialog.dismiss()
                        prepareImgUpload(memberEmail)
                    } else {
                        dialog.dismiss()
                        CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.dataUpdateSuccess))
                        MemberInfoHelper.memberInfoMap.clear()
                        val infoList = response.body()
                        if (infoList != null) {
                            memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                                infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                        }
                        fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                    }
                } else {
                    dialog.dismiss()
                    val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.dataUpdateFailed))
                }
            }
            override fun onFailure(call: Call<Model.MemberResponse>, t: Throwable) {
                dialog.dismiss()
                val message: String = when (t) {
                    is SocketTimeoutException -> getString(R.string.connectTimeOut)
                    is IOException -> getString(R.string.connectFailed)
                    else -> getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, requireContext(), message)
                call.cancel()
            }

        })
    }
    private fun textChangedListener() {
        editNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val nickname = s.toString()
                when {
                    s.isNullOrEmpty() -> {
                        nicknameTextInputLayout.error = null
                        editNickname.error = getString(R.string.nicknameEmpty)
                    }
                    containsSpecialCharacter(nickname) -> {
                        editNickname.error = null
                        nicknameTextInputLayout.error = getString(R.string.illegalCharacters)
                    }
                    charCount in 1..2 -> {
                        editNickname.error = null
                        nicknameTextInputLayout.error = getString(R.string.errorUsernameMessage)
                    }
                    nickname == currentNickname -> {
                        editNickname.error = null
                        nicknameTextInputLayout.error = null
                    }
                    else -> {
                        editNickname.error = null
                        nicknameTextInputLayout.error = null
                    }
                }
                handler.post(checkProfileChange)
            }
        })
        editPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val phoneNumber = s.toString()
                when {
                    s.isNullOrEmpty() -> {
                        phoneTextInputLayout.error = null
                        editPhone.error = getString(R.string.phoneEmpty)
                    }
                    containsCharacter(phoneNumber) -> {
                        editPhone.error = null
                        phoneTextInputLayout.error = getString(R.string.illegalCharacters)
                    }
                    charCount in 1..9 -> {
                        editPhone.error = null
                        phoneTextInputLayout.error = getString(R.string.errorPhoneMessage)
                    }
                    phoneNumber == currentPhone -> {
                        phoneTextInputLayout.error = null
                        editPhone.error = null
                    }
                    else -> {
                        phoneTextInputLayout.error = null
                        editPhone.error = null
                    }
                }
                handler.post(checkProfileChange)
            }
        })
        editAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                handler.post(checkProfileChange)
            }
        })
    }
    private fun layoutClickListener()  {
        genderLinearLayout.setOnClickListener(linearLayoutOnClickListener())
        countryLinearLayout.setOnClickListener(linearLayoutOnClickListener())
        bornLinearLayout.setOnClickListener(linearLayoutOnClickListener())
    }
    private fun linearLayoutOnClickListener() : View.OnClickListener?  {
        return View.OnClickListener {
            val view = it as? View
            when(view?.id) {
                R.id.genderLinearLayout -> {
                    showBottomDialogGender()
                }
                R.id.countryLinearLayout -> {
                    showBottomDialogCountry()
                }
                R.id.bornLinearLayout -> {
                    showBottomDialogBorn()
                }
            }
        }
    }
    private fun showBottomDialogGender() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_profile_layout_list)
        val closeTextView = dialog.findViewById<TextView>(R.id.closeTextView)
        val gender_RecyclerView = dialog.findViewById<RecyclerView>(R.id.country_RecyclerView)
        val genderList = ArrayList(genderList)
        /* RecyclerView */
        gender_RecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val bottomRecyclerViewAdapter = BottomRecyclerViewAdapter(genderList)
        gender_RecyclerView!!.adapter = bottomRecyclerViewAdapter
        ItemClickSupport.addTo(gender_RecyclerView)
        gender_RecyclerView.onItemClick{ recyclerView, position, _ ->
            dialog.dismiss()
            val itemString = (recyclerView.adapter as BottomRecyclerViewAdapter).getItem(position)
            genderTextView.text = itemString
            handler.post(checkProfileChange)
        }
        closeTextView.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    private fun showBottomDialogCountry() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_profile_layout_list)
        val closeTextView = dialog.findViewById<TextView>(R.id.closeTextView)
        val country_RecyclerView = dialog.findViewById<RecyclerView>(R.id.country_RecyclerView)
        val countryList = ArrayList(countryList)
        /* RecyclerView */
        country_RecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val bottomRecyclerViewAdapter = BottomRecyclerViewAdapter(countryList)
        country_RecyclerView!!.adapter = bottomRecyclerViewAdapter
        ItemClickSupport.addTo(country_RecyclerView)
        country_RecyclerView.onItemClick{ recyclerView, position, _ ->
            dialog.dismiss()
            val itemString = (recyclerView.adapter as BottomRecyclerViewAdapter).getItem(position)
            countryTextView.text = itemString
            handler.post(checkProfileChange)
        }
        closeTextView.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    private fun showBottomDialogBorn() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_profile_layout_born)
        val closeTextView = dialog.findViewById<TextView>(R.id.closeTextView)
        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        closeTextView.setOnClickListener{
            dialog.dismiss()
        }
        /* Date Initial */
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        var selectYear : String? = currentBornYear
        var selectDate : String? = currentBornDate
        datePicker.init(year, month, day) { _, year, month, day ->
            selectYear = year.toString()
            selectDate = memberInfoHelper.makeDateString(day, month+1)
        }
        /* When dialog dismiss */
        dialog.setOnDismissListener{
            bornYearTextView.text = selectYear
            bornDateTextView.text = selectDate
            handler.post(checkProfileChange)
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> {
                // 檢查權限請求的結果
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 用戶同意了權限請求，執行相應操作
                } else {
                    // 用戶拒絕了權限請求，執行相應操作（例如，顯示一個提示消息）
                    CustomSnackbar.showSnackbar(requireView(), requireContext(),"Need permission to get picture")
                }
                return
            }
            // 其他權限請求的處理
        }
    }
}