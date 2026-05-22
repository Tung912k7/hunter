from app.models.hackathon import Hackathon

def test_create_and_get_hackathons(client, db_session):
    h1 = Hackathon(
        platform="devpost",
        platform_id="dp-1",
        title="Devpost Hack",
        description="Great devpost hackathon",
        url="https://devpost.com/1",
        prize_type="fiat",
        prize_currency="USD",
        prize_value=10000.0,
        is_online=True,
        is_vietnam_eligible=True,
        report_count=0
    )
    h2 = Hackathon(
        platform="gitcoin",
        platform_id="gc-1",
        title="Gitcoin Grant",
        description="Crypto grant round",
        url="https://gitcoin.co/1",
        prize_type="crypto",
        prize_currency="ETH",
        prize_value=50000.0,
        is_online=True,
        is_vietnam_eligible=True,
        report_count=0
    )
    db_session.add(h1)
    db_session.add(h2)
    db_session.commit()

    # Query all
    response = client.get("/api/v1/hackathons")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2

    # Query with is_vietnam_eligible
    response = client.get("/api/v1/hackathons?is_vietnam_eligible=true")
    assert response.status_code == 200
    assert len(response.json()) == 2

    # Filter by platform
    response = client.get("/api/v1/hackathons?platforms=devpost")
    assert response.status_code == 200
    assert len(response.json()) == 1
    assert response.json()[0]["platform"] == "devpost"

    # Filter by prize_type
    response = client.get("/api/v1/hackathons?prize_type=crypto")
    assert response.status_code == 200
    assert len(response.json()) == 1
    assert response.json()[0]["platform"] == "gitcoin"

    # Filter by min_prize_value
    response = client.get("/api/v1/hackathons?min_prize_value=20000")
    assert response.status_code == 200
    assert len(response.json()) == 1
    assert response.json()[0]["platform"] == "gitcoin"

    # Search query
    response = client.get("/api/v1/hackathons?query=devpost")
    assert response.status_code == 200
    assert len(response.json()) == 1
    assert "Devpost" in response.json()[0]["title"]

def test_report_moderation(client, db_session):
    h = Hackathon(
        platform="devfolio",
        platform_id="df-1",
        title="Devfolio Hack",
        description="Some devfolio hackathon",
        url="https://devfolio.co/1",
        prize_type="fiat",
        prize_currency="USD",
        prize_value=5000.0,
        is_online=True,
        is_vietnam_eligible=True,
        report_count=0
    )
    db_session.add(h)
    db_session.commit()
    db_session.refresh(h)

    hackathon_id = h.id

    # First report
    response = client.post(f"/api/v1/hackathons/{hackathon_id}/report")
    assert response.status_code == 200
    data = response.json()
    assert data["report_count"] == 1
    assert data["is_vietnam_eligible"] is True

    # Second report
    response = client.post(f"/api/v1/hackathons/{hackathon_id}/report")
    assert response.status_code == 200
    data = response.json()
    assert data["report_count"] == 2
    assert data["is_vietnam_eligible"] is True

    # Third report -> should toggle is_vietnam_eligible to False
    response = client.post(f"/api/v1/hackathons/{hackathon_id}/report")
    assert response.status_code == 200
    data = response.json()
    assert data["report_count"] == 3
    assert data["is_vietnam_eligible"] is False

    # Fourth report -> should remain False
    response = client.post(f"/api/v1/hackathons/{hackathon_id}/report")
    assert response.status_code == 200
    data = response.json()
    assert data["report_count"] == 4
    assert data["is_vietnam_eligible"] is False

def test_report_not_found(client):
    response = client.post("/api/v1/hackathons/99999/report")
    assert response.status_code == 404
    assert response.json()["detail"] == "Hackathon not found"

def test_sync_hackathons(client):
    response = client.post("/api/v1/hackathons/sync")
    assert response.status_code == 200
    assert response.json() == {"status": "Sync initiated"}

