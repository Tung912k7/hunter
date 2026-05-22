from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional
from decimal import Decimal

class HackathonBase(BaseModel):
    platform: str = Field(..., description="devpost/devfolio/hackerearth/gitcoin/dorahacks/bewater")
    platform_id: str = Field(..., description="Unique event ID on that platform")
    title: str = Field(..., description="Title of the hackathon")
    description: Optional[str] = Field(None, description="Tagline or description")
    url: str = Field(..., description="Hackathon web link")
    rules_url: Optional[str] = Field(None, description="Hackathon rules page link")
    prize_type: str = Field("fiat", description="'fiat' or 'crypto'")
    prize_currency: str = Field("USD", description="Currency code (e.g. USD, ETH, SOL)")
    prize_value: Decimal = Field(Decimal("0.00"), description="Total prize pool value")
    is_online: bool = Field(False, description="Is the event held online?")
    start_date: Optional[datetime] = None
    end_date: Optional[datetime] = None

class HackathonCreate(HackathonBase):
    pass

class HackathonUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    url: Optional[str] = None
    rules_url: Optional[str] = None
    prize_type: Optional[str] = None
    prize_currency: Optional[str] = None
    prize_value: Optional[Decimal] = None
    is_online: Optional[bool] = None
    start_date: Optional[datetime] = None
    end_date: Optional[datetime] = None
    is_vietnam_eligible: Optional[bool] = None
    report_count: Optional[int] = None

class HackathonResponse(HackathonBase):
    id: int
    is_vietnam_eligible: bool
    report_count: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
