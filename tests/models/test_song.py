from pathlib import Path

import pytest

from melody_sync.models.song import Song

# ==========================================================
# Path Properties
# ==========================================================
def test_song_returns_correct_filename(
        sample_song: Song,
) -> None:
    """
    Deve retornar corretamente o nome do arquivo.
    """

    assert (
            sample_song.filename
            == "Queen - Bohemian Rhapsody.mp3"
    )


# ==========================================================
# Path Properties
# ==========================================================
def test_song_returns_correct_extension(
        sample_song: Song,
) -> None:
    """
    Deve retornar corretamente a extensão do arquivo.
    """

    assert sample_song.extension == "mp3"


# ==========================================================
# Path Properties
# ==========================================================
def test_song_returns_correct_directory(
        sample_song: Song,
) -> None:
    """
    Deve retornar corretamente o diretório do arquivo.
    """

    assert sample_song.directory == Path("/music")


# ==========================================================
# Domain Rules
# ==========================================================
def test_song_detects_metadata_presence(
        sample_song: Song,
        empty_song: Song,
) -> None:
    """
    Deve identificar corretamente a presença de metadados.
    """

    assert sample_song.has_metadata is True
    assert empty_song.has_metadata is False


# ==========================================================
# Calculated Properties
# ==========================================================
def test_song_calculates_duration_in_minutes(
        sample_song: Song,
) -> None:
    """
    Deve calcular corretamente a duração em minutos.
    """

    expected_duration = sample_song.duration / 60

    assert (
            sample_song.duration_minutes
            == pytest.approx(expected_duration)
    )


# ==========================================================
# Calculated Properties
# ==========================================================
def test_song_calculates_size_in_mb(
        sample_song: Song,
) -> None:
    """
    Deve calcular corretamente o tamanho em megabytes.
    """

    expected_size_mb = sample_song.size / (1024 ** 2)

    assert sample_song.size_mb == pytest.approx(expected_size_mb)


# ==========================================================
# Calculated Properties
# ==========================================================
def test_song_calculates_size_in_gb(
        sample_song: Song,
) -> None:
    """
    Deve calcular corretamente o tamanho em gigabytes.
    """

    expected_size_gb = sample_song.size / (1024 ** 3)

    assert sample_song.size_gb == pytest.approx(expected_size_gb)


# ==========================================================
# Domain Rules
# ==========================================================
def test_song_identifies_lossless_audio(
        sample_song: Song,
        lossless_song: Song,
) -> None:
    """
    Deve identificar corretamente formatos com e sem perdas.
    """

    assert sample_song.is_lossless is False
    assert lossless_song.is_lossless is True
