package elf.exceptions;

public class InvalidElfVersion extends ElfException {
    public InvalidElfVersion(byte version) {
        super(String.format("0x%02X", version));
    }
}
