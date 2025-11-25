package elf.exceptions;

public class InvalidElfClass extends ElfException {
    public InvalidElfClass(byte elf_class) {
        super(String.format("0x%02X", elf_class));
    }
}
