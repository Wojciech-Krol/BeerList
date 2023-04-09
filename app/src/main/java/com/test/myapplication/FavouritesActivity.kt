package com.test.myapplication

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener


class FavouritesActivity : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var adapter: BeerAdapter
    private lateinit var searchView: SearchView
    var beers = mutableListOf<Beer>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val favouritesButton = findViewById<ImageButton>(R.id.btn_favourites)
        favouritesButton.alpha = 0.0f
        favouritesButton.isEnabled = false

        val homeButton = findViewById<ImageButton>(R.id.btn_home)
        homeButton.setOnClickListener {
            finish()
        }

        val title=findViewById<TextView>(R.id.toolbar_title)
        title.text=getString(R.string.favourites)

        preferencesHelper = PreferencesHelper(this)
        beers = preferencesHelper.getFavouriteBeers()

        val constraintLayout  = findViewById<ConstraintLayout>(R.id.constraint_layout)
        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.beerInfos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BeerAdapter(beers)
        recyclerView.adapter = adapter
        linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        adapter.linearLayout = linearLayout

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
        loadBeerDataWithImages(beers)
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