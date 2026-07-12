from rich.panel import Panel

from melody_sync.ui.console import console


def print_banner():

    console.print(
        Panel.fit(
            "[bold cyan]Melody Sync[/bold cyan]\n"
            "[green]Music Library Manager[/green]",
            border_style="cyan"
        )
    )