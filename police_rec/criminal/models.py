from django.db import models

# Create your models here.


class Criminals(models.Model):
    first_name = models.CharField(max_length=20)
    last_name = models.CharField(max_length=20)
    crime = models.TextField()
    age = models.IntegerField()

    def __str__(self):
        return self.first_name + " " + self.last_name