from pathlib import Path

import typer

from melody_sync.services.music import scan_music

app = typer.Typer()


@app.command()
def run(
        folder: Path = typer.Argument(
            ...,
            exists=True,
            file_okay=False,
            dir_okay=True,
            resolve_path=True,
        )
):
    typer.echo("🎵 Iniciando análise...\n")

    songs = scan_music(folder)

    typer.echo(f"Encontradas {len(songs)} músicas.\n")

    for song in songs:
        typer.echo(
        f"""
        Título : {song.title}
        Artista: {song.artist}
        Álbum  : {song.album}
        Arquivo: {song.filename}
        """
        )