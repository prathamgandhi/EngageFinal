from django.urls import path
from . import views

urlpatterns = [
    path('register_criminal/', views.register_criminal, name='register'),
    path('check_criminal/', views.check_criminal, name = 'check'),
]
