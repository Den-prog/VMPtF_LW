#схеми для перепвірки вхідних/вихідних json даних
from pydantic import BaseModel, ConfigDict
from datetime import date
from typing import List, Optional


class StudentBase(BaseModel):
    first_name: str
    last_name: str
    email: str
    enrollment_year: int

class StudentCreate(StudentBase):
    pass

class StudentResponse(StudentBase):
    id: int
    
    # конфіг дозволяє Pydantic читати дані з об'єктів SQLAlchemy
    model_config = ConfigDict(from_attributes=True)

class TeacherBase(BaseModel):
    first_name: str
    last_name: str
    email: str

class TeacherCreate(TeacherBase):
    pass  

class TeacherResponse(TeacherBase):
    id:int
    
    model_config = ConfigDict(from_attributes=True)




class CourseBase(BaseModel):
    title: str
    credits: int
    teacher_id: int

class CourseCreate(CourseBase):
    pass

class CourseResponse(CourseBase):
    id: int
    model_config = ConfigDict(from_attributes=True)


class GradeBase(BaseModel):
    student_id: int
    course_id: int
    grade: int

class GradeCreate(GradeBase):
    pass

class GradeResponse(GradeBase):
    id: int
    date_given: date
    model_config = ConfigDict(from_attributes=True)