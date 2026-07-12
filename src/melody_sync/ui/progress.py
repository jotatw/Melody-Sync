from rich.progress import track


def track_files(files):

    return track(
        files,
        description="Analisando músicas..."
    )