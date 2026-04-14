from django.db.models import Sum
from django.shortcuts import render
from .models import Transaction, Category

def transactions_list(request):
    transactions = Transaction.objects.all()
    categories = Category.objects.all()

    search_query = request.GET.get('search')
    category_id = request.GET.get('category')
    sort_by = request.GET.get('sort')

    expenses = transactions.filter(debit_or_credit='debit')
    incomes = transactions.filter(debit_or_credit='credit')

    total_incomes = incomes.aggregate(Sum('amount'))['amount__sum'] or 0
    total_expenses = expenses.aggregate(Sum('amount'))['amount__sum'] or 0
    balance = total_incomes - total_expenses

    if category_id:
        transactions = transactions.filter(category_id=category_id)

    if search_query:
        transactions = transactions.filter(title__icontains=search_query)

    if sort_by:
        transactions = transactions.order_by(sort_by)

    context = {
        'transactions': transactions,
        'categories': categories,
        'total_incomes': total_incomes,
        'total_expences': total_expenses,
        'balance': balance,
    }

    return render(request, 'transactions_list.html', context)