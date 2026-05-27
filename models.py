#описуємо таблиці мовою python (orm)
import datetime

from sqlalchemy import CheckConstraint, Column, Date, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import relationship
from database import Base

class Student(Base):
    __tablename__ = "students"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String(100), nullable=False)
    last_name = Column(String(100), nullable=False)
    email = Column(String(150), unique=True, index=True, nullable=False)
    enrollment_year = Column(Integer, nullable=False)
    
    grades = relationship("Grade", back_populates="student", cascade="all, delete-orphan")
    
    
class Teacher(Base):
    __tablename__ = "teachers"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String(100), nullable=False)
    last_name = Column(String(100), nullable=False)
    email = Column(String(150), unique=True, index=True, nullable=False)


    courses = relationship("Course", back_populates="teacher", cascade="all, delete-orphan")
    
class StudyClass(Base): 
    __tablename__ = "classes"

    id = Column(Integer, primary_key=True, index=True)
    course_id = Column(Integer, ForeignKey("courses.id", ondelete="CASCADE"), nullable=False)
    room_number = Column(String(50))
    schedule_time = Column(DateTime, nullable=False)

    course = relationship("Course", back_populates="classes")


class Grade(Base):
    __tablename__ = "grades"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(Integer, ForeignKey("students.id", ondelete="CASCADE"), nullable=False)
    course_id = Column(Integer, ForeignKey("courses.id", ondelete="CASCADE"), nullable=False)
    grade = Column(Integer, CheckConstraint('grade >= 1 AND grade <= 100'), nullable=False)
    date_given = Column(Date, default=datetime.date.today, nullable=False)

    student = relationship("Student", back_populates="grades")
    course = relationship("Course", back_populates="grades")
    
    
    
class Course(Base):
    __tablename__ = "courses"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(200), nullable=False)
    credits = Column(Integer, nullable=False)

    #зовнішній ключ  викладача
    teacher_id = Column(Integer, ForeignKey("teachers.id", ondelete="CASCADE"), nullable=False)

    #зв'зяки
    teacher = relationship("Teacher", back_populates="courses")
    classes = relationship("StudyClass", back_populates="course", cascade="all, delete-orphan")
    grades = relationship("Grade", back_populates="course", cascade="all, delete-orphan")