from pathlib import Path

AUDIO_EXTENSIONS = {
    ".mp3",
    ".flac",
    ".m4a",
    ".opus",
    ".ogg",
    ".wav",
}

def scan_music(directory: Path) -> list[Path]:
    music_files = []

    for file in directory.rglob("*"):

        if (
                file.is_file()
                and file.suffix.lower() in AUDIO_EXTENSIONS
        ):
            music_files.append(file)

    return music_files