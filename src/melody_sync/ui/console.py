import typer
from rich.console import Console

console = Console()

HORIZONTAL_SEPARATOR = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"


def write(message: str = "") -> None:
    """
    Exibe uma mensagem no terminal.
    """
    typer.echo(message)


def blank_line() -> None:
    """
    Insere uma linha em branco.
    """
    write()


def separator() -> None:
    """
    Exibe um separador horizontal.
    """
    write(HORIZONTAL_SEPARATOR)


def title(text: str) -> None:
    """
    Exibe um título.
    """

    separator()
    write(text)
    separator()


def subtitle(text: str) -> None:
    """
    Exibe um subtítulo.
    """

    blank_line()
    write(text)