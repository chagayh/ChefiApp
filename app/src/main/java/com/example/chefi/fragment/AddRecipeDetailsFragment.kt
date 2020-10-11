package com.example.chefi.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chefi.Chefi
import com.example.chefi.R
import com.example.chefi.activities.MainActivity
import com.example.chefi.database.AppRecipe
import com.example.chefi.database.DbUser
import com.example.chefi.track.LocationInfo
import com.example.chefi.track.LocationTracker
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [AddRecipeDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRecipeDetailsFragment : Fragment() {
    val appContext: Chefi
        get() = activity?.applicationContext as Chefi

    private val args: AddRecipeDetailsFragmentArgs by navArgs()
    private val TAG_RECIPE_FRAGMENT = "recipeFragment"
    private lateinit var textViewName: EditText
    private lateinit var textViewDirections: EditText
    private lateinit var textViewIngredients: EditText
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchStatus: Switch
    private lateinit var switchLocation: Switch
    private lateinit var imageUrl: String
    private lateinit var addBtn: TextView
    private lateinit var addIngredientBtn: ImageButton
    private lateinit var linearLayoutIngredients: LinearLayout
    private lateinit var addDirectionsBtn: ImageButton
    private lateinit var linearLayoutDirections: LinearLayout
    private lateinit var user: DbUser
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val REQUEST_CODE_GPS_PERMISSIN = 778
    private var locationTracker : LocationTracker? = null
    private var locationInfo : LocationInfo? = null
    private var locationInfoAsString : String? = null

    private val ingredientsViewsList = ArrayList<View>()
    private val directionsViewsList = ArrayList<View>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setCustomOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_recipe_details, container, false)
        imageUrl = args.imageUrl

        textViewName = view.findViewById(R.id.editTextDescription)
        textViewDirections = view.findViewById(R.id.editTextDirections)
        textViewIngredients = view.findViewById(R.id.editTextIngredients)
        switchStatus = view.findViewById(R.id.switchForTrade)
        switchLocation = view.findViewById(R.id.switchLocation)
        addBtn = view.findViewById(R.id.addRecipeBtn)

        addIngredientBtn = view.findViewById(R.id.plusIngredientsBtn)
        linearLayoutIngredients =  view.findViewById(R.id.LinearLayoutIngredients)
        addDirectionsBtn = view.findViewById(R.id.plusDirectionsBtn)
        linearLayoutDirections =  view.findViewById(R.id.LinearLayoutDirections)

        locationInfo = LocationInfo()
        locationTracker = activity?.let { LocationTracker(it, locationInfo!!) }
        createBroadcastReceiver()
        val intent = IntentFilter("tracker")
        activity?.registerReceiver(broadcastReceiver, intent)

        user = appContext.getCurrUser()!!
        Log.e("Switch", switchStatus.isChecked.toString())
        addBtn.setOnClickListener {
            Log.d("location", "location = $locationInfoAsString")
            Log.d(TAG_RECIPE_FRAGMENT, "${arrayListOf(textViewDirections.text.toString())}")
            val workId = appContext.addRecipe(
                textViewName.text.toString(),
                imageUrl,
                convertViewArrayToStringsArray(false),
                convertViewArrayToStringsArray(true),
                switchStatus.isChecked,
                locationInfoAsString
            )
            setWorkObserver(workId)
        }

        switchLocation.setOnClickListener(){
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            if (ActivityCompat.checkSelfPermission(appContext, permission)
                == PackageManager.PERMISSION_GRANTED) {
                trackCurrLocation()
            } else {
                askForPermission()
            }
        }

        addIngredientBtn.setOnClickListener {
            val tempEditText = inflater.inflate(R.layout.inflate_ingredient, container, false)
            ingredientsViewsList.add(tempEditText)
            linearLayoutIngredients.addView(tempEditText)
        }

        addDirectionsBtn.setOnClickListener {
            val tempEditText = inflater.inflate(R.layout.inflate_instruction, container, false)
            directionsViewsList.add(tempEditText)
            linearLayoutDirections.addView(tempEditText)
        }
        return view
    }

    private fun createBroadcastReceiver() {
        broadcastReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "tracker" -> {
                        if(switchLocation.isChecked && (locationInfo?._latitude == null || locationInfo?._longitude == null)){
                            switchLocation.isChecked = false
                            Toast.makeText(activity, "Track location. Please try again.", Toast.LENGTH_LONG).show()
                        }else{
                            Log.d("tracker", "gps coor = $locationInfo")
                            locationInfoAsString = locationInfo?._latitude?.let {
                                locationInfo?._longitude?.let { it1 ->
                                    convertLatiAndLongToLand(
                                        it,
                                        it1
                                    )
                                }
                            }
                            locationTracker?.stopTracking()
                        }
                    }
                }
            }
        })
    }


    private fun askForPermission() {

        val permissionsArray = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermissions(permissionsArray, REQUEST_CODE_GPS_PERMISSIN)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_GPS_PERMISSIN -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    val alertDialog = AlertDialog.Builder(activity)
                    val view = LayoutInflater.from(activity)
                        .inflate(R.layout.dialog_gps_rational, null)
                    alertDialog.setView(view)
                    alertDialog.setPositiveButton("OK") { _: DialogInterface, _: Int -> }
                    alertDialog.show()
                } else {
                    trackCurrLocation()
                }
            }
        }
    }

    private fun trackCurrLocation() {
        if (checkUnderlyingProviders()) {
            locationTracker?.startTracking()
        } else {
            switchLocation.isChecked = false
            Toast.makeText(activity, "turn GPS on first", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkUnderlyingProviders() : Boolean {
        val locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun setWorkObserver(workId: UUID) {
        appContext.getWorkManager().getWorkInfoByIdLiveData(workId).observe(viewLifecycleOwner,
            { value ->
                if (value.outputData.size() != 0) {
                    Log.d(TAG_RECIPE_FRAGMENT, "value.outputData = ${value.outputData}")
                    val gson = Gson()

                    val uid = value.outputData.getString(appContext.getString(R.string.keyUid))
                    val description =
                        value.outputData.getString(appContext.getString(R.string.keyDescription))
                    val likes = value.outputData.getInt(appContext.getString(R.string.keyLikes), 0)
                    val imageUrl =
                        value.outputData.getString(appContext.getString(R.string.keyImageUrl))
                    val commentsAsJson =
                        value.outputData.getString(appContext.getString(R.string.keyComments))
                    val directionsAsJson =
                        value.outputData.getString(appContext.getString(R.string.keyUid))
                    val ingredientsAsJson =
                        value.outputData.getString(appContext.getString(R.string.keyUid))
                    val location = value.outputData.getString(appContext.getString(R.string.keyRecipeLocation))
                    val status = value.outputData.getBoolean(
                        appContext.getString(R.string.keyStatus),
                        false
                    )
                    val timeStampAsJson =
                        value.outputData.getString(appContext.getString(R.string.keyTimestamp))

                    val stringArrayListType = object : TypeToken<ArrayList<String>>() {}.type
                    val referenceArrayListType =
                        object : TypeToken<ArrayList<DocumentReference>>() {}.type
                    val dateType = object : TypeToken<Date>() {}.type

                    val appRecipeReturned = AppRecipe(
                        uid = uid,
                        description = description,
                        likes = likes,
                        imageUrl = imageUrl,
                        comments = gson.fromJson(
                            commentsAsJson,
                            referenceArrayListType
                        ),
                        directions = arrayListOf(textViewDirections.text.toString()),
                        ingredients = arrayListOf(textViewIngredients.text.toString()),
                        status = status,
                        owner = null,   // curr user own the recipe
                        timestamp = gson.fromJson(
                            timeStampAsJson,
                            dateType
                        ),
                        location = location
                    )

//                    val recipeAsJson = value.outputData.getString(appContext.getString(R.string.keyRecipeType))
//                    val recipeType = object : TypeToken<AppRecipe>() {}.type
//
//                    val returnedRecipe = Gson().fromJson<AppRecipe>(recipeAsJson, recipeType)
                    Log.d(
                        "change_url",
                        "in recipeFragment in setWorkObserver, recipe image url = ${appRecipeReturned.imageUrl}"
                    )
                    Toast.makeText(
                        appContext,
                        "Recipe was CREATED by @${user.userName}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // TODO - maybe add a preview page
                    val action = AddRecipeDetailsFragmentDirections.actionAddRecipeDetailsToRecipe(
                        appRecipeReturned
                    )
                    view?.findNavController()?.navigate(action)
                }
            })
    }

    private fun setCustomOnBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(
                        TAG_RECIPE_FRAGMENT,
                        "delete the recipe_profile from storage and database"
                    )
                    Log.d(
                        "change_url",
                        "in setCustomOnBackPressed of recipeFragment, imageUrl = $imageUrl"
                    )
                    appContext.deleteImage(imageUrl)
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    private fun convertViewArrayToStringsArray(isIngredients: Boolean): ArrayList<String>{
        val result = ArrayList<String>()
        val iterArray = if(isIngredients) ingredientsViewsList else directionsViewsList
        if (isIngredients){
            result.add(textViewIngredients.text.toString())
        }else{
            result.add(textViewDirections.text.toString())
        }
        for (tempView in iterArray){
            val tempEditText: EditText = tempView.findViewById(R.id.editTextIngredientsTemp)
            result.add(tempEditText.text.toString())
        }
        return result
    }

    private fun convertLatiAndLongToLand(latitude: Double, longitude: Double): String?{
        val geocoder: Geocoder
        val myActivity = activity as MainActivity
        val addresses: List<Address>
        geocoder = Geocoder(myActivity, Locale.getDefault())
        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val address: String =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

//        val city: String = addresses[0].locality
//        val state: String = addresses[0].getAdminArea()
//        val country: String = addresses[0].getCountryName()
//        val postalCode: String = addresses[0].getPostalCode()
//        val knownName: String = addresses[0].getFeatureName()
        Log.e("Checker", address)
        return address
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(broadcastReceiver)
    }

}
