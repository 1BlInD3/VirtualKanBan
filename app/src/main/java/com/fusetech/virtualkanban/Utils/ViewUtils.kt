package com.fusetech.mobilleltarkotlin

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

fun showMe(message: String, context: Context) {
    val dialog = AlertDialog.Builder(context)
    dialog.setTitle("Figyelem")
    dialog.setMessage(message)
    dialog.setPositiveButton("OK") { _, _ -> }
    dialog.create()
    dialog.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
}