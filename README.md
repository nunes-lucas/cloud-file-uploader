# Cloud file uploader

This is an app example on how to do upload files and directories to cloud provider
Current working with GDrive

----------

## GDrive setup

Follow the steps on this page [Gdrive Java Quickstart](https://developers.google.com/drive/v3/web/quickstart/java)
The file with your credentials must be under **resources** folder by the name of **client_secret.json**

## Setup

Create a file named **backup_files** under your home directory (Eg. /home/nunes/backup_files) containing a list of files and directoris to backup.

For example
```
/home/nunes/notes
/home/nunes/appointments.txt
/home/nunes/recording.mp3
```

## Run

```
$ mvn install
$ mvn exec:java
```

On the first time you run a browser tab will open requesting you to allow the app to manage your GDrive account

And then the app will start upload the files and directories you listed on **backup_files** to a folder underGDrive  root directory named **Backup_\<OS username\>**

## Alternative Run (jar file)

```
$ java -jar target/cloud-file-uploader-1.0-SNAPSHOT-jar-with-dependencies.jar
```
