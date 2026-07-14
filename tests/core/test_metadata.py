from pathlib import Path

from melody_sync.models.song import Song
from melody_sync.core.scanner.metadata import read_metadata


def test_metadata_uses_filename_when_title_is_missing() -> None:
    """
    Deve utilizar o nome do arquivo quando a tag 'title'
    não estiver presente.
    """

    # Arrange
    song = Song(
        path=Path("tests/fixtures/audio/no_tags.mp3"),
        size=0,
    )

    # Act
    result = read_metadata(song)

    # Assert
    assert result.title == "no_tags.mp3"


def test_metadata_returns_none_when_artist_is_missing(
        song_without_tags: Song,
) -> None:
    """
    Deve manter o artista como None quando a tag
    'artist' não estiver presente.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.artist is None


def test_metadata_returns_none_when_album_is_missing(
        song_without_tags: Song,
) -> None:
    """
    Deve manter o álbum como None quando a tag
    'album' não estiver presente.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.album is None


def test_metadata_reads_audio_duration(
        song_without_tags: Song,
) -> None:
    """
    Deve ler corretamente a duração do arquivo.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.duration is not None
    assert result.duration > 0


def test_metadata_reads_audio_bitrate(
        song_without_tags: Song,
) -> None:
    """
    Deve ler corretamente o bitrate do arquivo.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.bitrate is not None
    assert result.bitrate > 0


def test_metadata_reads_audio_sample_rate(
        song_without_tags: Song,
) -> None:
    """
    Deve ler corretamente a taxa de amostragem do arquivo.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.sample_rate is not None
    assert result.sample_rate > 0


def test_metadata_reads_audio_channels(
        song_without_tags: Song,
) -> None:
    """
    Deve ler corretamente a quantidade de canais do arquivo.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.channels is not None
    assert result.channels > 0


def test_metadata_reads_audio_codec(
        song_without_tags: Song,
) -> None:
    """
    Deve identificar corretamente o codec do arquivo.
    """

    # Act
    result = read_metadata(song_without_tags)

    # Assert
    assert result.codec is not None
    assert isinstance(result.codec, str)


def test_metadata_reads_title(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente o título do arquivo.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.title == "qualque coisa"


def test_metadata_reads_artist(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente o artista do arquivo.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.artist == "alguem"


def test_metadata_reads_album(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente o álbum do arquivo.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.album == "teste"


def test_metadata_reads_audio_duration_with_tags(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente a duração de um arquivo com metadados.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.duration is not None
    assert result.duration > 0


def test_metadata_reads_audio_bitrate_with_tags(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente o bitrate de um arquivo com metadados.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.bitrate is not None
    assert result.bitrate > 0


def test_metadata_reads_audio_sample_rate_with_tags(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente a taxa de amostragem de um arquivo com metadados.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.sample_rate is not None
    assert result.sample_rate > 0


def test_metadata_reads_audio_channels_with_tags(
        song_with_tags: Song,
) -> None:
    """
    Deve ler corretamente a quantidade de canais de um arquivo com metadados.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.channels is not None
    assert result.channels > 0


def test_metadata_reads_audio_codec_with_tags(
        song_with_tags: Song,
) -> None:
    """
    Deve identificar corretamente o codec de um arquivo com metadados.
    """

    # Act
    result = read_metadata(song_with_tags)

    # Assert
    assert result.codec is not None
    assert isinstance(result.codec, str)