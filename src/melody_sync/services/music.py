from pathlib import Path

from melody_sync.models.song import Song
from melody_sync.services.metadata import load_metadata

AUDIO_EXTENSIONS = {
    ".mp3",
    ".flac",
    ".m4a",
    ".opus",
    ".ogg",
    ".wav",
}


def scan_music(directory: Path) -> list[Song]:
    """
    Procura recursivamente por arquivos de áudio
    dentro da pasta informada.
    """

    music_files: list[Song] = []

    for file in directory.rglob("*"):

        if (
                file.is_file()
                and file.suffix.lower() in AUDIO_EXTENSIONS
        ):

            song = Song(
                path=file,
                filename=file.name,
                extension=file.suffix.lower(),
                size=file.stat().st_size,
            )

            song = load_metadata(song)

            music_files.append(song)

    return music_files