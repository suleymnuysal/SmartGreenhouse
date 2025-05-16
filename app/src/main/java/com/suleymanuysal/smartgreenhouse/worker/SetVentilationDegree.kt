package com.suleymanuysal.smartgreenhouse.worker

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SetVentilationDegree(val context: Context, workerParams:WorkerParameters) : Worker(context,workerParams){

    private val ventilationRef = Firebase.database.reference.child("Ventilation").child("Angle")

    override fun doWork(): Result {

        val inputData = inputData
        val incomeData = inputData.getInt("ventilationAngle",0)
        setVentilationToOn(ventilationRef,incomeData)


        return Result.success()
    }

    private fun setVentilationToOn(dbRef:DatabaseReference,ventilationAngle:Int){

        dbRef.setValue(ventilationAngle).addOnSuccessListener {
            Toast.makeText(context, "The ventilation is set for $ventilationAngle° degree", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { error ->
            Toast.makeText(context, "" + error.localizedMessage, Toast.LENGTH_SHORT)
                .show()
        }

    }

}