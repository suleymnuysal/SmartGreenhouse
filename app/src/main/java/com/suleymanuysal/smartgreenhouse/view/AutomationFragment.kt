package com.suleymanuysal.smartgreenhouse.view

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.suleymanuysal.smartgreenhouse.R
import com.suleymanuysal.smartgreenhouse.databinding.AutomationAlertDialogBinding
import com.suleymanuysal.smartgreenhouse.databinding.FragmentAutomationBinding
import java.math.RoundingMode
import java.text.DecimalFormat

class AutomationFragment : Fragment() {
    private var _binding: FragmentAutomationBinding? = null
    private val binding get() = _binding!!
    private lateinit var automationTemperature:DatabaseReference
    private lateinit var automationSoilMoisture:DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAutomationBinding.inflate(inflater,container,false)

        automationTemperature = Firebase.database.reference.child("Automation").child("Temperature")
        automationSoilMoisture = Firebase.database.reference.child("Automation").child("SoilMoisture")

        automationTemperature.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value.toString().toFloat()
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.FLOOR
                val textValue = df.format(value).toDouble()
                binding.textViewTemperatureAutomationValue.text = textValue.toString()+"\u2103"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        automationSoilMoisture.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val incomeValue = snapshot.value.toString().toFloat()
                val value = (100f - (incomeValue/4000f)*100f)
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.FLOOR
                val textValue = df.format(value).toDouble()
                binding.textViewSoilMoistureAutomationValue.text = "%"+textValue.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
            }

        })

        binding.textViewTemperatureAutomationValue.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.apply {
                val view = layoutInflater.inflate(R.layout.automation_alert_dialog,null)
                setView(view)
                setTitle("Set temperature value")
                setMessage("Optimal temperature value should be 28℃")
                setPositiveButton("Set",object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val editText = view.findViewById<EditText>(R.id.editTextValue)
                        if(editText.text.isNotEmpty()) {
                            val value = editText.text.trim().toString().toFloat()
                            if(value<=50f) {
                                automationTemperature.setValue(value).addOnSuccessListener {
                                    binding.textViewTemperatureAutomationValue.text =
                                        value.toString() + "\u2103"
                                    Toast.makeText(context, "The value is set", Toast.LENGTH_SHORT)
                                        .show()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "" + e.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }else{
                                Toast.makeText(context, " The value cannot be bigger than 50 ", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
                        }

                    }

                })
                show()

            }

        }

        binding.textViewSoilMoistureAutomationValue.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.apply {
                val view = layoutInflater.inflate(R.layout.automation_alert_dialog,null)
                setView(view)
                setTitle("Set soil moisture value")
                setMessage("Optimal  soil moisture value should be %60")
                setPositiveButton("Set",object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val editText = view.findViewById<EditText>(R.id.editTextValue)
                        if(editText.text.isNotEmpty()) {
                            val percentValue = editText.text.trim().toString().toFloat()
                            if(percentValue<=100) {
                                val value = 40*(100-percentValue)
                                automationSoilMoisture.setValue(value).addOnSuccessListener {
                                    binding.textViewSoilMoistureAutomationValue.text =
                                        "%" + percentValue.toString()
                                    Toast.makeText(context, "The value is set", Toast.LENGTH_SHORT)
                                        .show()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "" + e.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }else{
                                Toast.makeText(context, " The value cannot be bigger than 100 ", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(context, "Please enter a valid value", Toast.LENGTH_SHORT).show()
                        }

                    }

                })
                show()

            }

        }



        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}