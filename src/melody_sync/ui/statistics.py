from melody_sync.models.library_statistics import LibraryStatistics

from melody_sync.ui.console import (
    blank_line,
    subtitle,
    title,
    write,
)


def display_statistics(
        statistics: LibraryStatistics,
) -> None:
    """
    Exibe o resumo estatístico da biblioteca musical.
    """

    title("📚 Biblioteca Musical")
    blank_line()

    write(
        f"Total de músicas : {statistics.total_songs}"
    )

    write(
        f"Artistas únicos  : {statistics.unique_artists}"
    )

    write(
        f"Álbuns           : {statistics.unique_albums}"
    )
    blank_line()

    subtitle("Formatos encontrados")
    blank_line()

    for extension, quantity in sorted(
            statistics.formats.items()
    ):
        write(
            f"{extension.upper():6} : {quantity}"
        )
    blank_line()

    write(
        f"Tamanho total    : {statistics.total_size_gb:.2f} GB"
    )

    write(
        f"Duração total    : "
        f"{statistics.total_duration_hours:.2f} horas"
    )

    write(
        f"Bitrate médio    : "
        f"{statistics.average_bitrate / 1000:.0f} kbps"
    )