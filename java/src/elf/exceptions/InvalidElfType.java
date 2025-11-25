package elf.exceptions;

public class InvalidElfType extends ElfException {
    public InvalidElfType(short type) {
        super(String.format("0x%04X", type));
    }
}
