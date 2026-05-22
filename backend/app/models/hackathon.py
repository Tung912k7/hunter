from sqlalchemy import Column, Integer, String, Boolean, Numeric, DateTime, UniqueConstraint, func
from app.core.database import Base

class Hackathon(Base):
    __tablename__ = "hackathons"

    id = Column(Integer, primary_key=True, index=True)
    platform = Column(String(50), nullable=False, index=True)  # devpost, devfolio, hackerearth, gitcoin, dorahacks, bewater
    platform_id = Column(String, nullable=False, index=True)
    title = Column(String, nullable=False)
    description = Column(String, nullable=True)
    url = Column(String, nullable=False)
    rules_url = Column(String, nullable=True)
    prize_type = Column(String(50), nullable=False, default="fiat")  # "fiat" or "crypto"
    prize_currency = Column(String(20), nullable=False, default="USD")
    prize_value = Column(Numeric(15, 2), nullable=False, default=0.0)
    is_online = Column(Boolean, nullable=False, default=False)
    start_date = Column(DateTime(timezone=True), nullable=True)
    end_date = Column(DateTime(timezone=True), nullable=True)
    is_vietnam_eligible = Column(Boolean, nullable=False, default=True)
    report_count = Column(Integer, nullable=False, default=0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    __table_args__ = (
        UniqueConstraint("platform", "platform_id", name="uq_platform_platform_id"),
    )
