from tkinter import CASCADE
from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver
import uuid

def user_directory_path(instance, filename):
    return 'user_{0}/{1}'.format(instance.uuid, filename)

# This is a profile model which is linked to the User model by a one-to-one relationship
class Profile(models.Model):
    user=models.OneToOneField(User,on_delete=models.CASCADE,primary_key=True)
    stationId = models.TextField()
    phone=models.CharField(max_length=12)
    uuid=models.UUIDField(default=uuid.uuid4, editable=False)
    imageBase64=models.ImageField(upload_to=user_directory_path)

    @receiver(post_save, sender=User)
    def create_user_profile(sender, instance, created, **kwargs):
        if created:
            Profile.objects.create(user=instance)

    @receiver(post_save, sender=User)
    def save_user_profile(sender, instance, **kwargs):
        instance.profile.save()

    def __str__(self):
        return self.user.username

class Attendance(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    attendance = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.user.first_name + " " + self.user.last_name
