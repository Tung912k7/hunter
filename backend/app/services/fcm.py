import os
import logging
from typing import Dict, Any

from app.core.config import settings

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

firebase_initialized = False

# Try initializing Firebase Admin SDK
try:
    import firebase_admin
    from firebase_admin import credentials, messaging
    
    cred_path = settings.FIREBASE_CREDENTIALS_PATH
    if cred_path:
        if not os.path.isabs(cred_path):
            # Resolve relative to backend base dir
            base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            resolved_path = os.path.join(base_dir, cred_path)
        else:
            resolved_path = cred_path

        if os.path.exists(resolved_path):
            cred = credentials.Certificate(resolved_path)
            firebase_admin.initialize_app(cred)
            firebase_initialized = True
            logger.info("Firebase Admin SDK successfully initialized.")
        else:
            logger.warning(f"Firebase credentials JSON not found at: {resolved_path}. Using mock fallback.")
    else:
        logger.warning("FIREBASE_CREDENTIALS_PATH is not configured. Using mock fallback.")
except ImportError:
    logger.warning("firebase-admin package not installed. Using mock fallback.")
except Exception as e:
    logger.warning(f"Failed to initialize Firebase Admin SDK: {e}. Using mock fallback.")


class FCMService:
    @staticmethod
    def send_hackathon_alert(device_token: str, payload: Dict[str, Any]) -> bool:
        """
        Send high-priority FCM Data Message to a single device token.
        Values in the payload are automatically stringified.
        """
        stringified_payload = {k: str(v) for k, v in payload.items()}
        
        if not firebase_initialized:
            logger.info(f"[MOCK FCM] Sending message to token {device_token} with payload: {stringified_payload}")
            return True
            
        try:
            message = messaging.Message(
                data=stringified_payload,
                token=device_token,
                android=messaging.AndroidConfig(
                    priority="high"
                )
            )
            response = messaging.send(message)
            logger.info(f"Successfully sent FCM message: {response}")
            return True
        except Exception as e:
            logger.error(f"Failed to send FCM message: {e}")
            return False

    @staticmethod
    def broadcast_hackathon_alert(topic: str, payload: Dict[str, Any]) -> bool:
        """
        Broadcast high-priority FCM Data Message to an FCM topic.
        Values in the payload are automatically stringified.
        """
        stringified_payload = {k: str(v) for k, v in payload.items()}
        
        if not firebase_initialized:
            logger.info(f"[MOCK FCM] Broadcasting message to topic '{topic}' with payload: {stringified_payload}")
            return True
            
        try:
            message = messaging.Message(
                data=stringified_payload,
                topic=topic,
                android=messaging.AndroidConfig(
                    priority="high"
                )
            )
            response = messaging.send(message)
            logger.info(f"Successfully broadcasted FCM message to topic '{topic}': {response}")
            return True
        except Exception as e:
            logger.error(f"Failed to broadcast FCM message to topic '{topic}': {e}")
            return False
