from mutagen import File

from melody_sync.models.song import Song


def read_metadata(song: Song) -> Song:

    audio = File(song.path, easy=True)

    full_audio = File(song.path)

    if audio is None:
        return song

    song.title = (
        audio.get("title", [song.filename])[0]
    )

    song.artist = audio.get("artist", [None])[0]

    song.album = audio.get("album", [None])[0]

    if full_audio is not None and full_audio.info is not None:

        info = full_audio.info

        song.duration = getattr(info, "length", None)
        song.bitrate = getattr(info, "bitrate", None)
        song.sample_rate = getattr(info, "sample_rate", None)
        song.channels = getattr(info, "channels", None)

        song.codec = type(info).__name__

    return song