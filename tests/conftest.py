from pathlib import Path

import pytest

from melody_sync.models.song import Song
from melody_sync.models.library_statistics import LibraryStatistics


# ==========================================================
# Song Fixtures
# ==========================================================

@pytest.fixture
def sample_song() -> Song:
    """
    Retorna uma música com metadados completos.
    """

    return Song(
        path=Path("/music/Queen - Bohemian Rhapsody.mp3"),
        size=12_500_000,
        title="Bohemian Rhapsody",
        artist="Queen",
        album="A Night at the Opera",
        duration=354.0,
        bitrate=320000,
    )


@pytest.fixture
def lossless_song() -> Song:
    """
    Retorna uma música em formato sem perdas.
    """

    return Song(
        path=Path("/music/Pink Floyd - Time.flac"),
        size=45_000_000,
        title="Time",
        artist="Pink Floyd",
        album="The Dark Side of the Moon",
        duration=412.0,
        bitrate=1000000,
    )


@pytest.fixture
def empty_song() -> Song:
    """
    Retorna uma música sem metadados.
    """

    return Song(
        path=Path("/music/Unknown.mp3"),
        size=0,
    )


# ==========================================================
# LibraryStatistics Fixtures
# ==========================================================

@pytest.fixture
def sample_statistics() -> LibraryStatistics:
    """
    Retorna estatísticas de uma biblioteca musical.
    """

    return LibraryStatistics(
        total_songs=120,
        unique_artists=30,
        unique_albums=20,
        total_size=1024 ** 3,
        formats={
            ".mp3": 80,
            ".flac": 40,
        },
        total_duration=7200.0,
        average_bitrate=320000,
    )


@pytest.fixture
def empty_statistics() -> LibraryStatistics:
    """
    Retorna uma biblioteca vazia.
    """

    return LibraryStatistics(
        total_songs=0,
        unique_artists=0,
        unique_albums=0,
        total_size=0,
        formats={},
        total_duration=0.0,
        average_bitrate=0.0,
    )


@pytest.fixture
def song_without_tags() -> Song:
    return Song(
        path=Path("tests/fixtures/audio/no_tags.mp3"),
        size=0,
    )


@pytest.fixture
def song_with_tags() -> Song:
    return Song(
        path=Path("tests/fixtures/audio/with_tags.mp3"),
        size=0,
    )
