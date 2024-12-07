package com.example.capstonebangkitpawers.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class CheckPenyakit {

    fun checkPenyakit(penyakitRef: DatabaseReference, predictedLabel: String, callback: (Penyakit?) -> Unit) {
        if (predictedLabel == "Kutu") {
            penyakitRef.orderByChild("nama").equalTo("Kutu")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val penyakit = snapshot.children.firstOrNull()?.getValue(Penyakit::class.java)
                            callback(penyakit)
                        } else {
                            callback(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(null)
                    }
                })
        } else if (predictedLabel == "Rodent Ulcer") {
            penyakitRef.orderByChild("nama").equalTo("Rodent Ulcer")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val penyakit = snapshot.children.firstOrNull()?.getValue(Penyakit::class.java)
                            callback(penyakit)
                        } else {
                            callback(null)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        callback(null)
                    }
                })
        } else if (predictedLabel == "Eosinophilic Plaque") {
            penyakitRef.orderByChild("nama").equalTo("Eosinophilic Plaque")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val penyakit = snapshot.children.firstOrNull()?.getValue(Penyakit::class.java)
                            callback(penyakit)
                        } else {
                            callback(null)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        callback(null)
                    }
                })
        } else if (predictedLabel == "Acne") {
            penyakitRef.orderByChild("nama").equalTo("Acne")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val penyakit = snapshot.children.firstOrNull()?.getValue(Penyakit::class.java)
                            callback(penyakit)
                        } else {
                            callback(null)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        callback(null)
                    }
                })
        } else if (predictedLabel == "Dermatitis Alergi") {
            penyakitRef.orderByChild("nama").equalTo("Dermatitis Alergi")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val penyakit = snapshot.children.firstOrNull()?.getValue(Penyakit::class.java)
                            callback(penyakit)
                        } else {
                            callback(null)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        callback(null)
                    }
                })
        } else {
            callback(null)
        }
    }
}
