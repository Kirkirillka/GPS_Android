package de.tu_ilmenau.gpstracker.utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.regex.Pattern;

import de.tu_ilmenau.gpstracker.service.ClientService;
import de.tu_ilmenau.gpstracker.view.MainActivity;

public class Utils {
    static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))" +
            "((\\:[0-9]{1,5}))";
    static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private static final String SERVICE_CLASSNAME = ClientService.class.getName();

    public static boolean isValidIPV4(final String s) {
        return IPV4_PATTERN.matcher(s).matches();
    }

    public static boolean serviceIsRunning(MainActivity mainActivity) {
        ActivityManager manager = (ActivityManager) mainActivity.getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_CLASSNAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to create an AlertBox
     *
     * @param title   title of alerting box
     * @param message message in alerting box
     * @param activity activity for inserting alert
     */
    public static void alertBox(String title, String message, MainActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }


}
