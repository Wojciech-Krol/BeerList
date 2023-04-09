package com.test.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.coroutines.*
import java.net.URL

var favouriteBeers: MutableList<Beer> = mutableListOf()
lateinit var preferencesHelper: PreferencesHelper

data class Beer(
    val id: Int,
    val name: String?,
    val tagline: String?,
    val first_brewed: String?,
    val description: String?,
    val abv: Double?,
    val ibu: Double?,
    val target_fg: Double?,
    val target_og: Double?,
    val ebc: Double?,
    val srm: Double?,
    val ph: Double?,
    val attenuation_level: Double?,
    val food_pairing: List<String>?,
    val brewers_tips: String?,
    val image_url: String? = null,
    var imageBitmap: Bitmap? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Bitmap::class.java.classLoader)
    )
    operator fun iterator(): Iterator<Beer?> {
        return object : Iterator<Beer?> {
            override fun hasNext(): Boolean {
                return false
            }
            override fun next(): Beer? {
                return null
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(tagline)
        parcel.writeString(first_brewed)
        parcel.writeString(description)
        parcel.writeDouble(abv ?: 0.0)
        parcel.writeDouble(ibu ?: 0.0)
        parcel.writeDouble(target_fg ?: 0.0)
        parcel.writeDouble(target_og ?: 0.0)
        parcel.writeDouble(ebc ?: 0.0)
        parcel.writeDouble(srm ?: 0.0)
        parcel.writeDouble(ph ?: 0.0)
        parcel.writeDouble(attenuation_level ?: 0.0)
        parcel.writeStringList(food_pairing)
        parcel.writeString(brewers_tips)
        parcel.writeString(image_url)
        parcel.writeParcelable(imageBitmap, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Beer> {
        override fun createFromParcel(parcel: Parcel): Beer {
            return Beer(parcel)
        }

        override fun newArray(size: Int): Array<Beer?> {
            return arrayOfNulls(size)
        }
    }
}

class PreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getFavouriteBeers(): MutableList<Beer> {
        val beersJson = sharedPreferences.getString("FavouriteBeers", "")
        println(beersJson)
        return if (beersJson != "") {
            gson.fromJson(beersJson, Array<Beer>::class.java).toMutableList()
        } else {
            mutableListOf()
        }
    }

    fun saveFavouriteBeers(beers: MutableList<Beer>) {
        val beersJson = gson.toJson(beers.toTypedArray())
        sharedPreferences.edit {
            putString("FavouriteBeers", beersJson)
        }
    }
}

class BeerAdapter(private var beers: MutableList<Beer>) : RecyclerView.Adapter<BeerAdapter.BeerViewHolder>() {
    lateinit var linearLayout: LinearLayout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.beer_item, parent, false)
        return BeerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeerViewHolder, position: Int) {
        val beer = beers[position]
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, BeerDetailedActivity::class.java).apply {
                putExtra("beerName", beer.name)
                putExtra("beerTagline", beer.tagline)
                putExtra("beerFirstBrewed", beer.first_brewed)
                putExtra("beerDescription", beer.description)
                putExtra("beerAbv", beer.abv)
                putExtra("beerIbu", beer.ibu)
                putExtra("beerTargetFg", beer.target_fg)
                putExtra("beerTargetOg", beer.target_og)
                putExtra("beerEbc", beer.ebc)
                putExtra("beerSrm", beer.srm)
                putExtra("beerPh", beer.ph)
                putExtra("beerAttenuationLevel", beer.attenuation_level)
                putExtra("beerFoodPairings", Gson().toJson(beer.food_pairing))
                putExtra("beerBrewersTips", beer.brewers_tips)
                putExtra("beerImageUrl", beer.image_url)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.bind(beer)
    }

    override fun getItemCount() = beers.size

    fun setBeers(beers: MutableList<Beer>?) {
        if (beers == null) {
            return
        }
        this.beers = beers
    }

    class BeerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.beer_name)
        private val imageView: ImageView = itemView.findViewById(R.id.beer_image)
        fun bind(beer: Beer) {
            nameTextView.text = beer.name
            ImageLoader.getInstance().displayImage(beer.image_url, imageView)
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var adapter: BeerAdapter
    private lateinit var searchView: SearchView
    var beers = mutableListOf<Beer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = ImageLoaderConfiguration.Builder(this)
            .build()
        ImageLoader.getInstance().init(config)
        preferencesHelper = PreferencesHelper(this)
        favouriteBeers = preferencesHelper.getFavouriteBeers()

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val homeButton =findViewById<ImageButton>(R.id.btn_home)
        homeButton.alpha = 0.0f
        homeButton.isEnabled = false
        setSupportActionBar(toolbar)
        val favouritesButton=findViewById<ImageButton>(R.id.btn_favourites)
        favouritesButton.setOnClickListener {
            val intent = Intent(this, FavouritesActivity::class.java)
            startActivity(intent)
        }

        val constraintLayout  = findViewById<ConstraintLayout>(R.id.constraint_layout)
        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.beerInfos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BeerAdapter(beers)
        recyclerView.adapter = adapter
        linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        adapter.linearLayout = linearLayout
        // Download beer data with images
        GlobalScope.launch(Dispatchers.Main) {
            beers= downloadBeerData("https://api.punkapi.com/v2/beers?page=1&per_page=80")
            loadBeerDataWithImages(beers)
        }
        searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            private var filteredBeerList: MutableList<Beer> = mutableListOf()
            override fun onQueryTextChange(newText: String?): Boolean {
                filteredBeerList = if (newText.isNullOrEmpty()) {
                    beers
                } else {
                    beers.filter { it.name?.contains(newText, ignoreCase = true) == true }
                }.toMutableList()
                println(filteredBeerList)
                adapter.setBeers(filteredBeerList)
                adapter.notifyDataSetChanged()
                return true
            }
        })
    }

    suspend fun downloadBeerData(url: String): MutableList<Beer> = withContext(Dispatchers.IO) {
        val beerUrl = URL(url)
        val beerJson = beerUrl.readText()
        val beerJsonArray = JsonParser.parseString(beerJson).asJsonArray
        beerJsonArray.map { Gson().fromJson(it, Beer::class.java) }.toMutableList()
    }

    fun loadBeerDataWithImages(beers: MutableList<Beer>?) {
        if (beers == null) {
            // handle null case
        } else {
            for (beer in beers) {
                val nameTextView = TextView(this)
                nameTextView.text = beer.name
                linearLayout.addView(nameTextView)

                // Load image with ImageLoader
                if (beer.image_url != null) {
                    val imageView = ImageView(this)
                    linearLayout.addView(imageView)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    imageView.layoutParams = layoutParams
                    ImageLoader.getInstance().displayImage(
                        beer.image_url,
                        imageView,
                        object : SimpleImageLoadingListener() {
                            override fun onLoadingComplete(
                                imageUri: String?,
                                view: View?,
                                loadedImage: Bitmap?
                            ) {
                                super.onLoadingComplete(imageUri, view, loadedImage)
                                beer.imageBitmap = loadedImage
                                adapter.notifyDataSetChanged()
                            }
                        })
                }
            }
            adapter.setBeers(beers)
            adapter.notifyDataSetChanged()
        }
    }
}
