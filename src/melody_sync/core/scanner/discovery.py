from pathlib import Path

SUPPORTED_AUDIO_SUFFIXES: set[str] = {
    ".aac",
    ".flac",
    ".m4a",
    ".mp3",
    ".ogg",
    ".opus",
    ".wav",
}


def discover(directory: Path) -> list[Path]:
    """
    Encontra todos os arquivos de áudio suportados em um diretório.

    Args:
        directory:
            Diretório da biblioteca musical.

    Returns:
        Lista de músicas encontradas.
    """

    if not directory.exists():
        raise FileNotFoundError(directory)

    if not directory.is_dir():
        raise NotADirectoryError(directory)

    return sorted(
        file
        for file in directory.rglob("*")
        if file.is_file()
        and file.suffix.lower() in SUPPORTED_AUDIO_SUFFIXES
    )