from pathlib import Path

from melody_sync.core.scanner.statistics import calculate_statistics
from melody_sync.models.song import Song


def test_statistics_returns_empty_statistics_for_empty_library() -> None:
    """
    Deve retornar estatísticas vazias para uma biblioteca sem músicas.
    """

    # Arrange
    songs = []

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.total_songs == 0
    assert statistics.unique_artists == 0
    assert statistics.unique_albums == 0
    assert statistics.total_size == 0
    assert statistics.total_duration == 0
    assert statistics.average_bitrate == 0
    assert statistics.formats == {}


def test_statistics_counts_total_songs() -> None:
    """
    Deve calcular corretamente a quantidade total de músicas.
    """

    # Arrange
    songs = [
        Song(path=Path("song1.mp3"), size=100),
        Song(path=Path("song2.mp3"), size=200),
        Song(path=Path("song3.mp3"), size=300),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.total_songs == 3


def test_statistics_counts_unique_artists() -> None:
    """
    Deve calcular corretamente a quantidade de artistas únicos.
    """

    # Arrange
    songs = [
        Song(
            path=Path("song1.mp3"),
            size=100,
            artist="Queen",
        ),
        Song(
            path=Path("song2.mp3"),
            size=200,
            artist="Queen",
        ),
        Song(
            path=Path("song3.mp3"),
            size=300,
            artist="Pink Floyd",
        ),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.unique_artists == 2


def test_statistics_counts_unique_albums() -> None:
    """
    Deve calcular corretamente a quantidade de álbuns únicos.
    """

    # Arrange
    songs = [
        Song(
            path=Path("song1.mp3"),
            size=100,
            album="Album A",
        ),
        Song(
            path=Path("song2.mp3"),
            size=200,
            album="Album A",
        ),
        Song(
            path=Path("song3.mp3"),
            size=300,
            album="Album B",
        ),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.unique_albums == 2


def test_statistics_calculates_total_size() -> None:
    """
    Deve calcular corretamente o tamanho total da biblioteca.
    """

    # Arrange
    songs = [
        Song(
            path=Path("song1.mp3"),
            size=10_000_000,
        ),
        Song(
            path=Path("song2.mp3"),
            size=20_000_000,
        ),
        Song(
            path=Path("song3.mp3"),
            size=30_000_000,
        ),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.total_size == 60_000_000


def test_statistics_calculates_total_duration() -> None:
    """
    Deve calcular corretamente a duração total da biblioteca.
    """

    # Arrange
    songs = [
        Song(
            path=Path("song1.mp3"),
            size=100,
            duration=180,
        ),
        Song(
            path=Path("song2.mp3"),
            size=100,
            duration=240,
        ),
        Song(
            path=Path("song3.mp3"),
            size=100,
            duration=120,
        ),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.total_duration == 540


def test_statistics_counts_audio_formats() -> None:
    """
    Deve calcular corretamente a quantidade de músicas por formato.
    """

    # Arrange
    songs = [
        Song(
            path=Path("song1.mp3"),
            size=100,
        ),
        Song(
            path=Path("song2.mp3"),
            size=100,
        ),
        Song(
            path=Path("song3.flac"),
            size=100,
        ),
        Song(
            path=Path("song4.wav"),
            size=100,
        ),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.formats == {
        "mp3": 2,
        "flac": 1,
        "wav": 1,
    }


def test_statistics_calculates_average_bitrate() -> None:
    """
    Deve calcular corretamente o bitrate médio da biblioteca.
    """

    # Arrange
    songs = [
        Song(
            path=Path("song1.mp3"),
            size=100,
            bitrate=320000,
        ),
        Song(
            path=Path("song2.mp3"),
            size=100,
            bitrate=160000,
        ),
        Song(
            path=Path("song3.mp3"),
            size=100,
            bitrate=128000,
        ),
    ]

    # Act
    statistics = calculate_statistics(songs)

    # Assert
    assert statistics.average_bitrate == (
            320000 + 160000 + 128000
    ) / 3