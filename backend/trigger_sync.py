import httpx
import time
from sqlalchemy import create_engine, text
from app.core.config import settings

def run():
    # 1. Trigger the sync endpoint
    print("Triggering POST /api/v1/hackathons/sync...")
    try:
        resp = httpx.post("http://localhost:8000/api/v1/hackathons/sync", timeout=10.0)
        print(f"Sync trigger status: {resp.status_code}")
        print(f"Sync trigger response: {resp.json()}")
    except Exception as e:
        print(f"Error triggering sync: {e}")
        return

    # 2. Query Supabase database to monitor progress
    print("\nConnecting to Supabase to monitor sync progress...")
    engine = create_engine(settings.DATABASE_URL)
    
    # Run for 90 seconds, checking every 10 seconds
    for i in range(10):
        time.sleep(10)
        with engine.connect() as conn:
            try:
                # Get total counts
                res_total = conn.execute(text("SELECT COUNT(*) FROM hackathons"))
                total_count = res_total.scalar()
                
                # Get counts per platform
                res_platforms = conn.execute(text(
                    "SELECT platform, COUNT(*) FROM hackathons GROUP BY platform ORDER BY platform"
                ))
                platform_counts = [f"{row[0]}: {row[1]}" for row in res_platforms]
                
                print(f"[{i+1}/10] Time elapsed: {(i+1)*10}s | Total hackathons in DB: {total_count} | Platforms: {', '.join(platform_counts)}")
            except Exception as e:
                print(f"Error checking DB: {e}")

    # 3. Final summary: print some DoraHacks hackathons
    print("\nFinal sync summary of DoraHacks hackathons:")
    with engine.connect() as conn:
        try:
            res_dora = conn.execute(text(
                "SELECT platform, title, prize_value, prize_currency, url FROM hackathons WHERE platform='dorahacks' LIMIT 5"
            ))
            rows = list(res_dora)
            print(f"Found {len(rows)} DoraHacks hackathons:")
            for row in rows:
                print(f"- Title: {row[1]} | Prize: {row[2]} {row[3]} | URL: {row[4]}")
        except Exception as e:
            print(f"Error querying DoraHacks: {e}")

if __name__ == "__main__":
    run()
