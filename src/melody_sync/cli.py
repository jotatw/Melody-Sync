import typer

app = typer.Typer()


@app.command()
def hello():
    typer.echo("Olá! Bem-vindo ao Melody Sync!")

@app.command()
def version():
    typer.echo("Melody Sync v0.1.0")

if __name__ == "__main__":
    app()