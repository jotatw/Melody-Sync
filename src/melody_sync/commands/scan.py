from pathlib import Path

import typer

from melody_sync.core.scanner.scanner import scan
from melody_sync.core.scanner.statistics import calculate_statistics
from melody_sync.ui.statistics import display_statistics

app = typer.Typer()


@app.command()
def run(
        folder: Path = typer.Argument(
            ...,
            exists=True,
            file_okay=False,
            dir_okay=True,
            resolve_path=True,
            help="Pasta que contém a biblioteca musical.",
        ),
) -> None:
    """
    Analisa uma biblioteca musical.
    """

    songs = scan(folder)

    statistics = calculate_statistics(songs)

    display_statistics(statistics)