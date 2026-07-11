from pathlib import Path

import typer

from melody_sync.services.music import scan_music
from melody_sync.services.statistics import generate_statistics

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
        )
):
    typer.echo("🎵 Iniciando análise...\n")

    songs = scan_music(folder)

    stats = generate_statistics(songs)

    typer.echo("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    typer.echo("📚 Biblioteca Musical")
    typer.echo("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

    typer.echo(f"Total de músicas : {stats['total']}")
    typer.echo(f"Artistas únicos  : {stats['artists']}")
    typer.echo(f"Álbuns           : {stats['albums']}")

    typer.echo()

    typer.echo("Formatos encontrados")

    for extension, quantity in sorted(stats["extensions"].items()):
        typer.echo(f"{extension.upper():6} : {quantity}")

    typer.echo()

    typer.echo(
        f"Tamanho total    : {stats['size'] / (1024 ** 3):.2f} GB"
    )

    typer.echo(
        f"Duração total    : {stats['duration'] / 3600:.2f} horas"
    )

    typer.echo(
        f"Bitrate médio    : {stats['average_bitrate'] / 1000:.0f} kbps"
    )