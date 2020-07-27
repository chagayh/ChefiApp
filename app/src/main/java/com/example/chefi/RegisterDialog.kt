package com.example.chefi

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment


class RegisterDialog : AppCompatDialogFragment() {

    private lateinit var logInBtn: Button
    private lateinit var signInBtn: Button
    private lateinit var passwordEditText : EditText
    private lateinit var userNameEditText : EditText
    private lateinit var dialogView: View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.register_dialog, container, false)
        logInBtn = dialogView.findViewById(R.id.logInBtn)
        signInBtn = dialogView.findViewById(R.id.signInBtn)
        userNameEditText = dialogView.findViewById(R.id.userNameEditText)
        passwordEditText = dialogView.findViewById(R.id.passwordEditText)
        return dialogView
    }

    /** The system calls this only when creating the layout in a dialog. */
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.register_dialog, null)
        builder.setView(view)
        return builder.create()
    }



}
