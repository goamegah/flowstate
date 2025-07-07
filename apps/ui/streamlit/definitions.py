import os
from pathlib import Path

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
DATALOADER_DIR = os.path.join(Path(ROOT_DIR), 'dataloader')
PAGES_DIR = os.path.join(Path(ROOT_DIR), 'pages')
QUERIES_DIR = os.path.join(Path(DATALOADER_DIR), 'queries')