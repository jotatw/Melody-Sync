from collections import Counter

from melody_sync.models.song import Song


def generate_statistics(songs: list[Song]) -> dict:

    extensions = Counter()

    total_size = 0

    artists = set()

    albums = set()

    bitrate_sum = 0

    bitrate_count = 0

    duration_sum = 0

    for song in songs:

        extensions[song.extension] += 1

        total_size += song.size

        if song.artist:
            artists.add(song.artist)

        if song.album:
            albums.add(song.album)

        if song.duration:
            duration_sum += song.duration

        if song.bitrate:
            bitrate_sum += song.bitrate
            bitrate_count += 1

    average_bitrate = (
        bitrate_sum / bitrate_count
        if bitrate_count
        else 0
    )

    return {
        "total": len(songs),
        "extensions": extensions,
        "size": total_size,
        "artists": len(artists),
        "albums": len(albums),
        "duration": duration_sum,
        "average_bitrate": average_bitrate,
    }