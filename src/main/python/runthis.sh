#!/bin/bash
py -3.12 -m venv .venv312
source .venv312/Scripts/activate
python -m pip install --upgrade pip
python -m pip install torch transformers==4.41.2 tokenizers==0.19.1
python test2.pt
