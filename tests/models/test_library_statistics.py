import pytest

from melody_sync.models.library_statistics import LibraryStatistics

# ==========================================================
# State
# ==========================================================
def test_statistics_detects_empty_library(
        sample_statistics: LibraryStatistics,
        empty_statistics: LibraryStatistics,
) -> None:
    """
    Deve identificar corretamente uma biblioteca vazia.
    """

    assert sample_statistics.is_empty is False
    assert empty_statistics.is_empty is True


# ==========================================================
# Storage
# ==========================================================
def test_statistics_calculates_total_size_in_mb(
        sample_statistics: LibraryStatistics,
) -> None:
    """
    Deve calcular corretamente o tamanho total em megabytes.
    """

    expected_size_mb = sample_statistics.total_size / (1024 ** 2)

    assert (
            sample_statistics.total_size_mb
            == pytest.approx(expected_size_mb)
    )


# ==========================================================
# Storage
# ==========================================================
def test_statistics_calculates_total_size_in_gb(
        sample_statistics: LibraryStatistics,
) -> None:
    """
    Deve calcular corretamente o tamanho total em gigabytes.
    """

    expected_size_gb = sample_statistics.total_size / (1024 ** 3)

    assert (
            sample_statistics.total_size_gb
            == pytest.approx(expected_size_gb)
    )


# ==========================================================
# Time
# ==========================================================
def test_statistics_calculates_total_duration_in_minutes(
        sample_statistics: LibraryStatistics,
) -> None:
    """
    Deve calcular corretamente a duração total em minutos.
    """

    expected_duration = sample_statistics.total_duration / 60

    assert (
            sample_statistics.total_duration_minutes
            == pytest.approx(expected_duration)
    )


# ==========================================================
# Time
# ==========================================================
def test_statistics_calculates_total_duration_in_hours(
        sample_statistics: LibraryStatistics,
) -> None:
    """
    Deve calcular corretamente a duração total em horas.
    """

    expected_duration = sample_statistics.total_duration / 3600

    assert (
            sample_statistics.total_duration_hours
            == pytest.approx(expected_duration)
    )


# ==========================================================
# Time
# ==========================================================
def test_statistics_calculates_average_duration(
        sample_statistics: LibraryStatistics,
        empty_statistics: LibraryStatistics,
) -> None:
    """
    Deve calcular corretamente a duração média das músicas.
    """

    expected_average = (
            sample_statistics.total_duration
            / sample_statistics.total_songs
    )

    assert (
            sample_statistics.average_duration
            == pytest.approx(expected_average)
    )

    assert empty_statistics.average_duration == 0.0