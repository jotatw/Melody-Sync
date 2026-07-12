from rich.table import Table

from melody_sync.ui.console import console


def print_statistics(stats: dict):

    table = Table(title="Biblioteca Musical")

    table.add_column("Informação")
    table.add_column("Valor", justify="right")

    table.add_row(
        "Total de músicas",
        str(stats["total"])
    )

    table.add_row(
        "Artistas",
        str(stats["artists"])
    )

    table.add_row(
        "Álbuns",
        str(stats["albums"])
    )

    table.add_row(
        "Tamanho",
        f"{stats['size'] / (1024 ** 3):.2f} GB"
    )

    table.add_row(
        "Duração",
        f"{stats['duration'] / 3600:.2f} horas"
    )

    table.add_row(
        "Bitrate médio",
        f"{stats['average_bitrate'] / 1000:.0f} kbps"
    )

    console.print(table)

def print_extensions(stats):

    table = Table(
        title="Formatos"
    )

    table.add_column("Formato")

    table.add_column(
        "Quantidade",
        justify="right"
    )

    for ext, amount in sorted(
            stats["extensions"].items()
    ):

        table.add_row(
            ext.upper(),
            str(amount)
        )

    console.print(table)