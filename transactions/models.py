from django.db import models

class Category(models.Model):
    name = models.CharField('Назва', max_length=100)

    def __str__(self):
        return self.name

class Transaction(models.Model):
    title = models.CharField('Опис', max_length=100)
    amount = models.DecimalField(max_digits=10, decimal_places=2)

    TYPE_CHOICES = [('debit', 'Дебет'), ('credit', 'Кредит')]
    debit_or_credit = models.CharField(max_length=10, choices=TYPE_CHOICES, default='debit')

    category = models.ForeignKey('Category', on_delete=models.SET_NULL, null=True)

    def __str__(self):
        return self.title