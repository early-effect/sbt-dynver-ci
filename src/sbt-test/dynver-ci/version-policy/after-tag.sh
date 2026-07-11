#!/usr/bin/env bash
set -euo pipefail
echo 'more' >> README.md
git add README.md
git commit -m "after tag"
