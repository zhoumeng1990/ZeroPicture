package com.zero.libraryforphoto;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * Created by ZhouMeng on 2018/12/19.
 */

public class FileProviderForPhoto {

    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(context, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    public static Uri getUriForFile24(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider",
                file);
    }
}
