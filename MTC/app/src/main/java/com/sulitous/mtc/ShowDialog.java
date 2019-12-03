package com.sulitous.mtc;

import android.app.Activity;
import android.app.Dialog;
import android.widget.TextView;

public class ShowDialog {

    private Dialog mDialog;
    private TextView mTitle, mMessage;

    public ShowDialog(Activity activity) {

        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.loading_dialog);
        this.mDialog = dialog;
        mTitle = dialog.findViewById(R.id.dialog_title);
        mMessage = dialog.findViewById(R.id.dialog_message);
    }

    public void setTitle(String title){
        mTitle.setText(title);
    }

    public void setMessage(String message){
        mMessage.setText(message);
    }

    public void show(){
        mDialog.show();
    }

    public void hide(){
        mDialog.hide();
    }

    public void cancel(){
        mDialog.cancel();
    }
}
