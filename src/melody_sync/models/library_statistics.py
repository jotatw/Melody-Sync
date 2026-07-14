from dataclasses import dataclass


@dataclass(slots=True)
class LibraryStatistics:
    """
    Representa o resumo estatístico de uma biblioteca musical.
    """

    # ==========================
    # Biblioteca
    # ==========================

    total_songs: int

    # ==========================
    # Metadados
    # ==========================

    unique_artists: int
    unique_albums: int

    # ==========================
    # Armazenamento
    # ==========================

    total_size: int
    formats: dict[str, int]

    # ==========================
    # Qualidade
    # ==========================

    total_duration: float
    average_bitrate: float

    # ==========================
    # Propriedades
    # ==========================

    @property
    def total_size_mb(self) -> float:
        """Retorna o tamanho total da biblioteca em MB."""
        return self.total_size / (1024 * 1024)

    @property
    def total_size_gb(self) -> float:
        """Retorna o tamanho total da biblioteca em GB."""
        return self.total_size / (1024 ** 3)

    @property
    def total_duration_minutes(self) -> float:
        """Retorna a duração total da biblioteca em minutos."""
        return self.total_duration / 60

    @property
    def total_duration_hours(self) -> float:
        """Retorna a duração total da biblioteca em horas."""
        return self.total_duration / 3600

    @property
    def average_duration(self) -> float:
        """Retorna a duração média das músicas."""
        if self.total_songs == 0:
            return 0.0

        return self.total_duration / self.total_songs

    @property
    def is_empty(self) -> bool:
        """Indica se a biblioteca está vazia."""
        return self.total_songs == 0

    @property
    def average_bitrate_kbps(self) -> float:
        return self.average_bitrate / 1000