from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt

from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
import base64
from django.core.files.base import ContentFile
import face_recognition
from pathlib import Path
from .models import Criminals
import pickle

# A view to register new criminals into the database
@csrf_exempt
@api_view(['POST'])
def register_criminal(request):
    if request.method == 'POST':
        data =request.data
        if data.__contains__('first_name') and data.__contains__('last_name') and data.__contains__('crime') and data.__contains__('imageBase64') and data.__contains__('age'):
            imgstr = data['imageBase64']
            first_name = data['first_name']
            last_name = data['last_name']
            crime = data['crime']
            age = data['age']

            ext = 'jpeg'
            img = ContentFile(base64.b64decode(imgstr), name='temp.' + ext)
            new_face = face_recognition.load_image_file(img)
            new_face_encoding = face_recognition.face_encodings(new_face)[0]
            pickled = Path("criminal/criminals.pkl")


            # Load the criminal database and check for a known criminal
            criminals_dict = {}
            if pickled.is_file():
                infile = open("criminal/criminals.pkl", 'rb')
                criminals_dict = pickle.load(infile)
                infile.close()
            criminals_dict[first_name + " " + last_name] = new_face_encoding
            outfile = open("criminal/criminals.pkl", 'wb')
            pickle.dump(criminals_dict, outfile)
            outfile.close()

            Criminals.objects.create(first_name=first_name, last_name=last_name, crime=crime, age=age)

            return Response({'Status' : 'Success'}, status.HTTP_201_CREATED)

        else:
            return Response(status.HTTP_400_BAD_REQUEST)

# A view to check if the new image contains an image of a known criminal
@csrf_exempt
@api_view(['POST'])
def check_criminal(request):
    if request.method == 'POST':
        data = request.data
        if data.__contains__('imageBase64'):
            infile = open('criminal/criminals.pkl', 'rb')
            known_criminals = pickle.load(infile)
            
            imgstr = data['imageBase64']
            ext = 'jpeg'
            img = ContentFile(base64.b64decode(imgstr), name='temp.' + ext)
            new_face = face_recognition.load_image_file(img)
            new_face_encoding = face_recognition.face_encodings(new_face)[0]

            for key in known_criminals:
                if face_recognition.compare_faces([known_criminals[key]], new_face_encoding)[0]:
                    first_name, last_name = key.split(" ", 1)
                    criminal = Criminals.objects.get(first_name=first_name, last_name=last_name)
                    response_dict = {
                        'first_name' : first_name,
                        'last_name' : last_name,
                        'age' : criminal.age,
                        'crime' : criminal.crime
                    }
                    return Response(response_dict, status=status.HTTP_200_OK)
    else:
        return Response(status.HTTP_400_BAD_REQUEST)