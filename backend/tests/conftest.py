import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from fastapi.testclient import TestClient

from app.core.database import Base
from app.api import deps
from app.main import app

# Use a local SQLite file for tests
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"

@pytest.fixture(scope="session", autouse=True)
def test_db():
    engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    
    # Create the tables
    Base.metadata.create_all(bind=engine)
    
    yield TestingSessionLocal
    
    # Clean up
    Base.metadata.drop_all(bind=engine)

@pytest.fixture(scope="function")
def db_session(test_db):
    session = test_db()
    try:
        yield session
    finally:
        session.rollback()
        session.close()

@pytest.fixture(scope="function")
def client(db_session):
    def override_get_db():
        try:
            yield db_session
        finally:
            pass
            
    app.dependency_overrides[deps.get_db] = override_get_db
    with TestClient(app) as c:
        yield c
    app.dependency_overrides.clear()
