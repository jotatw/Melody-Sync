from collections import Counter

from melody_sync.models.library_statistics import LibraryStatistics
from melody_sync.models.song import Song


def calculate_statistics(songs: list[Song]) -> LibraryStatistics:
    """
    Calcula estatísticas gerais de uma biblioteca musical.

    Args:
        songs: Lista de músicas analisadas.

    Returns:
        LibraryStatistics contendo o resumo da biblioteca.
    """

    artists = {
        song.artist.strip()
        for song in songs
        if song.artist and song.artist.strip()
    }

    albums = {
        song.album.strip()
        for song in songs
        if song.album and song.album.strip()
    }

    formats = Counter(
        song.extension
        for song in songs
    )

    total_size = sum(
        song.size
        for song in songs
    )

    total_duration = sum(
        song.duration or 0
        for song in songs
    )

    bitrates = [
        song.bitrate
        for song in songs
        if song.bitrate is not None
    ]

    average_bitrate = (
        sum(bitrates) / len(bitrates)
        if bitrates
        else 0
    )

    return LibraryStatistics(
        total_songs=len(songs),
        unique_artists=len(artists),
        unique_albums=len(albums),
        total_size=total_size,
        formats=dict(formats),
        total_duration=total_duration,
        average_bitrate=average_bitrate,
    )