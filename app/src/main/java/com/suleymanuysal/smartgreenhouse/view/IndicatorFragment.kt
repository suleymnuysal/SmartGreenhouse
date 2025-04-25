package com.suleymanuysal.smartgreenhouse.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.suleymanuysal.smartgreenhouse.R
import com.suleymanuysal.smartgreenhouse.databinding.FragmentIndicatorBinding
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToLong

class IndicatorFragment : Fragment() {
    private var _binding : FragmentIndicatorBinding? = null
    private val binding get() = _binding!!
    private lateinit var temperatureRef: DatabaseReference
    private lateinit var humidityRef: DatabaseReference
    private lateinit var soilMoistureRef: DatabaseReference
    private lateinit var waterLevelRef: DatabaseReference
    private lateinit var waterPumpDisplayRef: DatabaseReference
    private lateinit var fanDisplayRef: DatabaseReference
    private lateinit var ventilationDisplayRef: DatabaseReference
    private lateinit var rainForecastDisplayRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentIndicatorBinding.inflate(inflater,container,false)

        temperatureRef = Firebase.database.reference.child("DHT").child("Temperature")
        humidityRef = Firebase.database.reference.child("DHT").child("Humidity")
        soilMoistureRef = Firebase.database.reference.child("SoilMoisture")
        waterLevelRef = Firebase.database.reference.child("Distance")
        waterPumpDisplayRef = Firebase.database.reference.child("Relays").child("WaterPump")
        fanDisplayRef = Firebase.database.reference.child("Relays").child("Fan")
        ventilationDisplayRef = Firebase.database.reference.child("Ventilation").child("Angle")
        rainForecastDisplayRef = Firebase.database.reference.child("RainDrop")

        binding.progressBar.visibility = View.VISIBLE
        binding.cardViewTemperature.visibility = View.INVISIBLE
        binding.cardViewHumidity.visibility = View.INVISIBLE
        binding.cardViewSoilMoisture.visibility = View.INVISIBLE
        binding.cardViewWaterLevel.visibility = View.INVISIBLE
        binding.cardViewWaterPumpDisplay.visibility = View.GONE
        binding.cardViewFanDisplay.visibility = View.GONE
        binding.cardViewVentilationDisplay.visibility = View.GONE
        binding.cardViewRainForecastDisplay.visibility = View.GONE


        temperatureRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressValue = snapshot.value.toString().toFloat()
                binding.circularSeekBarTemperature.progress = progressValue
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.FLOOR
                val textValue = df.format(binding.circularSeekBarTemperature.progress).toDouble()
                binding.textViewTemperatureValue.text = textValue.toString()+"\u2103"

                binding.progressBar.visibility = View.GONE
                binding.cardViewTemperature.visibility = View.VISIBLE
                binding.cardViewHumidity.visibility = View.VISIBLE
                binding.cardViewSoilMoisture.visibility = View.VISIBLE
                binding.cardViewWaterLevel.visibility = View.VISIBLE
                binding.cardViewWaterPumpDisplay.visibility = View.VISIBLE
                binding.cardViewFanDisplay.visibility = View.VISIBLE
                binding.cardViewVentilationDisplay.visibility = View.VISIBLE
                binding.cardViewRainForecastDisplay.visibility = View.VISIBLE

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        humidityRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val humidityValue = snapshot.value.toString().toFloat()
                binding.circularSeekBarHumidity.progress = humidityValue
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.FLOOR
                val textValue = df.format(binding.circularSeekBarHumidity.progress).toDouble()
                binding.textViewHumidityValue.text = "%"+textValue.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        soilMoistureRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val incomeData = snapshot.value.toString().toFloat()
                val moistureLevel: Float
                if(incomeData>4000f){
                    moistureLevel = 4000f
                }else{
                    moistureLevel = incomeData
                }
                val soilMoistureAsPer = (100f - (moistureLevel/4000f)*100f)
                binding.circularSeekBarSoilMoisture.progress = soilMoistureAsPer
                val textValue ="%"+binding.circularSeekBarSoilMoisture.progress.toInt()
                binding.textViewSoilMoistureValue.text = textValue
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        waterLevelRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val waterLevelValue = snapshot.value.toString().toInt()
                val waterLevelAsPer = 100 - ((50*waterLevelValue)/11)
                binding.circularSeekBarWaterLevel.progress = waterLevelAsPer.toFloat()
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.FLOOR
                val textValue = df.format(binding.circularSeekBarWaterLevel.progress).toDouble()
                binding.textViewWaterLevelValue.text = "%"+textValue.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        waterPumpDisplayRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value.toString().toInt()
                if(status == 0){
                    binding.textViewWaterPumpDisplayStatus.text = "OFF"
                    binding.imageView5.setImageResource(R.drawable.engine_motor_off_svgrepo_com)
                }else{
                    binding.textViewWaterPumpDisplayStatus.text = "ON"
                    binding.imageView5.setImageResource(R.drawable.engine_motor_svgrepo_com)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        fanDisplayRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value.toString().toInt()
                if(status == 0){
                    binding.textViewFanDisplayStatus.text = "OFF"
                    binding.imageViewB.setImageResource(R.drawable.fan_circled_svgrepo_com)
                }else{
                    binding.textViewFanDisplayStatus.text = "ON"
                    binding.imageViewB.setImageResource(R.drawable.fan_circled_on_svgrepo_com)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        ventilationDisplayRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value.toString().toInt()
                if(status == 0){
                    binding.textViewVentilationDisplayStatus.text = "OFF"
                }else{
                    binding.textViewVentilationDisplayStatus.text = status.toString()+"°"
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        rainForecastDisplayRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value.toString().toInt()
                if(status == 1){
                    binding.textViewRainForecastStatus.text = "Sunny"
                    binding.imageViewRain.setImageResource(R.drawable.sun_behind_cloud_svgrepo_com)

                }else{
                    binding.textViewRainForecastStatus.text = "Rainy"
                    binding.imageViewRain.setImageResource(R.drawable.rain_svgrepo_com)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}