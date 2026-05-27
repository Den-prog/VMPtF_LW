#налаштування підключення до бази
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base

SQLALCHEMY_DATABE_URL = "postgresql://postgres:superuser123@localhost/university_db"

#для підключення
engine = create_engine(SQLALCHEMY_DATABE_URL)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

#базовий клас для створення моделей
Base = declarative_base()

#залежність для отримання сесії бази даних у  ендпоінтах
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()