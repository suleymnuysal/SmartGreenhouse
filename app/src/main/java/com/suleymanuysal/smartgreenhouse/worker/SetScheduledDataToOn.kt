package com.suleymanuysal.smartgreenhouse.worker

import android.content.Context
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SetScheduledDataToOn(val context: Context, workerParams:WorkerParameters) : Worker(context,workerParams){

    private val waterPumpRef = Firebase.database.reference.child("Relays").child("WaterPump")
    private val fanRef = Firebase.database.reference.child("Relays").child("Fan")
    private var ventilationAngle:Int = 0

    override fun doWork(): Result {

        val inputData = inputData
        val incomeData = inputData.getString("whichKey")

        if(incomeData.equals("waterPump")){
            setTheEquipmentToOn(waterPumpRef)
        }else if(incomeData.equals("fan")){
            setTheEquipmentToOn(fanRef)
        }
        return Result.success()
    }

    private fun setTheEquipmentToOn(dbRef:DatabaseReference){

        dbRef.setValue(1).addOnSuccessListener {
            Toast.makeText(context, "The Equipment is ON "+ventilationAngle, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {e ->
            Toast.makeText(context, ""+e.localizedMessage, Toast.LENGTH_SHORT).show()
        }

    }


}