from pathlib import Path

import typer

from melody_sync.services.music import scan_music

app = typer.Typer()


@app.command()
def run(
        folder: Path = typer.Argument(..., exists=True)
):
    files = scan_music(folder)

    typer.echo(f"\nEncontradas {len(files)} músicas.\n")

    for file in files:
        typer.echo(file)