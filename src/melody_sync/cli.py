import typer

from melody_sync.commands import scan

app = typer.Typer()

app.add_typer(
    scan.app,
    name="scan",
    help="Analisa a biblioteca musical."
)


@app.command()
def version():
    typer.echo("Melody Sync v0.1.0")