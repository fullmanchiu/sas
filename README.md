# SAS（Starlight Assistant Service）
a simple assistant designed for Wuling Starlight S
1. 进入工程模式，打开wifi adb。（网上一堆教程）
2. 通过adb connect连接到车机。（网上的教程都是用甲壳虫adb助手，实测Android 14的手机闪退，建议用电脑命令行）
3. adb root ； adb remount（实测车上本身已经remount）
4. adb shell 在system/priv-app/下 新建apk文件夹 如system/priv-app/Srs/
5. 将apk push到创建的目录下 （adb push Sas.apk /system/priv-app/Srs/Sas.apk）
6. adb shell am restart/ adb reboot /长按方控*重启
7. 此时屏幕会显示小圆点，点击小圆点会打开applist

