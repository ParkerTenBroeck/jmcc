package elf.exceptions;

public class InvalidElfMagicNumber extends ElfException {
    public InvalidElfMagicNumber(int magic) {
        super(String.format("0x%04X", magic));
    }
}
