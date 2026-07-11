from mutagen import File

from melody_sync.models.song import Song


def load_metadata(song: Song) -> Song:

    audio = File(song.path, easy=True)

    if audio is None:
        return song

    song.title = (
        audio.get("title", [song.filename])[0]
    )

    song.artist = (
        audio.get("artist", ["Unknown Artist"])[0]
    )

    song.album = (
        audio.get("album", ["Unknown Album"])[0]
    )

    return song