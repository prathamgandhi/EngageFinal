# EngageFinal
This is the final submission for the Microsoft Engage Program 2022. A face recognition app with the law enforcement in mind.

# Steps to reproduce the application
Running the server : In the command line, use `pip3 install -r requirements.txt` to install all the requirements. Run the django server using `python3 manage.py runserver 0.0.0.0:8000` All the above steps have been tested on a Linux based system.
Running the app : Directly use the apk available in the build/outputs folder, if that doesn't work, build the application again using the source code, no special configuration required.

# What is CopX?
CopX is a face-recognition system to support our law and police enforcements. In order to demonstrate the uses of face-recognition, three major use cases have been created with this application.
- Face unlock as the second factor of authentication along with username and password for secure login.
- Face-recognition based attendance system along with the service to download the attendance list directly on users system.
- Identification of criminals from their photos from the police criminal database.

# Future improvements : 
- The request/response time can be improved with the help of celery on the backend, to run long running processes asynchronously.
- Pressing the back button on the home page, directly removes the login panel, this could not be fixed due to lack of time but needs to be done.
- UI can be greatly improved.

# Challenges : 
- Gaining an understanding of the functioning of the camera on Android.
- Learning to use ML models on Android directly.
- Learning new Android UI componenets.
- Quickening the speed of processing, by dividing the time taken for generation of face encodings between login and registration.
