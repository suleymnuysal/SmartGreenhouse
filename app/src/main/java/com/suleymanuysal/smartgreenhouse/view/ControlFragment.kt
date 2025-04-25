package com.suleymanuysal.smartgreenhouse.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.suleymanuysal.smartgreenhouse.R
import com.suleymanuysal.smartgreenhouse.databinding.FragmentControlBinding
import com.suleymanuysal.smartgreenhouse.worker.SetScheduledDataToOff
import com.suleymanuysal.smartgreenhouse.worker.SetScheduledDataToOn
import com.suleymanuysal.smartgreenhouse.worker.SetVentilationDegree
import me.tankery.lib.circularseekbar.CircularSeekBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ControlFragment : Fragment() {
    private var _binding : FragmentControlBinding? = null
    private val binding get() = _binding!!
    private lateinit var waterPumpRef: DatabaseReference
    private lateinit var fanRef: DatabaseReference
    private lateinit var ventilationRef: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentControlBinding.inflate(inflater,container,false)

        waterPumpRef = Firebase.database.reference.child("Relays").child("WaterPump")
        fanRef = Firebase.database.reference.child("Relays").child("Fan")
        ventilationRef = Firebase.database.reference.child("Ventilation").child("Angle")
        sharedPreferences = requireContext().getSharedPreferences("com.suleymanuysal.smartgreenhouse.view",Context.MODE_PRIVATE)

        checkStatus(waterPumpRef,fanRef,ventilationRef)

        val waterPumpStartTime = sharedPreferences.getString("waterPumpStarts"," -")
        val waterPumpEndTime = sharedPreferences.getString("waterPumpEnde"," -")
        val fanStartTime = sharedPreferences.getString("fanStarts"," -")
        val fanEndTime = sharedPreferences.getString("fanEnde"," -")
        val ventilationStartTime = sharedPreferences.getString("ventilationStarts"," -")
        val ventilationEndTime = sharedPreferences.getString("ventilationEnde"," -")

        binding.textViewWaterPumpStartTime.text = "Start Time: $waterPumpStartTime"
        binding.textViewWaterPumpEndTime.text = "End Time: $waterPumpEndTime"
        binding.textViewFanStartTime.text = "Start Time: $fanStartTime"
        binding.textViewFanEndTime.text = "End Time: $fanEndTime"
        binding.textViewVentilationStartTime.text = "Start Time: $ventilationStartTime"
        binding.textViewVentilationEndTime.text = "End Time: $ventilationEndTime"


        binding.waterPumpAddTimer.setOnClickListener {

            if(binding.waterPumpExpandable.visibility == View.GONE){
                binding.waterPumpExpandable.visibility = View.VISIBLE
                binding.waterPumpAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
            }else{
                binding.waterPumpExpandable.visibility = View.GONE
                binding.waterPumpAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            }
        }

        binding.fanAddTimer.setOnClickListener {

            if(binding.fanExpandable.visibility == View.GONE){
                binding.fanExpandable.visibility = View.VISIBLE
                binding.fanAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
            }else{
                binding.fanExpandable.visibility = View.GONE
                binding.fanAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            }
        }

        binding.ventilationAddTimer.setOnClickListener {

            if(binding.ventilationExpandable.visibility == View.GONE){
                binding.ventilationExpandable.visibility = View.VISIBLE
                binding.ventilationAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
            }else{
                binding.ventilationExpandable.visibility = View.GONE
                binding.ventilationAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            }
        }

        binding.textViewWaterPumpClearTime.setOnClickListener {
            clearAllTime("waterPumpStarts","waterPumpEnde"
                ,binding.textViewWaterPumpStartTime,binding.textViewWaterPumpEndTime)
        }
        binding.textViewFanClearTime.setOnClickListener {
            clearAllTime("fanStarts","fanEnde"
                ,binding.textViewFanStartTime,binding.textViewFanEndTime)
        }
        binding.textViewVentilationClearTime.setOnClickListener {
            clearAllTime("ventilationStarts","ventilationEnde"
                ,binding.textViewVentilationStartTime,binding.textViewVentilationEndTime)

        }

        chooseTime(binding.textViewWaterPumpStartTime,binding.textViewWaterPumpEndTime
            ,sharedPreferences,"waterPumpStarts"
            ,"waterPumpEnde",binding.buttonWaterPumpOn)

        chooseTime(binding.textViewFanStartTime,binding.textViewFanEndTime
            ,sharedPreferences,"fanStarts"
            ,"fanEnde",binding.buttonFanOn)

        chooseTime(binding.textViewVentilationStartTime,binding.textViewVentilationEndTime
            ,sharedPreferences,"ventilationStarts"
            ,"ventilationEnde",binding.buttonVentilationOn)

        val currentWPStartTime = sharedPreferences.getString("waterPumpStarts"," -")
        val currentWPEndTime = sharedPreferences.getString("waterPumpEnde"," -")
        val currentFStartTime = sharedPreferences.getString("fanStarts"," -")
        val currentFEndTime = sharedPreferences.getString("fanEnde"," -")
        val currentVStartTime = sharedPreferences.getString("ventilationStarts"," -")
        val currentVEndTime = sharedPreferences.getString("ventilationEnde"," -")

        binding.textViewWaterPumpTimerDisplay.text = "$currentWPStartTime <-> $currentWPEndTime"
        binding.textViewFanTimerDisplay.text = "$currentFStartTime <-> $currentFEndTime"
        binding.textViewVentilationTimerDisplay.text = "$currentVStartTime <-> $currentVEndTime"

        binding.buttonWaterPumpOn.setOnClickListener {

            openEquipment(binding.waterPumpExpandable,binding.waterPumpAddTimerImageView
                ,R.drawable.engine_motor_svgrepo_com,"waterPumpStarts","waterPumpEnde"
                ,waterPumpRef,binding.imageViewWaterPump,binding.textViewWaterPumpStartTime,binding.textViewWaterPumpEndTime
                ,"waterPump","waterPumpOff")


        }

        binding.buttonWaterPumpOff.setOnClickListener {

            showAlertDialog(waterPumpRef,binding.imageViewWaterPump
                ,R.drawable.engine_motor_off_svgrepo_com,"waterPumpStarts","waterPumpEnde"
                ,binding.textViewWaterPumpStartTime,binding.textViewWaterPumpEndTime,binding.buttonWaterPumpOn)

        }

        binding.buttonFanOn.setOnClickListener {

            openEquipment(binding.fanExpandable,binding.fanAddTimerImageView
                ,R.drawable.fan_circled_on_svgrepo_com,"fanStarts","fanEnde"
                ,fanRef,binding.imageViewFan,binding.textViewFanStartTime,binding.textViewFanEndTime
                ,"fan","fanOff")


        }

        binding.buttonFanOff.setOnClickListener {
            showAlertDialog(fanRef,binding.imageViewFan
                ,R.drawable.fan_circled_svgrepo_com,"fanStarts","fanEnde"
                ,binding.textViewFanStartTime,binding.textViewFanEndTime,binding.buttonFanOn)


        }


        binding.buttonVentilationOn.setOnClickListener {
            if(binding.ventilationExpandable.visibility == View.VISIBLE){
                binding.ventilationExpandable.visibility = View.GONE
                binding.ventilationAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            }
            Toast.makeText(context, "Please specify a value first", Toast.LENGTH_SHORT).show()
        }


        binding.circularSeekBarVentilationAngle.setOnSeekBarChangeListener(object :CircularSeekBar.OnCircularSeekBarChangeListener{
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                val angle = progress.toInt().toString()+"°"
                binding.textViewVentilationAngleValue.text = angle

            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
                val angle = seekBar!!.progress.toInt().toString()+"°"
                binding.textViewVentilationAngleValue.text = angle

            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                val ventilationAngle = seekBar!!.progress.toInt()
                val angleText = seekBar.progress.toInt().toString()+"°"
                binding.textViewVentilationAngleValue.text = angleText

                if(seekBar.progress.toInt().toString() != "0") {

                    binding.buttonVentilationOn.setOnClickListener {

                        if(binding.ventilationExpandable.visibility == View.VISIBLE){
                            binding.ventilationExpandable.visibility = View.GONE
                            binding.ventilationAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                        }

                        val currentStartTime = sharedPreferences.getString("ventilationStarts"," -")
                        val currentEndTime = sharedPreferences.getString("ventilationEnde"," -")

                        if(currentStartTime.equals("-") && currentEndTime.equals("-")){

                            ventilationRef.setValue(ventilationAngle).addOnSuccessListener {

                            }.addOnFailureListener { error ->
                                Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                            }

                        }else if(currentStartTime!!.isNotEmpty() && currentEndTime.equals("-")) {
                            //Schedule
                            val currentTime = getCurrentTime()
                            val state = compareTime(currentTime, currentStartTime)

                            if(state){

                                ventilationRef.setValue(ventilationAngle).addOnSuccessListener {

                                }.addOnFailureListener { error ->
                                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                                //clear time
                                clearStartTime("ventilationStarts",binding.textViewVentilationStartTime)

                            }else{
                                val delayTime = calculateTimeAsMinutes(currentTime,currentStartTime)
                                Toast.makeText(context, "The ventilation is set for $delayTime minutes later", Toast.LENGTH_SHORT).show()
                                setVentilationDegree(delayTime,ventilationAngle)

                            }

                        }else if(currentStartTime.equals("-") && currentEndTime!!.isNotEmpty()){

                            val currentTime = getCurrentTime()
                            val state = compareTime(currentTime, currentEndTime)

                            if(state){
                                ventilationRef.setValue(ventilationAngle).addOnSuccessListener {

                                }.addOnFailureListener { error ->
                                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                                //clear time
                                clearEndTime("ventilationEnde",binding.textViewVentilationEndTime)

                            }else{

                                ventilationRef.setValue(ventilationAngle).addOnSuccessListener {

                                }.addOnFailureListener { error ->
                                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                                }

                                val delayTime = calculateTimeAsMinutes(currentTime,currentEndTime)
                                Toast.makeText(context, "The equipment will be closed for $delayTime minutes later", Toast.LENGTH_SHORT).show()
                                setVentilationDegree(delayTime,0)

                            }

                        }else if(currentStartTime.isNotEmpty() && currentEndTime!!.isNotEmpty()){

                            val currentTime = getCurrentTime()
                            val state = compareTime(currentTime, currentEndTime)

                            if(state){

                                ventilationRef.setValue(ventilationAngle).addOnSuccessListener {
                                }.addOnFailureListener { error ->
                                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                                //clear time
                                clearAllTime("ventilationStarts","ventilationEnde"
                                    ,binding.textViewVentilationStartTime,binding.textViewVentilationEndTime)

                            }else{

                                val delayTime = calculateTimeAsMinutes(currentTime,currentStartTime)
                                val endDelayTime = calculateTimeAsMinutes(currentTime,currentEndTime)
                                Toast.makeText(context, "The ventilation is set for $delayTime minutes later and will be closed for $endDelayTime minutes later", Toast.LENGTH_SHORT).show()
                                setVentilationDegree(delayTime,ventilationAngle)
                                setVentilationDegree(endDelayTime,0)


                            }

                        }


                    }

                }else{
                    binding.buttonVentilationOn.setOnClickListener {
                        if(binding.ventilationExpandable.visibility == View.VISIBLE){
                            binding.ventilationExpandable.visibility = View.GONE
                            binding.ventilationAddTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                        }
                        Toast.makeText(context, "Please specify a value higher than 0", Toast.LENGTH_SHORT).show()
                    }

                }


            }


        })

        binding.buttonVentilationOff.setOnClickListener {

            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Are you sure to cancel ?")
            dialog.setMessage("If you cancel the task, all data you set up will be deleted.")
            dialog.setCancelable(true)
            dialog.setPositiveButton(
                "Yes"
            ) { p0, p1 ->

                ventilationRef.setValue(0).addOnSuccessListener {

                }.addOnFailureListener { error ->
                    Toast.makeText(context, "" + error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                clearAllTime(
                    "ventilationStarts",
                    "ventilationEnde",
                    binding.textViewVentilationStartTime,
                    binding.textViewVentilationEndTime
                )

                binding.textViewVentilationTimerDisplay.text = "- <-> -"

                binding.buttonVentilationOn.text = "On"
                Toast.makeText(context, "The task is canceled", Toast.LENGTH_SHORT).show()

            }
            dialog.setNegativeButton(
                "No"
            ) { p0, p1 ->

            }
            dialog.show()
        }

        return binding.root
    }

    private fun checkStatus(vararg databaseReference: DatabaseReference){

        for(dbRf in databaseReference){
            var textRef:TextView? = null
            var imageViewRef:ImageView? = null

            if (dbRf == waterPumpRef){
                textRef = binding.textViewWaterPumpStatus
                imageViewRef = binding.imageViewWaterPump
            }else if(dbRf == fanRef){
                textRef = binding.textViewFanStatus
                imageViewRef = binding.imageViewFan
            }else if(dbRf == ventilationRef){
                textRef = binding.textViewVentilationStatus
            }

            dbRf.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.value.toString()

                    if(dbRf == ventilationRef){
                        if(status == "0"){
                            binding.textViewVentilationAngleValue.text = "0"+"°"
                            binding.circularSeekBarVentilationAngle.progress = 0f
                            binding.buttonVentilationOn.setOnClickListener {
                                Toast.makeText(context, "Please specify a value higher than 0", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            binding.textViewVentilationAngleValue.text = status+"°"
                            binding.circularSeekBarVentilationAngle.progress = status.toFloat()
                        }
                    }

                    if(status == "0"){
                        textRef!!.text = "Status: Off"

                        if(imageViewRef == binding.imageViewWaterPump){
                            imageViewRef.setImageResource(R.drawable.engine_motor_off_svgrepo_com)
                        }else if(imageViewRef == binding.imageViewFan){
                            imageViewRef.setImageResource(R.drawable.fan_circled_svgrepo_com)
                        }

                    }else{
                        if(imageViewRef == binding.imageViewWaterPump){
                            imageViewRef.setImageResource(R.drawable.engine_motor_svgrepo_com)
                        }else if(imageViewRef == binding.imageViewFan){
                            imageViewRef.setImageResource(R.drawable.fan_circled_on_svgrepo_com)
                        }

                        textRef!!.text = "Status: On"

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
                }

            })
        }



    }


    private fun showTimePicker(textView: TextView,sharedPreferences: SharedPreferences,spKey:String):Boolean{

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(requireContext(),TimePickerDialog.OnTimeSetListener { timePicker, hour, min ->

            val currentTime = getCurrentTime()
            val selectedTime = "$hour:$min"
            val state = compareTime(currentTime,selectedTime)

            if(state){
                Toast.makeText(context, "Please select time greater than current time", Toast.LENGTH_SHORT).show()

            }else {
                val textTimeStart = "Start Time: $hour:$min"
                val textTimeEnd = "End Time: $hour:$min"
                val time = "$hour:$min"
                sharedPreferences.edit().putString(spKey, time).apply()

                if (spKey.endsWith("s")) {
                    textView.text = textTimeStart
                } else {
                    textView.text = textTimeEnd
                }
            }

        },hour,minute,true)

        timePicker.setTitle("Select Hour")
        timePicker.setButton(DialogInterface.BUTTON_POSITIVE,"Set",timePicker)
        timePicker.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",timePicker)
        timePicker.show()

        return true
    }

    private fun openEquipment(expandable:LinearLayout,addTimerImageView: ImageView,imageSource:Int
                         ,startKey:String,endKey:String,dbRef:DatabaseReference,imageView: ImageView
                         ,startTime: TextView,endTime: TextView,whichToOpen:String,whichToClose:String){

        if(expandable.visibility == View.VISIBLE){
            expandable.visibility = View.GONE
            addTimerImageView.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
        }

        val currentStartTime = sharedPreferences.getString(startKey,"-")
        val currentEndTime = sharedPreferences.getString(endKey,"-")


        if(currentStartTime.equals("-") && currentEndTime.equals("-")){

            dbRef.setValue(1).addOnSuccessListener {
                imageView.setImageResource(imageSource)
            }.addOnFailureListener { error ->
                Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        }else if(currentStartTime!!.isNotEmpty() && currentEndTime.equals("-")) {
            //Schedule
            val currentTime = getCurrentTime()
            val state = compareTime(currentTime, currentStartTime)

            if(state){

                dbRef.setValue(1).addOnSuccessListener {
                    imageView.setImageResource(imageSource)
                }.addOnFailureListener { error ->
                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                //clear time
                clearStartTime(startKey,startTime)

            }else{
                val delayTime = calculateTimeAsMinutes(currentTime,currentStartTime)
                Toast.makeText(context, "The equipment is set for $delayTime minutes later", Toast.LENGTH_SHORT).show()
                setTheEquipmentToOpen(delayTime,whichToOpen)

            }

        }else if(currentStartTime.equals("-") && currentEndTime!!.isNotEmpty()){

            val currentTime = getCurrentTime()
            val state = compareTime(currentTime, currentEndTime)

            if(state){
                dbRef.setValue(1).addOnSuccessListener {
                    imageView.setImageResource(imageSource)
                }.addOnFailureListener { error ->
                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                //clear time
                clearEndTime(endKey,endTime)

            }else{

                dbRef.setValue(1).addOnSuccessListener {
                    imageView.setImageResource(imageSource)
                }.addOnFailureListener { error ->
                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                }

                val delayTime = calculateTimeAsMinutes(currentTime,currentEndTime)
                Toast.makeText(context, "The equipment will be closed for $delayTime minutes later", Toast.LENGTH_SHORT).show()
                setTheEquipmentToClose(delayTime,whichToClose)

            }

        }else if(currentStartTime.isNotEmpty() && currentEndTime!!.isNotEmpty()){

            val currentTime = getCurrentTime()
            val state = compareTime(currentTime, currentEndTime)

            if(state){

                dbRef.setValue(1).addOnSuccessListener {
                    imageView.setImageResource(imageSource)
                }.addOnFailureListener { error ->
                    Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                //clear time
                clearAllTime(startKey,endKey
                    ,startTime,endTime)

            }else{

                val delayTime = calculateTimeAsMinutes(currentTime,currentStartTime)
                val endDelayTime = calculateTimeAsMinutes(currentTime,currentEndTime)
                Toast.makeText(context, "The equipment is set for $delayTime minutes later and will be closed for $endDelayTime minutes later", Toast.LENGTH_SHORT).show()
                setTheEquipmentToOpen(delayTime,whichToOpen)
                setTheEquipmentToClose(endDelayTime,whichToClose)


            }

        }

    }

    private fun getCurrentTime():String{

        val calendar = Calendar.getInstance();
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = "$hour:$minute"

        return currentTime
    }

    private fun setTheEquipmentToOpen(delayTime:Long,whichKey:String){

        val setTheEquipmentToOnRequest:WorkRequest = OneTimeWorkRequestBuilder<SetScheduledDataToOn>()
            .setInputData(Data.Builder().putString("whichKey",whichKey).build())
            .setInitialDelay(delayTime,TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(setTheEquipmentToOnRequest)

        val currentWPStartTime = sharedPreferences.getString("waterPumpStarts"," -")
        val currentWPEndTime = sharedPreferences.getString("waterPumpEnde"," -")
        val currentFStartTime = sharedPreferences.getString("fanStarts"," -")
        val currentFEndTime = sharedPreferences.getString("fanEnde"," -")

        binding.textViewWaterPumpTimerDisplay.text = "$currentWPStartTime <-> $currentWPEndTime"
        binding.textViewFanTimerDisplay.text = "$currentFStartTime <-> $currentFEndTime"

    }

    private fun setVentilationDegree(delayTime:Long, ventilationAngle:Int){

        val setVentilationDegreeRequest:WorkRequest = OneTimeWorkRequestBuilder<SetVentilationDegree>()
            .setInputData(Data.Builder().putInt("ventilationAngle",ventilationAngle).build())
            .setInitialDelay(delayTime,TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(setVentilationDegreeRequest)

        val currentVStartTime = sharedPreferences.getString("ventilationStarts"," -")
        val currentVEndTime = sharedPreferences.getString("ventilationEnde"," -")
        binding.textViewVentilationTimerDisplay.text = "$currentVStartTime <-> $currentVEndTime"
    }



    private fun setTheEquipmentToClose(delayTime:Long,whichKey:String){

        val setTheEquipmentToOffRequest:WorkRequest = OneTimeWorkRequestBuilder<SetScheduledDataToOff>()
            .setInputData(Data.Builder().putString("whichKeyOff",whichKey).build())
            .setInitialDelay(delayTime,TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(setTheEquipmentToOffRequest)

    }

    @SuppressLint("SimpleDateFormat")
    private fun calculateTimeAsMinutes(startTime:String, endTime:String):Long{

        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val start = simpleDateFormat.parse(startTime)
        val end = simpleDateFormat.parse(endTime)

        val difference: Long = end!!.time - start!!.time
        val days = (difference / (1000*60*60*24))
        val hours =  ((difference - (1000*60*60*24*days)) / (1000*60*60))
        val min = (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60)

        val delayTime = (hours*60).plus(min)

        return delayTime
    }

    @SuppressLint("SimpleDateFormat")
    private fun compareTime(currentTime:String, savedTime: String):Boolean{

        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val savedTime = simpleDateFormat.parse(savedTime)
        val currentTime = simpleDateFormat.parse(currentTime)

        val difference: Long = currentTime!!.time - savedTime!!.time

        if(difference>0){
            return true
        }else{
            return false
        }

    }
    private fun clearAllTime(whichKeyStart: String,whichKeyEnd: String,whereStart:TextView,whereEnd:TextView){
        sharedPreferences.edit().putString(whichKeyStart,"-").apply()
        sharedPreferences.edit().putString(whichKeyEnd,"-").apply()
        whereStart.text = "Start Time: -"
        whereEnd.text = "End Time: -"
        binding.textViewWaterPumpTimerDisplay.text = "- <-> -"
        binding.textViewFanTimerDisplay.text = "- <-> -"
        binding.textViewVentilationTimerDisplay.text = "- <-> -"
    }
    private fun chooseTime(startTime:TextView,endTime:TextView, sp:SharedPreferences
                           ,spKeyForStart: String,spKeyForEnd: String,buttonOn:Button){
        startTime.setOnClickListener {

            val state = showTimePicker(startTime,sp,spKeyForStart)
            if (state){
                buttonOn.text = "Set"
            }else{
                buttonOn.text = "On"
            }
        }
        endTime.setOnClickListener {
            val state = showTimePicker(endTime,sp,spKeyForEnd)
            if (state){
                buttonOn.text = "Set"
            }else{
                buttonOn.text = "On"
            }
        }

    }

    private fun showAlertDialog(reference: DatabaseReference,imageView: ImageView,source:Int,whereStart: String
                                ,whereEnd: String,startTime: TextView,endTime: TextView,buttonOn: Button){

        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Are you sure to cancel ?")
        dialog.setMessage("If you cancel the task, all data you set up will be deleted.")
        dialog.setCancelable(true)
        dialog.setPositiveButton("Yes"
        ) { p0, p1 ->

            reference.setValue(0).addOnSuccessListener {
                imageView.setImageResource(source)
            }.addOnFailureListener { error ->
                Toast.makeText(context, ""+error.localizedMessage, Toast.LENGTH_SHORT).show()
            }
            clearAllTime(whereStart,whereEnd
                ,startTime,endTime)

            binding.textViewWaterPumpTimerDisplay.text = "- <-> -"
            binding.textViewFanTimerDisplay.text = "- <-> -"
            buttonOn.text = "On"
            Toast.makeText(context, "The task is canceled", Toast.LENGTH_SHORT).show()

        }
        dialog.setNegativeButton("No"
        ){p0, p1 ->

        }
        dialog.show()
    }


    private fun clearStartTime(whichKeyStart: String,whereStart:TextView){
        sharedPreferences.edit().putString(whichKeyStart,"-").apply()
        whereStart.text = "Start Time: -"
    }

    private fun clearEndTime(whichKeyEnd: String,whereEnd:TextView){
        sharedPreferences.edit().putString(whichKeyEnd,"-").apply()
        whereEnd.text = "Start Time: -"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}