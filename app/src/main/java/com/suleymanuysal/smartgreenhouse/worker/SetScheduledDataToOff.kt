package com.suleymanuysal.smartgreenhouse.worker

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SetScheduledDataToOff(val context: Context,workerParameters: WorkerParameters) :
    Worker(context,workerParameters){

    private val waterPumpRef = Firebase.database.reference.child("Relays").child("WaterPump")
    private val fanRef = Firebase.database.reference.child("Relays").child("Fan")

    override fun doWork(): Result {

        val inputData = inputData
        val incomeData = inputData.getString("whichKeyOff")

        if(incomeData.equals("waterPumpOff")){
            setTheEquipmentToOff(waterPumpRef)
        }else if(incomeData.equals("fanOff")){
            setTheEquipmentToOff(fanRef)
        }
        return Result.success()
    }

    private fun setTheEquipmentToOff(dbRef: DatabaseReference){

        dbRef.setValue(0).addOnSuccessListener {
            Toast.makeText(context, "The Equipment is OFF ", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {e ->
            Toast.makeText(context, ""+e.localizedMessage, Toast.LENGTH_SHORT).show()
        }


    }

}