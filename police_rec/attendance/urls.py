from django.urls import path
from . import views

urlpatterns = [
    path('register/', views.register, name='register'),
    path('login/', views.login_user, name = 'login'),
    path('start_attendance/', views.start_attendance, name='start_attendance'),
    path('mark_attendance/', views.mark_attendance, name='mark_attendance'),
    path('get_attendance/', views.get_attendance, name='get_attendance'),
]

