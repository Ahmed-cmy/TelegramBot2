# 1. تحديد بيئة التشغيل الأساسية (هنا اخترنا بيئة خفيفة لجافا 17، يمكنك تغيير الرقم حسب إصدارك)
FROM openjdk:17-jdk-alpine

# 2. إنشاء مجلد عمل داخل الحاوية لترتيب الملفات
WORKDIR /app

# 3. نسخ ملف البوت الخاص بك من جهازك إلى داخل الحاوية
COPY target/ShEhabBot-1.0-SNAPSHOT.jar /app/ShEhabBot-1.0-SNAPSHOT.jar

# 4. الأمر الذي سيقوم الخادم بتنفيذه لتشغيل البوت بشكل مستمر
CMD ["java", "-jar", "/app/ShEhabBot-1.0-SNAPSHOT.jar"]