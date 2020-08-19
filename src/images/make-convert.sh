#!/bin/sh -e
dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
for src in "$dir"/*.png
do
  echo "Processing $(basename "$src")..."
  for sz in 16 24 32 48
  do
    file=$(basename $src)
    mkdir -p "${dir}/../../src/main/webapp/images/${sz}x${sz}"
    dst="${dir}/../../src/main/webapp/images/${sz}x${sz}/${file}"

    echo "$dst" -o "$src" -nt "$dst"

    if [ ! -e "$dst" -o "$src" -nt "$dst" ]
    then
      echo -n "  generating ${sz}x${sz}..."
      mkdir "${dir}/../../src/main/webapp/images/${sz}x${sz}" > /dev/null 2>&1 || true
      convert $src -resize ${sz}x${sz} $dst
    fi
  done
done
