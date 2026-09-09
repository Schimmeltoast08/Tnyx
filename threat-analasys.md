# Vault Theft
Vault protected by password that's stored as a hash with salt

# Forensic analasys
Minimize Lifetime of sensitive Data in Memory to be safer against crashes
# Corrupt Vault
add shasum of vault to vault //tcrypt + AES-GCM

# What will not be protected
compromised OS
keyloggers
compromised JVM
Social Engineering

# Design choices
AES-GCM for failed decrypt on corrupt file
KDF must be slow or expensive to protect against brute force ex: Argon2id
build encryption using interfaces for future modularity //strategy pattern
Avoid Strings, use char[] to later wipe from memory
Auto-lock after N minutes
Never overwrite old vault, if crash then vault dead. Instead, make new vault and then delete old vault, insert old vault timestamps // atomic replacement
Encrypt a random 256-bit key (DEK) using Key encryption Key (KEK) --> on Master password change, no need to re-encrypt whole vault, just re-encrypt DEK with new KEK
Only have nececairy metadata in plain text, encrypt the rest

# Vault design
format version

kdf{
  used algorithm
  salt
  parameters //smart
}
encryption process{
  algorithm
  nonce
  encrypted Vault key // do more research on DEK u KEK
}
Time{
  creation date
  last edited date
}
encrypted data{
  nonce
  data aka ciphertext
}
maby additional metadata //optional //user-settings



