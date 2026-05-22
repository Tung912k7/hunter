# Debug Report: Supabase Connection String Truncation during Hackathon Synchronization

## Bug Characterization
| Attribute   | Value          |
| ----------- | -------------- |
| Description | Synchronization fails during DoraHacks scraping due to a PostgreSQL `StringDataRightTruncation` error because the scraped hackathon title exceeds the `VARCHAR(255)` limit of the database schema. Subsequent scraper sync operations are also broken due to unhandled transaction rollback states. |
| Severity    | High           |
| Reproduction | Trigger the synchronization API endpoint `POST /api/v1/hackathons/sync`. When the `DoraHacksScraper` runs, it scrapes the target website and parses a hackathon card title that contains concatenated tags/metadata (exceeding 255 characters). The database tries to insert the record, resulting in a database-level `StringDataRightTruncation` exception. |

## Root Cause
**The root cause is two-fold:**
1. **Schema Limit:** The database model `Hackathon` in `backend/app/models/hackathon.py` defines the `title` column as `String(255)` (equivalent to `VARCHAR(255)` in PostgreSQL). During DoraHacks scraping, the scraper extracts a highly verbose card title string that exceeds 255 characters.
2. **Fragile Transaction Management:** In `backend/app/api/v1/endpoints/hackathons.py`, the sync loops run `db.commit()` inside a `try/except IntegrityError` block. Because `StringDataRightTruncation` raises a subclass of `DataError` (not `IntegrityError`), the exception is not caught locally, terminating the DoraHacks insert loop. The transaction is left un-rolled-back in a failed state, which contaminates the SQLAlchemy session and breaks subsequent inserts (e.g. `BeWaterScraper` inserts) with a transaction state error.

## Fix
1. **Model Modification:**
   Change the `title` column in `backend/app/models/hackathon.py` to use an unbounded `String` (which maps to `TEXT` or unbounded `VARCHAR` in PostgreSQL, allowing arbitrarily long titles):
   ```python
   title = Column(String, nullable=False)
   ```
2. **Database Migration:**
   Execute a DDL statement on Supabase to alter the existing column type from `VARCHAR(255)` to `TEXT`:
   ```sql
   ALTER TABLE hackathons ALTER COLUMN title TYPE TEXT;
   ```
3. **Resilient Sync Transaction Handling:**
   Improve the exception handling inside `run_sync_task` in `backend/app/api/v1/endpoints/hackathons.py` to catch any database-level exceptions (or `Exception`), log the error, and perform `db.rollback()` to ensure subsequent items or scrapers are not blocked by a corrupted transaction state:
   ```python
   try:
       db.commit()
   except Exception as e:
       logger.error(f"Failed to commit event {event['title']}: {e}")
       db.rollback()
   ```

## Verification
- [ ] Test case added
- [ ] Regression checked
