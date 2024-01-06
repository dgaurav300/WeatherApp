package com.example.weatherapp

import android.icu.text.SimpleDateFormat
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.URL
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    var CITY:String="Chennai,IN"
    val API_KEY:String="06c921750b9a82d8f5d1294e1586276f"

    var cityList= arrayListOf<String>("Chennai,IN","Lucknow,IN","Delhi,IN","Kolkata,IN")

    lateinit var addressSpinner:Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addressSpinner=findViewById(R.id.spinnerAddress)

        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, cityList)

        addressSpinner.adapter=adapter

        addressSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                CITY=cityList[position]
                weatherTask().execute()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }





        weatherTask().execute()



    }

    inner class weatherTask(): AsyncTask<String, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()

            findViewById<ProgressBar>(R.id.loader).visibility= View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility=View.GONE
            findViewById<TextView>(R.id.errorText).visibility=View.GONE


        }

        override fun doInBackground(vararg p0: String?): String? {
            var  response :String?
            try {
                response= URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API_KEY")

                    .readText(Charsets.UTF_8)
            }
            catch (e:Exception){
                response=null
                findViewById<TextView>(R.id.errorText).text=e.message
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt*1000)
                )
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min Temp: " + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp: " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.min_temp).text = tempMin
                findViewById<TextView>(R.id.max_temp).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
               val errorText:TextView=findViewById<TextView>(R.id.errorText)
                //errorText.text=e.message
                errorText.visibility = View.VISIBLE
            }
        }
    }
}