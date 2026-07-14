from dataclasses import dataclass
from pathlib import Path


@dataclass(slots=True)
class Song:
    """
    Representa uma música da biblioteca do Melody Sync.
    """

    # ==========================
    # Arquivo
    # ==========================

    path: Path
    size: int

    # ==========================
    # Metadados
    # ==========================

    title: str | None = None
    artist: str | None = None
    album: str | None = None

    # ==========================
    # Informações Técnicas
    # ==========================

    duration: float | None = None
    bitrate: int | None = None
    sample_rate: int | None = None
    channels: int | None = None
    codec: str | None = None

    @property
    def filename(self) -> str:
        """
        Retorna o nome do arquivo.
        """
        return self.path.name

    @property
    def extension(self) -> str:
        """
        Retorna a extensão do arquivo em letras minúsculas,
        sem o ponto inicial.
        """
        return self.path.suffix.lower().lstrip(".")

    @property
    def is_lossless(self) -> bool:
        """
        Retorna True se o formato for lossless.
        """
        return self.extension in {
            "flac",
            "wav",
            "aiff",
        }

    @property
    def directory(self) -> Path:
        """
        Retorna o diretório da música.
        """
        return self.path.parent

    @property
    def size_mb(self) -> float:
        """
        Retorna o tamanho em MB.
        """
        return self.size / (1024 * 1024)

    @property
    def size_gb(self) -> float:
        return self.size / (1024 ** 3)

    @property
    def duration_minutes(self) -> float:
        """
        Retorna a duração em minutos.
        """
        return self.duration / 60 if self.duration else 0

    @property
    def has_metadata(self) -> bool:
        """
        Indica se a música possui metadados básicos.
        """
        return bool(
            self.title and self.title.strip()
            and self.artist and self.artist.strip()
        )