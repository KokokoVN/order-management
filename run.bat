@echo off
chcp 65001 > nul
echo ===================================================
echo     KHỞI CHẠY ỨNG DỤNG XÁC THỰC SPRING BOOT
echo ===================================================
echo Đang chạy trên Java:
java -version
echo.

if exist "target\auth-app-0.0.1-SNAPSHOT.jar" (
    echo Đang khởi động máy chủ từ file JAR...
    java -jar target\auth-app-0.0.1-SNAPSHOT.jar
) else (
    echo Đang biên dịch và chạy bằng Maven...
    mvn spring-boot:run
)

pause

