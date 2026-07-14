from pathlib import Path

import pytest

from melody_sync.core.scanner.discovery import discover

def test_discovery_finds_single_audio_file(
        tmp_path: Path,
) -> None:
    """
    Deve encontrar um único arquivo de áudio válido.
    """
    # Arrange
    expected_file = tmp_path / "Queen.mp3"
    expected_file.touch()

    # Act
    result = discover(tmp_path)

    # Assert
    assert isinstance(result, list)
    assert len(result) == 1
    assert result[0] == expected_file


def test_discovery_finds_multiple_audio_files(
        tmp_path: Path,
) -> None:
    """
    Deve encontrar múltiplos arquivos de áudio válidos.
    """

    # Arrange
    expected_files = [
        tmp_path / "Queen.mp3",
        tmp_path / "Pink Floyd.flac",
        tmp_path / "Metallica.wav",
        ]

    for file in expected_files:
        file.touch()

    # Act
    result = discover(tmp_path)

    # Assert
    assert len(result) == len(expected_files)

    for file in expected_files:
        assert file in result


def test_discovery_returns_empty_list_for_empty_directory(
        tmp_path: Path,
) -> None:
    """
    Deve retornar uma lista vazia quando o diretório não contém arquivos de áudio.
    """

    # Act
    result = discover(tmp_path)

    # Assert
    assert result == []


def test_discovery_ignores_unsupported_files(
        tmp_path: Path,
) -> None:
    """
    Deve ignorar arquivos com extensões não suportadas.
    """

    # Arrange
    supported_files = [
        tmp_path / "Queen.mp3",
        tmp_path / "Pink Floyd.flac",
        ]

    unsupported_files = [
        tmp_path / "image.png",
        tmp_path / "notes.txt",
        tmp_path / "video.mp4",
        tmp_path / "document.pdf",
        ]

    for file in supported_files + unsupported_files:
        file.touch()

    # Act
    result = discover(tmp_path)

    # Assert
    assert len(result) == len(supported_files)

    for file in supported_files:
        assert file in result

    for file in unsupported_files:
        assert file not in result


def test_discovery_finds_audio_files_in_nested_directories(
        tmp_path: Path,
) -> None:
    """
    Deve encontrar arquivos de áudio em diretórios aninhados.
    """

    # Arrange
    (tmp_path / "Queen.mp3").touch()

    rock = tmp_path / "Rock"
    rock.mkdir()

    (rock / "Pink Floyd.flac").touch()
    (rock / "Metallica.mp3").touch()

    classical = tmp_path / "Classical"
    classical.mkdir()

    (classical / "Bach.wav").touch()

    # Act
    result = discover(tmp_path)

    # Assert
    assert len(result) == 4


def test_discovery_accepts_uppercase_extensions(
        tmp_path: Path,
) -> None:
    """
    Deve aceitar arquivos com extensões em letras maiúsculas ou mistas.
    """

    # Arrange
    expected_files = [
        tmp_path / "Queen.MP3",
        tmp_path / "Pink Floyd.FlAc",
        tmp_path / "Metallica.WAV",
        ]

    for file in expected_files:
        file.touch()

    # Act
    result = discover(tmp_path)

    # Assert
    assert len(result) == len(expected_files)

    for file in expected_files:
        assert file in result



def test_discovery_returns_sorted_results(
        tmp_path: Path,
) -> None:
    """
    Deve retornar os arquivos encontrados em ordem alfabética.
    """

    # Arrange
    files = [
        tmp_path / "Zeta.mp3",
        tmp_path / "Alpha.flac",
        tmp_path / "Delta.wav",
        tmp_path / "Beta.mp3",
        ]

    for file in files:
        file.touch()

    expected = sorted(files)

    # Act
    result = discover(tmp_path)

    # Assert
    assert result == expected


def test_discovery_raises_file_not_found_for_missing_directory(
        tmp_path: Path,
) -> None:
    """
    Deve lançar FileNotFoundError quando o diretório não existir.
    """

    # Arrange
    missing_directory = tmp_path / "Music"

    # Act / Assert
    with pytest.raises(FileNotFoundError):
        discover(missing_directory)


def test_discovery_raises_not_a_directory_for_file_path(
        tmp_path: Path,
) -> None:
    """
    Deve lançar NotADirectoryError quando o caminho informado
    não for um diretório.
    """

    # Arrange
    file_path = tmp_path / "Queen.mp3"
    file_path.touch()

    # Act / Assert
    with pytest.raises(NotADirectoryError):
        discover(file_path)