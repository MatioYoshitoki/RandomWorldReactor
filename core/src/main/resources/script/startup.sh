nohup java -jar -Xms512M -Xmx512M -XX:PermSize=128M -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=1 ../core-1.0-SNAPSHOT.jar &
