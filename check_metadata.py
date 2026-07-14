from pathlib import Path

from mutagen import File

AUDIO_DIR = Path("tests/fixtures/audio")

for audio_file in AUDIO_DIR.iterdir():
    if not audio_file.is_file():
        continue

    print("=" * 50)
    print(audio_file.name)

    audio = File(audio_file, easy=True)
    full = File(audio_file)

    print("\nTags:")

    if audio is None:
        print("Arquivo não suportado.")
        continue

    if not audio.keys():
        print("Nenhuma")
    else:
        for key, value in audio.items():
            print(f"{key}: {value}")

    print("\nInformações:")

    if full and full.info:
        info = full.info

        print(f"Duração     : {getattr(info, 'length', None)}")
        print(f"Bitrate     : {getattr(info, 'bitrate', None)}")
        print(f"Sample Rate : {getattr(info, 'sample_rate', None)}")
        print(f"Channels    : {getattr(info, 'channels', None)}")
        print(f"Codec       : {type(info).__name__}")