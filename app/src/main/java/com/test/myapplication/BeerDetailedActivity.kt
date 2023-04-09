package com.test.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.nostra13.universalimageloader.core.ImageLoader
import com.google.gson.reflect.TypeToken


data class BeerProperty(val label: String, val value: String?)

class BeerDetailedActivity : AppCompatActivity() {
    fun goToFavouritesActivity(view: View) {
        val intent = Intent(this, FavouritesActivity::class.java)
        startActivity(intent)
    }
    fun goToMainActivity(view: View) {
        finish()
    }

    private fun createBeerFromIntentExtras(): Beer {
        val beerId = intent.getIntExtra("beerId", 0)
        val beerName = intent.getStringExtra("beerName") ?: ""
        val beerTagline = intent.getStringExtra("beerTagline") ?: ""
        val beerFirstBrewed = intent.getStringExtra("beerFirstBrewed") ?: ""
        val beerDescription = intent.getStringExtra("beerDescription") ?: ""
        val beerAbv = intent.getDoubleExtra("beerAbv", 0.0)
        val beerIbu = intent.getDoubleExtra("beerIbu", 0.0)
        val beerTargetFG = intent.getDoubleExtra("beerTargetFg", 0.0)
        val beerTargetOG = intent.getDoubleExtra("beerTargetOg", 0.0)
        val beerEbc = intent.getDoubleExtra("beerEbc", 0.0)
        val beerSrm = intent.getDoubleExtra("beerSrm", 0.0)
        val beerPh = intent.getDoubleExtra("beerPh", 0.0)
        val beerAttenuationLevel = intent.getDoubleExtra("beerAttenuationLevel", 0.0)
        val beerFoodPairingsJson = intent.getStringExtra("beerFoodPairings")
        val beerFoodPairings: List<String> = if (!beerFoodPairingsJson.isNullOrEmpty()) {
            Gson().fromJson(beerFoodPairingsJson, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
        val beerBrewersTips = intent.getStringExtra("beerBrewersTips") ?: ""
        val beerImageUrl = intent.getStringExtra("beerImageUrl") ?: ""
        return Beer(beerId,beerName, beerTagline, beerFirstBrewed, beerDescription,beerAbv,beerIbu,beerTargetFG,beerTargetOG,beerEbc,beerSrm,beerPh,beerAttenuationLevel,beerFoodPairings,beerBrewersTips, beerImageUrl)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.beer_clicked_activity)
        val toolbar=findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val homeButton=findViewById<ImageView>(R.id.btn_home)
        val favouritesButton=findViewById<ImageView>(R.id.btn_favourites)
        homeButton.setOnClickListener {
            goToMainActivity(it)
        }
        favouritesButton.setOnClickListener {
            goToFavouritesActivity(it)
        }
        setSupportActionBar(toolbar)
        val Title = findViewById<TextView>(R.id.toolbar_title)
        Title.text =getString(R.string.beer_details)



        val beer = createBeerFromIntentExtras()

        val nameTextView = findViewById<TextView>(R.id.beerName)
        val beerImageView=findViewById<ImageView>(R.id.beerImageView)
        nameTextView.text = beer.name
        ImageLoader.getInstance().displayImage(beer.image_url, beerImageView)

        val beerProperties = listOf(
            BeerProperty("Tagline", beer.tagline),
            BeerProperty("First brewed", beer.first_brewed),
            BeerProperty("Description", beer.description),
            BeerProperty("ABV", beer.abv.toString()),
            BeerProperty("IBU", beer.ibu.toString()),
            BeerProperty("Target FG", beer.target_fg.toString()),
            BeerProperty("Target OG", beer.target_og.toString()),
            BeerProperty("EBC", beer.ebc.toString()),
            BeerProperty("SRM", beer.srm.toString()),
            BeerProperty("PH", beer.ph.toString()),
            BeerProperty("Attenuation level", beer.attenuation_level.toString()),
            BeerProperty("Food pairings", beer.food_pairing?.joinToString(", ")),
            BeerProperty("Brewers tips", beer.brewers_tips),

        )

        val recyclerView = findViewById<RecyclerView>(R.id.beerInfos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BeerPropertiesAdapter(beerProperties)

        val fab = findViewById<FloatingActionButton>(R.id.favouritesButton)

        if (favouriteBeers.contains(beer)) {
            fab.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
        }
        fab.setOnClickListener {
            if (favouriteBeers.contains(beer)) {
                Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show()
                favouriteBeers.remove(beer)
                fab.setColorFilter(ContextCompat.getColor(this, R.color.black))
                preferencesHelper.saveFavouriteBeers(favouriteBeers)
            } else {
                favouriteBeers.add(beer)
                Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show()
                fab.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
                preferencesHelper.saveFavouriteBeers(favouriteBeers)
            }
        }

    }
    private inner class BeerPropertiesAdapter(val beerProperties: List<BeerProperty>) :
        RecyclerView.Adapter<BeerPropertiesAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
            val valueTextView: TextView = itemView.findViewById(R.id.valueTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.beer_property_item, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val beerProperty = beerProperties[position]
            holder.labelTextView.text = beerProperty.label
            holder.valueTextView.text = beerProperty.value
        }

        override fun getItemCount(): Int {
            return beerProperties.size
        }
    }
}