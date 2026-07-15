# 🎵 Melody Sync

> Organize, analyze and explore your local music library.

Melody Sync is an open-source Python application designed to scan local music libraries, extract audio metadata and generate useful statistics.

The project emphasizes clean architecture, modular design, automated testing and incremental development.

---

## 🚧 Project Status

**Current Version**

`v0.1.0-dev`

**Current Milestone**

✅ Core MVP completed

The project currently provides a fully functional backend capable of:

- discovering audio files
- reading metadata
- creating music objects
- generating library statistics

A command-line interface (CLI) is the next milestone.

---

## ✨ Features

### Implemented

- ✅ Audio file discovery
- ✅ Metadata extraction
- ✅ Song model
- ✅ Library statistics
- ✅ Support for nested directories
- ✅ Automated tests

### Planned

- ⏳ Command Line Interface (CLI)
- ⏳ Real library scanning
- ⏳ Configuration file
- ⏳ Playlist management
- ⏳ Duplicate detection
- ⏳ Graphical User Interface (GUI)

---

## 🏗 Architecture

```
Music Library
      │
      ▼
 Discovery
      │
      ▼
 Metadata
      │
      ▼
  Scanner
      │
      ▼
 Song Objects
      │
      ▼
 Statistics
```

### Models

- Song
- LibraryStatistics

### Core

- Discovery
- Metadata
- Scanner
- Statistics

---

## 📂 Project Structure

```text
melody_sync/
│
├── core/          # Business logic
├── models/        # Domain models
│
docs/              # Documentation
tests/             # Automated tests
```

---

## 📖 Documentation

Project documentation is available in the `docs/` directory.

Main documents include:

- Architecture
- Handbook
- Sprint Board
- Sprint Journal
- Project History
- Test Plan
- Test Report

---

## 🗺 Roadmap

### Milestone 1 — Core MVP

- [x] Song
- [x] LibraryStatistics
- [x] Discovery
- [x] Metadata
- [x] Scanner
- [x] Statistics

---

### Milestone 2 — Command Line Interface
- [ ] Interactive CLI

- [ ] Real library scan

- [ ] Configuration system

---

### Milestone 3

- [ ] Graphical User Interface
- [ ] Playlist management
- [ ] Duplicate detection
- [ ] Export tools

---

## Tech Stack

- Python 3.14
- pytest
- mutagen
- pathlib
- dataclasses

---

## Requirements

- Python 3.14+
- pip
## 🚀 Quick Start

---

```bash
git clone https://github.com/jotatw/Melody-Sync.git

cd Melody-Sync

python -m venv .venv

source .venv/bin/activate

pip install -e .

pytest -v
```

---

## 🧪 Testing

The project currently contains **54 automated tests**.

Run all tests:

```bash
pytest -v
```

Current result:

```text
54 passed
```

---

### Quality Assurance

- 54 automated tests
- Unit tests
- Integration tests
- Real audio fixtures

---

## 🤝 Contributing

The project is under active development.

Contributions, ideas and suggestions are welcome after the Core MVP stabilization.

---

## 📄 License

MIT License