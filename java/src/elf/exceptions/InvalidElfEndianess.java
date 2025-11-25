package elf.exceptions;

public class InvalidElfEndianess extends ElfException {
    public InvalidElfEndianess(byte endianess) {
        super(String.format("0x%02X", endianess));
    }
}
