# Stage 1: Build dependencies
FROM python:3.11-slim as builder

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# Stage 2: Final runtime environment
FROM python:3.11-slim as runner

WORKDIR /app

# Install system dependencies for Playwright
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Copy installed python packages from builder
COPY --from=builder /root/.local /root/.local
ENV PATH=/root/.local/bin:$PATH

# Copy application files
COPY . .

# Install playwright system dependencies
RUN python -m playwright install --with-deps chromium

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
