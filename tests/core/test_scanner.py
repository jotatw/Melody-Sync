import shutil

from pathlib import Path

from melody_sync.core.scanner.scanner import scan


def test_scanner_returns_empty_list_for_empty_library(
        tmp_path: Path,
) -> None:
    """
    Deve retornar uma lista vazia quando a biblioteca não possui arquivos de áudio.
    """

    # Act
    result = scan(tmp_path)

    # Assert
    assert result == []


def test_scanner_returns_one_song_for_single_audio_file(
        tmp_path: Path,
) -> None:
    """
    Deve retornar uma lista contendo uma única música.
    """

    # Arrange
    source = Path("tests/fixtures/audio/with_tags.mp3")
    destination = tmp_path / "with_tags.mp3"

    shutil.copy(source, destination)

    # Act
    result = scan(tmp_path)

    # Assert
    assert len(result) == 1


def test_scanner_returns_multiple_songs(
        tmp_path: Path,
) -> None:
    """
    Deve retornar uma lista contendo todas as músicas encontradas.
    """

    # Arrange
    source = Path("tests/fixtures/audio/with_tags.mp3")

    for name in (
            "Queen.mp3",
            "Pink Floyd.mp3",
            "Bach.mp3",
    ):
        shutil.copy(source, tmp_path / name)

    # Act
    result = scan(tmp_path)

    # Assert
    assert len(result) == 3


def test_scanner_preserves_song_path(
        tmp_path: Path,
) -> None:
    """
    Deve preservar o caminho original da música.
    """

    # Arrange
    source = Path("tests/fixtures/audio/with_tags.mp3")
    destination = tmp_path / "Queen.mp3"

    shutil.copy(source, destination)

    # Act
    result = scan(tmp_path)

    # Assert
    assert len(result) == 1
    assert result[0].path == destination


def test_scanner_loads_song_title(
        tmp_path: Path,
) -> None:
    """
    Deve retornar uma música com o título carregado.
    """

    # Arrange
    source = Path("tests/fixtures/audio/with_tags.mp3")
    destination = tmp_path / "with_tags.mp3"

    shutil.copy(source, destination)

    # Act
    result = scan(tmp_path)

    # Assert
    assert len(result) == 1
    assert result[0].title == "qualque coisa"


def test_scanner_uses_filename_when_title_is_missing(
        tmp_path: Path,
) -> None:
    """
    Deve utilizar o nome do arquivo como título quando
    não existirem metadados.
    """

    # Arrange
    source = Path("tests/fixtures/audio/no_tags.mp3")
    destination = tmp_path / "no_tags.mp3"

    shutil.copy(source, destination)

    # Act
    result = scan(tmp_path)

    # Assert
    assert len(result) == 1
    assert result[0].title == "no_tags.mp3"


def test_scanner_finds_songs_in_nested_directories(
        tmp_path: Path,
) -> None:
    """
    Deve encontrar músicas em diretórios aninhados.
    """

    # Arrange
    rock = tmp_path / "Rock"
    pop = tmp_path / "Pop"

    rock.mkdir()
    pop.mkdir()

    source = Path("tests/fixtures/audio/with_tags.mp3")

    shutil.copy(source, rock / "Queen.mp3")
    shutil.copy(source, pop / "Artist.mp3")

    # Act
    result = scan(tmp_path)

    # Assert
    assert len(result) == 2