#!/bin/sh

# Decrypt the file
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --quiet --batch --yes --decrypt --passphrase="$RELEASE_SECRET_PASSPHRASE" \
--output app/src/release/google-services.json app/src/release/google-services.json.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$RELEASE_SECRET_PASSPHRASE" \
--output data/src/release/res/values/secrets.xml data/src/release/res/values/secrets.xml.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$RELEASE_SECRET_PASSPHRASE" \
--output app/src/internal/google-services.json app/src/internal/google-services.json.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$RELEASE_SECRET_PASSPHRASE" \
--output data/src/internal/res/values/secrets.xml data/src/internal/res/values/secrets.xml.gpg
