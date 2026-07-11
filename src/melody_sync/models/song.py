from dataclasses import dataclass
from pathlib import Path


@dataclass
class Song:
    path: Path
    filename: str
    extension: str
    size: int

    title: str | None = None
    artist: str | None = None
    album: str | None = None

    duration: float | None = None
    bitrate: int | None = None
    codec: str | None = None

    @property
    def size_mb(self) -> float:
        return self.size / (1024 * 1024)