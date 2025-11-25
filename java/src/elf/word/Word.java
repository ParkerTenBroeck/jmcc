package elf.word;

public sealed interface Word permits W32, W64{

    long long_value();
    String to_str();
    String to_str_hex();
}
