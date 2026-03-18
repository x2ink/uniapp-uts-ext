package com.x2;
import android.content.Context
import android.widget.Toast

class ToastTest(private val context: Context){
    fun openToast(text: String){
        Toast.makeText(context,text, Toast.LENGTH_LONG).show()
    }
}