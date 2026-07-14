from pathlib import Path

from melody_sync.core.scanner.discovery import discover
from melody_sync.core.scanner.metadata import read_metadata
from melody_sync.models.song import Song


def scan(directory: Path) -> list[Song]:
    """
    Percorre uma biblioteca musical e retorna uma lista de objetos Song.
    """

    songs: list[Song] = []

    for path in discover(directory):
        song = Song(
            path=path,
            size=path.stat().st_size,
        )

        songs.append(read_metadata(song))

    return songs