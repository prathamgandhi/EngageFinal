from django.shortcuts import render
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
import base64
import pickle
import datetime
import xlsxwriter
from pathlib import Path


from django.core.files.base import ContentFile
from django.contrib.auth import authenticate

from .models import User, Attendance

import face_recognition

# Create your views here.

# This method loads the attendance in ram, so that it need not be fetched from disk repeatedly
load_attendance_list = {}

@csrf_exempt
@api_view(['POST'])
def register(request):
    if request.method == "POST":
        data = request.data
        # Check for validity of data
        if data.__contains__('imageBase64') and data.__contains__('first_name') and data.__contains__('last_name') and data.__contains__('password') and data.__contains__('phone') and data.__contains__('stationId'):
            imgstr = data['imageBase64']
            ext = 'jpeg'
            img = ContentFile(base64.b64decode(imgstr), name='temp.' + ext)

            first_name = data['first_name']
            last_name = data['last_name']
            phone = data['phone']
            stationId = data['stationId']
            phone = data['phone']
            username = first_name + last_name + phone
            password = data['password']

            # Load the new face embeddings in a serialized pkl file
            new_face = face_recognition.load_image_file(img)
            new_face_encoding = face_recognition.face_encodings(new_face)[0]
            pickled = Path("attendance/officers.pkl")
            officers_dict = {}
            if pickled.is_file():
                infile = open("attendance/officers.pkl", 'rb')
                officers_dict = pickle.load(infile)
                infile.close()
            officers_dict[username] = new_face_encoding
            outfile = open("attendance/officers.pkl", 'wb')
            pickle.dump(officers_dict, outfile)
            outfile.close()

            user = User.objects.create(username=username, first_name=first_name, last_name=last_name)
            user.set_password(password)
            # Save the profile of the user as well
            user.profile.phone = phone
            user.profile.stationId = stationId
            
            user.save()
            return Response({"Success" : "Created"}, status=status.HTTP_201_CREATED)
        return Response({"Error"}, status=status.HTTP_400_BAD_REQUEST)

@csrf_exempt
@api_view(['POST'])
def login_user(request):
    if request.method == 'POST':
        data = request.data
        # Check the validity of the data
        if data.__contains__('first_name') and data.__contains__('last_name') and data.__contains__('password') and data.__contains__('imageBase64'):
            first_name = data['first_name']
            last_name = data['last_name']
            phone = data['phone']
            password = data['password']
            img = data['imageBase64']
            username = first_name + last_name + phone
            if(User.objects.filter(username=username).exists()):
                user = authenticate(username=username, password=password)
                if user is not None:
                    ext = 'jpeg'
                    unknown_img = face_recognition.load_image_file(ContentFile(base64.b64decode(img), name='temp.' + ext))
                    infile = open('attendance/officers.pkl', 'rb')
                    officers_dict = pickle.load(infile)
                    infile.close()
                    known_encoding = officers_dict[username]
                    unknown_encoding = face_recognition.face_encodings(unknown_img)[0]
                    results = face_recognition.compare_faces([known_encoding], unknown_encoding)
                    if results[0]:
                        return Response({'Success' : 'User authenticated'}, status=status.HTTP_202_ACCEPTED)
                    else:
                        return Response({'Error' : 'User not authenticated'}, status=status.HTTP_401_UNAUTHORIZED)
                else:
                    print(username)
                    print(password)
                    return Response({'Error' : 'User not authenticated'}, status=status.HTTP_401_UNAUTHORIZED)
            return Response({'Error' : 'User not found'}, status=status.HTTP_404_NOT_FOUND)
        return Response({'Error' : 'Bad Request'}, status=status.HTTP_400_BAD_REQUEST)

@csrf_exempt
@api_view(['GET'])
def start_attendance(request):
    if request.method == 'GET':
        if Path('officers.pkl').is_file():
            infile = open('officers.pkl', 'rb')
            # Use the global load_atttendance_list which is stored in memory
            global load_attendance_list
            load_attendance_list = pickle.load(infile)
            infile.close()
            return Response(status=status.HTTP_204_NO_CONTENT)
        return Response({'Error' : 'No records exist right now'}, status=status.HTTP_404_NOT_FOUND)

@csrf_exempt
@api_view(['POST'])
def mark_attendance(request):
    if request.method == 'POST':
        data = request.data
        img = data['imageBase64']
        ext = 'jpeg'
        test_img = face_recognition.load_image_file(ContentFile(base64.b64decode(img), name='temp.' + ext))
        test_encodings = face_recognition.face_encodings(test_img)[0]
        # Compare all the faces and check the matched one
        for key in load_attendance_list:
            print(load_attendance_list[key])
            if face_recognition.compare_faces([load_attendance_list[key]], test_encodings)[0]:
                user = User.objects.get(username = key)
                Attendance.objects.create(user=user)
                return Response({'Code' : 'Success', 'firstName' : user.first_name, 'lastName' : user.last_name}, status=status.HTTP_201_CREATED)
        return Response({'Code' : 'Error Face not identified'}, status=status.HTTP_404_NOT_FOUND)


# Generate an attendance spreadsheet and send to the user
@csrf_exempt
@api_view(['GET'])
def get_attendance(request):
    if request.method == 'GET':
        day = datetime.date.today().day
        month = datetime.date.today().month
        year = datetime.date.today().year
        todays_attendance = (Attendance.objects.filter(attendance__year=str(year),attendance__month = str(month), attendance__day=str(day)))
        attendance_dict = {}
        for attendance in todays_attendance:
            attendance_dict[attendance.user] = attendance
        users = User.objects.all()
        workbook = xlsxwriter.Workbook('attendance/' + str(datetime.date.today()) + '.xlsx')
        worksheet = workbook.add_worksheet()
        worksheet.write(0, 0, 'First Name')
        worksheet.write(0, 1, 'Last Name')
        worksheet.write(0, 2, 'Phone')
        worksheet.write(0, 3, 'Station ID')
        worksheet.write(0, 4, 'Attendance')
        row = 1
        for user in users:
            worksheet.write(row, 0, user.first_name)
            worksheet.write(row, 1, user.last_name)
            worksheet.write(row, 2, user.profile.phone)
            worksheet.write(row, 3, user.profile.stationId)
            if user in attendance_dict:
                str(attendance_dict[user].attendance)
                worksheet.write(row,  4, str(attendance_dict[user].attendance))
            else:
                worksheet.write(row, 4, 'A')
            row += 1
        workbook.close()
        with open('attendance/' + str(datetime.date.today()) + '.xlsx', 'rb') as f:
            file = f.read()
            content_type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            response = HttpResponse(file, content_type=content_type)
            response['Content-Disposition'] = f'attachment; filename={str(datetime.date.today())}'
        return response
