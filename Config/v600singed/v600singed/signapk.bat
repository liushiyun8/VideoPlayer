color 2f
mode con cols=100 lines=20
java -jar signapk.jar platform.x509.pem platform.pk8 a.apk a_signed.apk
del a.apk
pause