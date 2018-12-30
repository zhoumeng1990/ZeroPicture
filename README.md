前段时间做拍照和相册选择图片上传，遇到一些坑，最近闲来没事，就整理一篇博客，并写了个demo，项目中使用纯java，这次采用java + kotlin混合
## 1、首先遇到的是Android7.0的坑

说是坑，有点欲加之罪的感觉，其实就是Android7.0的一个行为变更，以拍照为例，代码如下：

```
val file = File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)
val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
startActivityForResult(intent, REQUEST_IMAGE_PHOTO)
```
一经运行，在7.0以下的系统是可以正常运行，7.0及以上，便出现以下闪退，提示“android.os.FileUriExposedException: file:///storage/emulated/0/icon.jpg exposed beyond app through ClipData.Item.getUri()”，如图所示：
![Android7.0](https://img-blog.csdnimg.cn/20181223233011902.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BhbmdwYW5nMTIzNjU0,size_16,color_FFFFFF,t_70)

查阅资料，根据官方文档提供[Android 7.0 行为变更](https://developer.android.com/about/versions/nougat/android-7.0-changes)

![Android7.0变更图](https://img-blog.csdnimg.cn/20181223234046922.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3BhbmdwYW5nMTIzNjU0,size_16,color_FFFFFF,t_70)

官方已经指出问题所在，并给出了解决方案，通过FileProvider操作来解决闪退的问题。解决方案现在已经好多，今天这里不做多余的赘述，有兴趣可以直接看demo。

## 2、无法加载此图片

 - Intent.FLAG_GRANT_READ_URI_PERMISSION：临时访问读权限  intent的接受者将被授予 INTENT 数据uri 或者 在ClipData 上的读权限。
 - Intent.FLAG_GRANT_WRITE_URI_PERMISSION:临时访问写权限  intent的接受者将被授予 INTENT 数据uri 或者 在ClipData 上的写权限。


代码如下：
```
fun startSmallPhotoZoom(activity: Activity, uri: Uri?, code: Int) {
            val intent = Intent("com.android.camera.action.CROP")
            intent.setDataAndType(uri, "image/*")
            //以下两行添加，解决无法加载此图片的提示
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", 1) // 裁剪框比例
            intent.putExtra("aspectY", 1)
            intent.putExtra("outputX", 300) // 输出图片大小
            intent.putExtra("outputY", 300)
            intent.putExtra("scale", true)
            intent.putExtra("return-data", true)
            activity.startActivityForResult(intent, code)
        }
```

## 3、保存时发生错误，保存失败

部分手机会出现这种情况，小米居多，造成此原因主要是onActivityResult回调时，必要信息没有得到返回。

解决方案如下：

```
/**
     * 将URI转为图片的路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
```

后续有啥问题，会持续更新，如有错误，欢迎指正(*^__^*) 嘻嘻……
