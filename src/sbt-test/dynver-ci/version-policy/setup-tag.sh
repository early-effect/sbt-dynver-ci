#!/usr/bin/env bash
set -euo pipefail
git init
git config user.email "ci@example.com"
git config user.name "CI"
git config commit.gpgsign false
echo 'readme' > README.md
git add README.md build.sbt project/build.properties project/plugins.sbt
git commit -m "init"
git tag v0.2.0
