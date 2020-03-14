package com.example.zenskikalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AboutDialog extends AlertDialog.Builder {

    public AboutDialog(Context context) {
        super(context);
        setTitle("About");
        setMessage("App name:Zenski kalendar \nby:Jelena Radeka");
        setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public AlertDialog prepareDialog(){
        AlertDialog dialog = create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}

