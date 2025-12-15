from fastapi import Header, HTTPException, status

from config import settings


async def verify_api_key(x_api_key: str = Header(None)):
    if not x_api_key or x_api_key != settings.api_password:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing API key",
            headers={"WWW-Authenticate": "ApiKey"},
        )
    return x_api_key
