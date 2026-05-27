#ендпоінті арі 
from contextlib import asynccontextmanager
from fastapi_cache import FastAPICache
from fastapi_cache.backends.inmemory import InMemoryBackend
from fastapi_cache.decorator import cache
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

import models
import schemas
from database import SessionLocal, engine, get_db

#створення таблиць. Цей рядок гарантує, що SQLAlchemy синхронізується
models.Base.metadata.create_all(bind=engine)

@asynccontextmanager
async def lifespan(app: FastAPI):
    FastAPICache.init(InMemoryBackend(), prefix="fastapi-cache")
    yield

app = FastAPI(title="University Management API", lifespan=lifespan)


#ендпоінт студенти
#створення студента
@app.post("/students/", response_model=schemas.StudentResponse)
def create_student(student: schemas.StudentCreate, db: Session = Depends(get_db)):
    db_student = models.Student(**student.model_dump())
    db.add(db_student)
    db.commit()
    db.refresh(db_student)
    return db_student

#считування студента
@app.get("/students/", response_model=List[schemas.StudentResponse])
def read_students(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return db.query(models.Student).offset(skip).limit(limit).all()

#видалення студента 
@app.delete("/students/{students_id}")
def delete_student(student_id: int, db: Session = Depends(get_db)):
    student = db.query(models.Student).filter(models.Student.id == student_id).first()
    
    if not student:
        raise HTTPException(status_code=404, detail="Студента такого немає")
    
    db.delete(student)
    db.commit()
    
    return {
        "status": "success", 
        "message": f"Студента {student.first_name} {student.last_name} успішно видалено."
    }

#ендпоінти викладачі
@app.post("/teachers/", response_model=schemas.TeacherResponse)
def create_teacher(teacher: schemas.TeacherCreate, db: Session = Depends(get_db)):
    db_teacher = models.Teacher(**teacher.model_dump())
    db.add(db_teacher)
    db.commit()
    db.refresh(db_teacher)
    return db_teacher

@app.get("/teachers/", response_model=List[schemas.TeacherResponse])
def read_teachers(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return db.query(models.Teacher).offset(skip).limit(limit).all()

@app.delete("/teachers/{teacher_id}")
def delete_teacher(teacher_id: int, db: Session = Depends(get_db)):
    teacher = db.query(models.Teacher).filter(models.Teacher.id == teacher_id).first()

    if not teacher:
        raise HTTPException(status_code=404, detail="Викладача немає")

    db.delete(teacher)
    db.commit()

    return {
        "status": "success", 
        "message": f"Викладача {teacher.first_name} {teacher.last_name} успішно видалено."
    }



#ендпоінти курси
@app.post("/courses/", response_model=schemas.CourseResponse)
def create_course(course: schemas.CourseCreate, db: Session = Depends(get_db)):
    db_course = models.Course(**course.model_dump()) # type: ignore
    db.add(db_course)
    db.commit()
    db.refresh(db_course)
    return db_course

@app.get("/courses/", response_model=List[schemas.CourseResponse])
@cache(expire=60) # type: ignore
def read_courses(db: Session = Depends(get_db)):
    return db.query(models.Course).all() # type: ignore

@app.post("/courses/{course_id}/reorganize")
def reorganize_course(course_id: int, new_credits: int, db:Session = Depends(get_db)):
    try:
        #1. шукаємо курс і оновлюємо кредити
        course = db.query(models.Course).filter(models.Course.id == course_id).first()
        if not course:
            raise HTTPException(status_code=404, detail="Курс не знайдено")
        
        course.credits = new_credits # type: ignore
        
        #2.видаляємо всі старі заняття цього курсу 
        db.query(models.StudyClass).filter(models.StudyClass.course_id == course_id).delete()
        
        db.commit()
        
        return{
            "status": "success", 
            "message": f"Курс '{course.title}' реорганізовано: кредити оновлено, старі заняття видалено."
        }
    except Exception as e:
        #якщо сталося будь яка помилка
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Помилка транзакції. Зміни скасовано. Деталі: {str(e)}")
    
    


@app.delete("/courses/{course_id}")
def delete_course(course_id: int, db: Session = Depends(get_db)):
    course = db.query(models.Course).filter(models.Course.id == course_id).first()
    
    if not course:
        raise HTTPException(status_code=404, detail="Курс не знайдено")
    
    db.delete(course)
    db.commit()
    
    return {
        "status": "success", 
        "message": f"Курс '{course.title}' та всі пов'язані заняття і оцінки успішно видалено."
    }

#ендпоінти оцінок
@app.post("/grades/", response_model=schemas.GradeResponse)
def create_grade(grade: schemas.GradeCreate, db: Session = Depends(get_db)):
    #перевірка на існування студента та курса 
    student = db.query(models.Student).filter(models.Student.id == grade.student_id).first()
    course = db.query(models.Course).filter(models.Course.id == grade.course_id).first() # type: ignore
    
    if not student or not course:
        raise HTTPException(status_code=404, detail="Студента або курс не знайдено")
        
    db_grade = models.Grade(**grade.model_dump())
    db.add(db_grade)
    db.commit()
    db.refresh(db_grade)
    return db_grade

@app.get("/grades/",response_model=List[schemas.GradeResponse])
@cache(expire=60) # type: ignore
def read_grades(db: Session = Depends(get_db)):
    print("Запит до БД за оцінками.")
    return db.query(models.Grade).all()


@app.delete("/grades/{grade_id}")
def delete_grade(grade_id: int, db: Session = Depends(get_db)):
    grade = db.query(models.Grade).filter(models.Grade.id == grade_id).first()
    
    if not grade:
        raise HTTPException(status_code=404, detail="Оцінку не знайдено")
    
    db.delete(grade)
    db.commit()
    
    return {
        "status": "success", 
        "message": "Оцінку успішно видалено."
    }

