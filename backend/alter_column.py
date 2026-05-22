from sqlalchemy import create_engine, text
from app.core.config import settings

engine = create_engine(settings.DATABASE_URL)
print(f"Connecting to DB: {settings.DATABASE_URL}")

with engine.connect() as conn:
    print("Altering title column to TEXT...")
    conn.execute(text("ALTER TABLE hackathons ALTER COLUMN title TYPE TEXT;"))
    print("Altering url column to TEXT...")
    conn.execute(text("ALTER TABLE hackathons ALTER COLUMN url TYPE TEXT;"))
    print("Altering rules_url column to TEXT...")
    conn.execute(text("ALTER TABLE hackathons ALTER COLUMN rules_url TYPE TEXT;"))
    print("Altering platform_id column to TEXT...")
    conn.execute(text("ALTER TABLE hackathons ALTER COLUMN platform_id TYPE TEXT;"))
    conn.commit()

print("Altered columns to TEXT successfully!")
